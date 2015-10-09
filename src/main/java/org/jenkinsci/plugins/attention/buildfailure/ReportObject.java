package org.jenkinsci.plugins.attention.buildfailure;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.attention.VolunteerAction;
import org.jenkinsci.plugins.attention.VolunteerCollection;

public class ReportObject {
    private int failedBuilds;
    private String lastFailedBuildLink;
    private String lastFailedBuildname;
    private LinkedList<VolunteerCollection> volunteers;
    private String fixSubmittedByName;
    private boolean fixSubmitted;
    private String failureString;
    private List<String> originalBreakers = new LinkedList<String>();

    private static HashMap<String, ReportObject> MEMORY = new HashMap<String, ReportObject>();

    public synchronized static void removeCache(@SuppressWarnings("rawtypes") Job project) {
        MEMORY.remove(project.getName());
    }

    public synchronized static ReportObject getInstance(@SuppressWarnings("rawtypes") Job project,
            @SuppressWarnings("rawtypes") Run lastBuildCompleted) {
        ReportObject object = MEMORY.get(project.getName());
        if (object == null) {
            object = new ReportObject(project, lastBuildCompleted);
            MEMORY.put(project.getName(), object);
        } else {
            @SuppressWarnings("rawtypes")
            Run lastFailedBuild = project.getLastFailedBuild();
            if (lastFailedBuild != null
                    && lastFailedBuild.getFullDisplayName().equalsIgnoreCase(object.getLastFailedBuildname())) {
                return object;
            }
            removeCache(project);
            object = new ReportObject(project, lastBuildCompleted);
            MEMORY.put(project.getName(), object);
        }
        return object;
    }

    @SuppressWarnings("rawtypes")
    public ReportObject(Job project, Run lastBuild) {
        Run lastFailedBuild = project.getLastBuild();
        Run lastSuccessfulBuild = project.getLastSuccessfulBuild();
        Run firstBrokenBuild = project.getFirstBuild();
        if (lastSuccessfulBuild != null) {
            setFailedBuilds(lastBuild.getNumber() - project.getLastSuccessfulBuild().getNumber());
            firstBrokenBuild = (AbstractBuild) lastSuccessfulBuild.getNextBuild();
        } else {
            setFailedBuilds(lastBuild.getNumber());
        }

        if (firstBrokenBuild != null) {
            if (firstBrokenBuild instanceof AbstractBuild) {
                AbstractBuild build = (AbstractBuild) firstBrokenBuild;
                for (Object u : build.getCulprits()) {
                    if (u instanceof User) {
                        getOriginalBreakers().add(((User) u).getFullName());
                    }
                }
            }
        }

        this.setLastFailedBuildname(lastFailedBuild.getFullDisplayName());
        setLastFailedBuildLink(Jenkins.getInstance().getRootUrl() + "/" + lastFailedBuild.getUrl());
        VolunteerAction action = lastBuild.getAction(VolunteerAction.class);
        setVolunteers(action.getVolunteers());
        setFixSubmittedByName(action.getFixSubmittedByName());
        setFixSubmitted(action.isFixSubmitted());
        int count = 0;
        int failed = 0;
        Run checkme = lastBuild;
        for (int i = 0; i < 10; i++) {
            count++;

            if (checkme.getResult().isWorseThan(Result.SUCCESS)) {
                failed++;
            }
            checkme = checkme.getPreviousBuild();
            if (checkme == null) {
                break;
            }

        }

        if (count > 0) {
            setFailureString(failed + " time" + ((failed > 1) ? "s" : "") + " out of the " + count);
        } else {
            setFailureString("never run");
        }
    }

    public int getFailedBuilds() {
        return failedBuilds;
    }

    public void setFailedBuilds(int failedBuilds) {
        this.failedBuilds = failedBuilds;
    }

    public String getLastFailedBuildLink() {
        return lastFailedBuildLink;
    }

    public void setLastFailedBuildLink(String lastFailedBuildLink) {
        this.lastFailedBuildLink = lastFailedBuildLink;
    }

    public String getLastFailedBuildname() {
        return lastFailedBuildname;
    }

    public void setLastFailedBuildname(String lastFailedBuildname) {
        this.lastFailedBuildname = lastFailedBuildname;
    }

    public String getFixSubmittedByName() {
        return fixSubmittedByName;
    }

    public void setFixSubmittedByName(String fixSubmittedByName) {
        this.fixSubmittedByName = fixSubmittedByName;
    }

    public LinkedList<VolunteerCollection> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(LinkedList<VolunteerCollection> volunteers) {
        this.volunteers = volunteers;
    }

    public boolean isFixSubmitted() {
        return fixSubmitted;
    }

    public void setFixSubmitted(boolean fixSubmitted) {
        this.fixSubmitted = fixSubmitted;
    }

    public List<String> getOriginalBreakers() {
        return originalBreakers;
    }

    public void setOriginalBreakers(List<String> originalBreakers) {
        this.originalBreakers = originalBreakers;
    }

    public String getFailureString() {
        return failureString;
    }

    public void setFailureString(String failureString) {
        this.failureString = failureString;
    }

}
