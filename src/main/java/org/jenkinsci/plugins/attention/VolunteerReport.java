package org.jenkinsci.plugins.attention;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.RootAction;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.View;

import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

@Extension
public class VolunteerReport implements RootAction {

    @Override
    public String getIconFileName() {
        return "/plugin/attention/flaghand.png";
    }

    @Override
    public String getDisplayName() {
        return "Volunteer Report";
    }

    @Override
    public String getUrlName() {
        return "volunteerreport";
    }

    public String getPluginURL() {
        return Jenkins.getInstance().getRootUrl() + "plugin/attention";
    }

    public String getCurrentViewName() {
        return this.getRootView().getDisplayName();
    }

    public View getRootView() {
        View view = Stapler.getCurrentRequest().findAncestorObject(View.class);
        return view != null ? view : Jenkins.getInstance().getPrimaryView();
    }

    public static class VolunteerObject {
        private LinkedList<VolunteerJobObject> myVolunteers = new LinkedList<>();
        private LinkedList<VolunteerJobObject> allVolunteers = new LinkedList<>();

        public void addVolunteers(String projectName, List<VolunteerCollection> volunteers) {
            List<VolunteerCollection> mine = new LinkedList<>();
            for (VolunteerCollection vol : volunteers) {
                if (vol.isMe()) {
                    mine.add(vol);
                }
            }
            if (mine.size() > 0) {
                myVolunteers.add(new VolunteerJobObject(projectName, mine));
            }
            allVolunteers.add(new VolunteerJobObject(projectName, volunteers));
        }

        public LinkedList<VolunteerJobObject> getMyVolunteers() {
            return myVolunteers;
        }

        public void setMyVolunteers(LinkedList<VolunteerJobObject> myVolunteers) {
            this.myVolunteers = myVolunteers;
        }

        public LinkedList<VolunteerJobObject> getAllVolunteers() {
            return allVolunteers;
        }

        public void setAllVolunteers(LinkedList<VolunteerJobObject> allVolunteers) {
            this.allVolunteers = allVolunteers;
        }
    }

    public static class VolunteerJobObject {
        private String projectName;
        private List<VolunteerCollection> volunteers;

        public VolunteerJobObject(String projectName, List<VolunteerCollection> volunteers) {
            this.setProjectName(projectName);
            this.setVolunteers(volunteers);
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public List<VolunteerCollection> getVolunteers() {
            return volunteers;
        }

        public void setVolunteers(List<VolunteerCollection> volunteers) {
            this.volunteers = volunteers;
        }
    }

    @SuppressWarnings("rawtypes")
    public VolunteerObject getVolunteersReport() {
        VolunteerObject result = new VolunteerObject();
        List<Job> jobs = Jenkins.getInstance().getAllItems(Job.class);
        for (Job job : jobs) {
            Run<?, ?> lastBuild = job.getLastBuild();
            if (lastBuild.getResult().isWorseThan(Result.SUCCESS)) {
                VolunteerAction action = lastBuild.getAction(VolunteerAction.class);
                if (action != null) {
                    if (!action.getVolunteers().isEmpty()) {
                        result.addVolunteers(job.getDisplayName(), action.getVolunteers());
                    }
                }
            }
        }
        
        return result;
    }

}
