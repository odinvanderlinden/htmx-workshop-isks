using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using DotnetHtmxTodoApp.Models;
using Htmx;

namespace DotnetHtmxTodoApp.Controllers;

public class TodoController : Controller
{
    private readonly ILogger<TodoController> _logger;
    
    private static readonly List<TodoItem> _todoItems = new()
    {
        new TodoItem { Id = 1, Title = "Learn HTMX", IsCompleted = true },
        new TodoItem { Id = 2, Title = "Build a Todo App", IsCompleted = false },
        new TodoItem { Id = 3, Title = "Add HTMX to the Todo App", IsCompleted = false }
    };

    public TodoController(ILogger<TodoController> logger)
    {
        _logger = logger;
    }

    public IActionResult Index()
    {
        return View(_todoItems);
    }
    
    // [HttpGet("active")]
    // public IActionResult GetActive() {
    //     return viewIndexOrTodoItemsFragment(TodoFilter.Active);
    // }
    //
    // [HttpGet("/completed")]
    // public IActionResult GetCompleted() {
    //     return viewIndexOrTodoItemsFragment(TodoFilter.Completed);
    // }

    [HttpPut]
    public IActionResult Toggle(int id)
    {
        var todoItem = _todoItems.FirstOrDefault(x => x.Id == id);
        if (todoItem != null)
        {
            todoItem.IsCompleted = !todoItem.IsCompleted;
        }

        return PartialView("TodoItem", todoItem);
    }
    
    [HttpPost]
    public IActionResult Add(string title)
    {
        var todoItem = new TodoItem
        {
            Id = _todoItems.Max(x => x.Id) + 1,
            Title = title,
            IsCompleted = false
        };
        _todoItems.Add(todoItem);
        return PartialView("TodoItem", todoItem);
    }

    [HttpDelete("{Id}")]
    public IActionResult Delete(int Id)
    {
        _todoItems.RemoveAll(i => i.Id == Id);
        return Ok();
    }
    
    public IActionResult PatchTitle(string title, int id)
    {
        var todoItem = _todoItems.FirstOrDefault(x => x.Id == id);
        if (todoItem != null)
        {
            todoItem.Title = title;
        }
        return PartialView("TodoItem", todoItem);
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
    }

    public enum TodoFilter {
        All,
        Active,
        Completed
    }
}