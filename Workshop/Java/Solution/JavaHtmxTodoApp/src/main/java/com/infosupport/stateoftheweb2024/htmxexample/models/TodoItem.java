package com.infosupport.stateoftheweb2024.htmxexample.models;

public class TodoItem {

    private int id;
    private String title;

    private boolean completed;

    public TodoItem(int id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public void complete() {
        this.completed = true;
    }

    public void toggle() {
        this.completed = !this.completed;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }
}
