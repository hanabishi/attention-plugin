package org.jenkinsci.plugins.attention.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.jenkinsci.plugins.attention.VolunteerCollection;
import org.jenkinsci.plugins.attention.buildfailure.ReportObject;
import org.jenkinsci.plugins.attention.pbe.DetectedIssue;

public class PageData {
    private LinkedList<SimpleUser> users = new LinkedList<SimpleUser>();
    private List<Team> teams;
    private List<VolunteerCollection> volunteers;
    private ArrayList<DetectedIssue> issues;

    private List<String> views = new LinkedList<>();
    private List<ReportObject> buildList = new LinkedList<>();

    public ArrayList<DetectedIssue> getIssues() {
        return issues;
    }

    public void setIssues(ArrayList<DetectedIssue> issues) {
        this.issues = issues;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<VolunteerCollection> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(List<VolunteerCollection> volunteers) {
        this.volunteers = volunteers;
    }

    public void sortUsers() {
        Collections.sort(getUsers(), new Comparator<SimpleUser>() {
            @Override
            public int compare(SimpleUser u1, SimpleUser u2) {
                return u1.getName().toLowerCase().compareTo(u2.getName().toLowerCase());
            }
        });
    }

    public LinkedList<SimpleUser> getUsers() {
        return users;
    }

    public void setUsers(LinkedList<SimpleUser> users) {
        this.users = users;
    }

    public List<ReportObject> getBuildList() {
        return buildList;
    }

    public void setBuildList(List<ReportObject> buildList) {
        this.buildList = buildList;
    }

    public List<String> getViews() {
        return views;
    }

    public void setViews(List<String> views) {
        this.views = views;
    }
}
