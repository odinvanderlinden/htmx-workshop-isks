namespace DotnetTodoApp.Models;

public class TodoItem
{
    public required int Id { get; set; }
    public required string Title { get; set; }
    public required bool IsCompleted { get; set; }
}