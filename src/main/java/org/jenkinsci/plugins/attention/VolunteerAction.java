package org.jenkinsci.plugins.attention;

import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.attention.VolunteerRecorder.VolunteerDescriptor;
import org.jenkinsci.plugins.attention.buildfailure.ReportObject;
import org.jenkinsci.plugins.attention.pbe.DetectedIssue;
import org.jenkinsci.plugins.attention.response.JavaScriptResponse;
import org.jenkinsci.plugins.attention.response.PageData;
import org.jenkinsci.plugins.attention.response.SimpleUser;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class VolunteerAction implements Action {

    private LinkedList<VolunteerCollection> volunteers = new LinkedList<VolunteerCollection>();

    private Run<?, ?> build = null;
    private VolunteerRecorder recorder = null;
    private boolean fixSubmitted = false;
    private String fixSubmittedByName = "";
    private boolean intermittentProblem = false;
    private String intermittentByName = "";
    private ArrayList<DetectedIssue> issues = new ArrayList<DetectedIssue>();

    public VolunteerAction() {
        DetectedIssue defaultIssue = new DetectedIssue();
        defaultIssue.setupTestFailure(DetectedIssue.DEFAULT_CATEGORY, "");
        issues.add(defaultIssue);
    }

    public boolean showForm() {
        return User.current() != null;
    }

    @JavaScriptMethod
    public PageData getPageData() {
        PageData page = new PageData();
        page.setIssues(issues);
        page.setTeams(((VolunteerDescriptor) recorder.getDescriptor()).getTeamList());
        page.setVolunteers(volunteers);
        for (User user : User.getAll()) {
            if (!user.getDisplayName().contains("@")) {
                page.getUsers().add(new SimpleUser(user));
            }
        }
        page.sortUsers();
        return page;
    }

    public void processIssues(List<DetectedIssue> issues) {
        this.issues.addAll(issues);
        List<VolunteerCollection> unVolunteer = new LinkedList<>();
        for (VolunteerCollection vol : volunteers) {
            boolean errorPersist = true;
            if (vol.getIssue() != null
                    && !vol.getIssue().getErrorHeader().equalsIgnoreCase(DetectedIssue.DEFAULT_CATEGORY)) {
                errorPersist = false;
                for (DetectedIssue issue : issues) {
                    if (vol.getIssue().getErrorHeader().equalsIgnoreCase(issue.getErrorHeader())) {
                        errorPersist = true;
                    }
                }
            }
            if (!errorPersist) {
                unVolunteer.add(vol);
            }
        }
        for (VolunteerCollection vol : unVolunteer) {
            try {
                this.performUnVolunteer(build, vol);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        synchronized (build) {
            try {
                build.save();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public String getCurrentUserID() {
        User current = User.current();
        return (current != null) ? current.getId() : "guest";
    }

    public VolunteerAction(VolunteerRecorder recorder, Run<?, ?> build, VolunteerAction prevVolunteerAction) {
        this.build = build;
        this.recorder = recorder;
        if (prevVolunteerAction != null) {
            for (VolunteerCollection col : prevVolunteerAction.getVolunteers()) {
                volunteers.add(col.copy());
            }
            this.fixSubmitted = prevVolunteerAction.fixSubmitted;
            this.fixSubmittedByName = prevVolunteerAction.fixSubmittedByName;
            this.intermittentProblem = prevVolunteerAction.intermittentProblem;
            this.intermittentByName = prevVolunteerAction.intermittentByName;
        }

    }

    // This is used by the volunteer column
    public String formatVolunteerString() {
        String names = "";
        for (VolunteerCollection volunteer : this.getVolunteers()) {
            names = names.concat(volunteer.getFullName() + ", ");
        }
        return names.length() > 0 ? names.substring(0, names.length() - 2) : "";
    }

    public String getMessage() {
        User user = User.current();
        if (user != null) {
            for (VolunteerCollection col : volunteers) {
                if (col.getId().equals(user.getId())) {
                    return col.getComment();
                }
            }
        }
        return "";
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "volunteer";
    }

    public String getUsername() {
        return User.current() != null ? User.current().getId() : "";
    }

    public String getUsers() {
        String userlist = "";
        User me = User.current();
        for (User u : User.getAll()) {
            String defaultValue = "";
            if (me != null && u.equals(me)) {
                defaultValue = "selected=\"selected\"";
            }
            userlist = userlist.concat("<option " + defaultValue + " value=\"" + u.getId() + "\">" + u.getFullName()
                    + "</option>");
        }

        return userlist;
    }

    @Exported(visibility = 2)
    public LinkedList<VolunteerCollection> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(LinkedList<VolunteerCollection> volunteers) {
        this.volunteers = volunteers;
    }

    public String getIconUrl() {
        return Jenkins.getInstance().getRootUrl() + "images/24x24";
    }

    public String getSmallIconUrl() {
        return Jenkins.getInstance().getRootUrl() + "images/16x16";
    }

    public String getPluginURL() {
        return Jenkins.getInstance().getRootUrl() + "plugin/Volunteer";
    }

    public void performUnVolunteer(Run<?, ?> localBuild, VolunteerCollection toRemove) throws IOException {
        VolunteerAction action = localBuild.getAction(this.getClass());
        if (action == null) {
            return;
        }
        if (action.getVolunteers() == null) {
            return;
        }
        for (VolunteerCollection vc : action.getVolunteers()) {
            if (vc.getId().equalsIgnoreCase(toRemove.getId())) {
                action.getVolunteers().remove(vc);
            }
        }
        localBuild.save();
        if (localBuild.getResult().isWorseThan(Result.SUCCESS) && localBuild.getNextBuild() != null) {
            performUnVolunteer(localBuild.getNextBuild(), toRemove);
        }
    }

    @JavaScriptMethod
    public JavaScriptResponse unVolunteer(String id) {
        try {
            if (recorder == null) {
                return new JavaScriptResponse("The recorder is null, please notify your Jenkins administrator", true,
                        volunteers);
            }
            VolunteerCollection toRemove = null;
            for (VolunteerCollection vc : volunteers) {
                if (vc.getId().equalsIgnoreCase(id)) {
                    toRemove = vc;
                    break;
                }
            }

            VolunteerDescriptor vd = (VolunteerDescriptor) recorder.getDescriptor();
            performUnVolunteer(build, toRemove);
            vd.getClient().notifyUnVolunteered(toRemove, User.current(), build);
            ReportObject.removeCache(build.getParent());
            return new JavaScriptResponse(id + " was removed", false, volunteers);
        } catch (Throwable e) {
            e.printStackTrace();
            return new JavaScriptResponse(e.getMessage() + "<br />" + generateStackString(e), true, volunteers);
        }
    }

    private void updateFixOnTheWayStatus(Run<?, ?> localBuild, boolean status) throws IOException {
        VolunteerAction action = localBuild.getAction(this.getClass());
        if (action == null) {
            return;
        }
        action.setFixSubmitted(status);
        action.setFixSubmittedByName((status) ? User.current().getFullName() : "");
        localBuild.save();
        if (localBuild.getResult().isWorseThan(Result.SUCCESS) && localBuild.getNextBuild() != null) {
            updateFixOnTheWayStatus(localBuild.getNextBuild(), status);
        }

        if (status) {
            performVolunteer(localBuild, new VolunteerCollection(User.current().getId(), false, "I've submitted a fix",
                    true, new DetectedIssue()), false);
        }
        ReportObject.removeCache(build.getParent());
    }

    @JavaScriptMethod
    public JavaScriptResponse flagFixSubmitted(boolean newStatus) {
        try {
            updateFixOnTheWayStatus(build, newStatus);
            return new JavaScriptResponse(getFixSubmittedByName(), false, volunteers);
        } catch (Throwable e) {
            e.printStackTrace();
            return new JavaScriptResponse(e.getMessage() + "<br />" + generateStackString(e), true, volunteers);
        }
    }

    private void updateIntermittentStatus(Run<?, ?> localBuild, boolean status) throws IOException {
        VolunteerAction action = localBuild.getAction(this.getClass());
        if (action == null) {
            return;
        }
        action.setIntermittentProblem(status);
        action.setIntermittentByName((status) ? User.current().getFullName() : "");
        localBuild.save();
        if (localBuild.getResult().isWorseThan(Result.SUCCESS) && localBuild.getNextBuild() != null) {
            updateIntermittentStatus(localBuild.getNextBuild(), status);
        }

        if (status) {
            performVolunteer(localBuild, new VolunteerCollection(User.current().getId(), false,
                    "I consider this to be an intermittent problem", true, new DetectedIssue()), false);
        }
        ReportObject.removeCache(build.getParent());
    }

    @JavaScriptMethod
    public JavaScriptResponse flagIntermittent(boolean newStatus) {
        try {
            updateIntermittentStatus(build, newStatus);
            return new JavaScriptResponse(getIntermittentByName(), false, volunteers);
        } catch (Throwable e) {
            e.printStackTrace();
            return new JavaScriptResponse(e.getMessage() + "<br />" + generateStackString(e), true, volunteers);
        }
    }

    @JavaScriptMethod
    public JavaScriptResponse updateVolunteer(String comment, boolean fixing, String volunteerID, boolean isTeam,
            String issueHeader) {
        try {
            if (volunteerID == "" || volunteerID == null) {
                return new JavaScriptResponse("You need to select a user/team to volunteer", true, volunteers);
            }

            if (recorder == null) {
                return new JavaScriptResponse("The recorder is null, please notify your Jenkins administrator", true,
                        volunteers);
            }
            DetectedIssue issue = new DetectedIssue();
            for (DetectedIssue existingIssue : issues) {
                if (existingIssue.getErrorHeader().equalsIgnoreCase(issueHeader)) {
                    issue = existingIssue;
                }
            }
            VolunteerCollection volunteerData = new VolunteerCollection(volunteerID, isTeam, comment, fixing, issue);
            if (volunteerData.getId() == "" || volunteerData.getId() == null) {
                return new JavaScriptResponse(
                        "Failed to create the volunteer data, the current user ID is either null or empty", true,
                        volunteers);
            }
            VolunteerDescriptor vd = (VolunteerDescriptor) recorder.getDescriptor();
            performVolunteer(build, volunteerData, true);

            if (fixing) {
                vd.getClient().notifyNewFixer(volunteerData, User.current(), build);
            } else {
                vd.getClient().notifyNewInvestigator(volunteerData, User.current(), build);
            }
            ReportObject.removeCache(build.getParent());
            return new JavaScriptResponse(volunteerData.getFullName() + " was volunteered", false, volunteers);
        } catch (Throwable e) {
            e.printStackTrace();
            return new JavaScriptResponse(e.getMessage() + "<br />" + generateStackString(e), true, volunteers);
        }
    }

    private void performVolunteer(Run<?, ?> localBuild, VolunteerCollection volunteerData, boolean updateIfFound)
            throws IOException {
        try {
            boolean found = false;
            VolunteerAction action = localBuild.getAction(this.getClass());
            if (action == null) {
                return;
            }
            for (VolunteerCollection vc : action.getVolunteers()) {
                if (vc.getId().equalsIgnoreCase(volunteerData.getId())) {
                    vc.setComment(volunteerData.getComment());
                    vc.setFixing(volunteerData.isFixing());
                    vc.setIssue(volunteerData.getIssue());
                    found = true;
                    break;
                }
            }
            if (!found) {
                action.getVolunteers().add(volunteerData);
            } else if (!updateIfFound) {
                return;
            }

            if (localBuild.getResult().isWorseThan(Result.SUCCESS) && localBuild.getNextBuild() != null) {
                performVolunteer(localBuild.getNextBuild(), volunteerData, updateIfFound);
            }
        } finally {
            localBuild.save();
        }
    }

    private String generateStackString(final Throwable e) {
        e.printStackTrace();
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } finally {
            IOUtils.closeQuietly(sw);
            IOUtils.closeQuietly(pw);
        }
    }

    @Exported(visibility = 2)
    public boolean isFixSubmitted() {
        return fixSubmitted;
    }

    public void setFixSubmitted(boolean fixSubmitted) {
        this.fixSubmitted = fixSubmitted;
    }

    @Exported(visibility = 2)
    public String getFixSubmittedByName() {
        return fixSubmittedByName;
    }

    public void setFixSubmittedByName(String fixSubmittedByName) {
        this.fixSubmittedByName = fixSubmittedByName;
    }

    @Exported(visibility = 2)
    public boolean isIntermittentProblem() {
        return intermittentProblem;
    }

    public void setIntermittentProblem(boolean intermittentProblem) {
        this.intermittentProblem = intermittentProblem;
    }

    @Exported(visibility = 2)
    public String getIntermittentByName() {
        return intermittentByName;
    }

    public void setIntermittentByName(String intermittentByName) {
        this.intermittentByName = intermittentByName;
    }

    public ArrayList<DetectedIssue> getIssues() {
        return issues;
    }

    public void setIssues(ArrayList<DetectedIssue> issues) {
        this.issues = issues;
    }

}
