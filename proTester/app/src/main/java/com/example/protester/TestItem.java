package com.example.protester;

public class TestItem {
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private int order;
    private Class activityClass;

    public TestItem(String id, String name, String description, boolean enabled, int order, Class activityClass) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.order = order;
        this.activityClass = activityClass;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getOrder() {
        return order;
    }

    public Class getActivityClass() {
        return activityClass;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}