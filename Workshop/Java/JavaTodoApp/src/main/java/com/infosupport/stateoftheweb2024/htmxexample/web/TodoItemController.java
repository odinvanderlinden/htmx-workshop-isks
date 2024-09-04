package com.infosupport.stateoftheweb2024.htmxexample.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.infosupport.stateoftheweb2024.htmxexample.models.TodoItem;
import com.infosupport.stateoftheweb2024.htmxexample.models.TodoItemForm;

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
    public String index(Model model) {
        return viewIndex(model);
    }

    @PostMapping()
    public String addItem(TodoItemForm form, Model model) {
        var item = new TodoItem(++idGenerator, form.getTitle(), false);
        todoItems.add(item);

        return viewIndex(model);
    }

    @PostMapping(value = "/{id}/toggle")
    public String toggle(@PathVariable("id") Long id, Model model) {
        TodoItem todoItem = todoItems.stream().filter(item -> item.getId() == id).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,
                "TodoItem with id " + id + " not found"));

        todoItem.toggle();

        return viewIndex(model);
    }

    @PostMapping("/{id}/delete")
    @ResponseStatus(value = HttpStatus.OK)
    public String deleteItem(@PathVariable("id") Long id, Model model) {
        todoItems.removeIf(item -> item.getId() == id);
        return viewIndex(model);
    }

    @PatchMapping(value = "/{id}")
    public String patchTitle(@PathVariable("id") Long id, TodoItemForm form, Model model) {
        var todo = todoItems.stream().filter(item -> item.getId() == id).findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND,
                "TodoItem with id " + id + " not found"));
        todo.setTitle(form.getTitle());
        return viewIndex(model);

    }

    // private ModelAndView viewTodoFragment(TodoItem todo) {
    //     var model = new ModelAndView();
    //     model.addObject("item", todo);
    //     model.setViewName("fragments :: todoItem");
    //     return model;
    // }
    private String viewTodoItemsFragment(Model model) {
        model.addAttribute("items", todoItems);
        return "fragments :: todoItems";
    }

    private String viewIndex(Model model) {
        viewTodoItemsFragment(model);
        model.addAttribute("newItem", new TodoItemForm());
        model.addAttribute("activeCount", todoItems.stream().filter(item -> !item.isCompleted()).count());
        return "index";
    }
}
