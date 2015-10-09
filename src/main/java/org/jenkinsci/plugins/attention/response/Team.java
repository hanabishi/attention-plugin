package org.jenkinsci.plugins.attention.response;

public class Team {
    private String mail;
    private String name;

    public Team(String name, String mail) {
        this.setName(name);
        this.setMail(mail);
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
