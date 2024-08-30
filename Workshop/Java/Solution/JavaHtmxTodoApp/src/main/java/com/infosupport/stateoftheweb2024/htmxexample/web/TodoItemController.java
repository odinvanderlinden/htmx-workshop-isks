package com.infosupport.stateoftheweb2024.htmxexample.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;

import com.infosupport.stateoftheweb2024.htmxexample.models.TodoItem;
import com.infosupport.stateoftheweb2024.htmxexample.models.TodoItemForm;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxTrigger;

@Controller
@RequestMapping("/")
public class TodoItemController {

    private static final String completedUrl = "/completed";
    private static final String activeUrl = "/active";

    private static int idGenerator = 0;

    private static final List<TodoItem> todoItems = new ArrayList<>(List.of(
            new TodoItem(++idGenerator, "Learn HTMX", true),
            new TodoItem(++idGenerator, "Build a Todo App", false),
            new TodoItem(++idGenerator, "Add HTMX to the Todo App", false)));

    @GetMapping
    public String index(Model model, HtmxRequest htmxRequest) {
        return viewIndexOrTodoItemsFragment(model, TodoFilter.ALL, htmxRequest);
    }

    @GetMapping(activeUrl)
    public String active(Model model, HtmxRequest htmxRequest) {
        return viewIndexOrTodoItemsFragment(model, TodoFilter.ACTIVE, htmxRequest);
    }

    @GetMapping(completedUrl)
    public String completed(Model model, HtmxRequest htmxRequest) {
        return viewIndexOrTodoItemsFragment(model, TodoFilter.COMPLETED, htmxRequest);
    }

    @HxRequest
    @GetMapping(value = "/active-items-fragment")
    public String activeItemsFragment(Model model) {
        model.addAttribute("activeCount", todoItems.stream().filter(item -> !item.isCompleted()).count());
        return "fragments :: active-items-fragment";
    }

    @HxRequest
    @GetMapping(value = "/filters")
    public String filtersFragment(Model model, HtmxRequest htmxRequest) {
        model.addAttribute("filter", filterFromHtmxRequest(htmxRequest));
        return "fragments :: filters";
    }

    @HxRequest
    @HxTrigger("itemsChanged")
    @PostMapping()
    public ModelAndView addItem(TodoItemForm form, HtmxRequest htmxRequest) {
        var item = new TodoItem(++idGenerator, form.getTitle(), false);
        todoItems.add(item);

        var model = new ModelAndView();
        if (filterFromHtmxRequest(htmxRequest) == TodoFilter.COMPLETED) {
            model.setStatus(HttpStatus.NO_CONTENT);
            return null;
        } else {
            model.addObject("item", item);
            model.setViewName("fragments :: todoItem");
            return model;
        }
    }

    @HxRequest
    @HxTrigger("itemsChanged")
    @PutMapping(path = "/toggle-all")
    public String toggleAll(Model model, HtmxRequest htmxRequest) {
        todoItems.forEach(item -> item.complete());
        return viewTodoItemsFragment(model, filterFromHtmxRequest(htmxRequest));
    }

    @HxRequest
    @HxTrigger("itemsChanged")
    @DeleteMapping("/completed")
    public String deleteCompletedItems(Model model, HtmxRequest htmxRequest) {
        todoItems.removeIf(item -> item.isCompleted());
        return viewTodoItemsFragment(model, filterFromHtmxRequest(htmxRequest));
    }

    @HxRequest
    @HxTrigger("itemsChanged")
    @PutMapping(value = "/{id}/toggle")
    public ModelAndView toggle(@PathVariable("id") Long id, HtmxRequest htmxRequest) {
        TodoItem todoItem = todoItems.stream().filter(item -> item.getId() == id).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "TodoItem with id " + id + " not found"));

        todoItem.toggle();

        var activeFilter = filterFromHtmxRequest(htmxRequest);
        if ((activeFilter == TodoFilter.COMPLETED && !todoItem.isCompleted())
                || (activeFilter == TodoFilter.ACTIVE && todoItem.isCompleted())) {
            return null;
        }

        return viewTodoFragment(todoItem);
    }

    @HxRequest
    @HxTrigger("itemsChanged")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteItem(@PathVariable("id") Long id) {
        todoItems.removeIf(item -> item.getId() == id);
    }

    @HxRequest
    @PatchMapping(value = "/{id}")
    public ModelAndView patchTitle(@PathVariable("id") Long id, TodoItemForm form) {
        var todo = todoItems.stream().filter(item -> item.getId() == id).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "TodoItem with id " + id + " not found"));
        todo.setTitle(form.getTitle());
        return viewTodoFragment(todo);
    }

    private ModelAndView viewTodoFragment(TodoItem todo) {
        var model = new ModelAndView();
        model.addObject("item", todo);
        model.setViewName("fragments :: todoItem");
        return model;
    }

    private String viewTodoItemsFragment(Model model, TodoFilter filter) {
        model.addAttribute("items", switch (filter) {
            case ALL -> todoItems;
            case ACTIVE -> todoItems.stream().filter(item -> !item.isCompleted()).toList();
            case COMPLETED -> todoItems.stream().filter(TodoItem::isCompleted).toList();
        });
        return "fragments :: todoItems";
    };

    private String viewIndexOrTodoItemsFragment(Model model, TodoFilter filter, HtmxRequest htmxRequest) {
        if (htmxRequest.isHtmxRequest()) {
            return viewTodoItemsFragment(model, filter);
        }
        return viewIndex(model, filter);
    }

    private String viewIndex(Model model, TodoFilter filter) {
        viewTodoItemsFragment(model, filter);
        model.addAttribute("newItem", new TodoItemForm());
        model.addAttribute("filter", filter);
        model.addAttribute("activeCount", todoItems.stream().filter(item -> !item.isCompleted()).count());
        return "index";
    }

    private TodoFilter filterFromHtmxRequest(HtmxRequest htmxRequest) {
        if (htmxRequest.getCurrentUrl().endsWith(completedUrl)) {
            return TodoFilter.COMPLETED;
        } else if (htmxRequest.getCurrentUrl().endsWith(activeUrl)) {
            return TodoFilter.ACTIVE;
        } else {
            return TodoFilter.ALL;
        }

    }

    public enum TodoFilter {
        ALL,
        ACTIVE,
        COMPLETED
    }

}
