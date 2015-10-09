package org.jenkinsci.plugins.attention.response;

import hudson.model.User;

public class SimpleUser {
    private String id;
    private String name;

    public SimpleUser(User user) {
        id = user.getId();
        name = user.getDisplayName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
