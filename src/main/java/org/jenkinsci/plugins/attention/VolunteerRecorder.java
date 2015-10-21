package org.jenkinsci.plugins.attention;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.attention.response.Team;
import org.jenkinsci.plugins.attention.tools.MailClient;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

@SuppressWarnings("unchecked")
@Extension
public class VolunteerRecorder extends Recorder {

    private List<UserOperation> history;

    public VolunteerRecorder() {
        history = new LinkedList<>();
    }

    public synchronized void logOperation(UserOperation operation) {
        this.history.add(operation);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        ArrayList<Action> actions = new ArrayList<Action>();
        if (project.getLastBuild() != null) {
            actions.add(new VolunteerAction(this, project.getLastBuild(), project.getLastBuild().getAction(
                    VolunteerAction.class)));
        }
        return actions;
    }

    @Override
    public final boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        if (build.getResult().isWorseThan(Result.SUCCESS)) {
            build.addAction(new VolunteerAction(this, build, build.getPreviousBuild() != null ? build
                    .getPreviousBuild().getAction(VolunteerAction.class) : null));
        }
        return true;
    }

    @Exported(visibility = 2)
    public List<UserOperation> getHistory() {
        return history;
    }

    public void setHistory(List<UserOperation> history) {
        this.history = history;
    }

    public static class UserOperation {
    }

    @Extension
    public static final class VolunteerDescriptor extends BuildStepDescriptor<Publisher> {

        private List<Team> teamList = new LinkedList<Team>();
        private String emailFrom;
        private String emailReplyTo;
        private String emailServer;
        private String emailUsername;
        private String emailPassword;
        private boolean showAllView = true;

        private MailClient client;

        public VolunteerDescriptor() {
            super(VolunteerRecorder.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Attention plugin";
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public VolunteerRecorder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new VolunteerRecorder();
        }

        public FormValidation doCheckEmailFrom(@QueryParameter String value) throws IOException, ServletException {
            if (value.contains("@") && value.contains(".") && value.length() > 3) {
                return FormValidation.ok();
            }
            return FormValidation.error("Invalid E-mail");
        }

        public FormValidation doCheckEmailReplyTo(@QueryParameter String value) throws IOException, ServletException {
            if (value.contains("@") && value.contains(".") && value.length() > 3) {
                return FormValidation.ok();
            }
            return FormValidation.error("Invalid E-mail");
        }

        public FormValidation doCheckMail(@QueryParameter String value) throws IOException, ServletException {
            if (value.contains("@") && value.contains(".") && value.length() > 3) {
                return FormValidation.ok();
            }
            return FormValidation.error("Invalid E-mail");
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            this.getTeamList().clear();
            emailFrom = formData.getString("emailFrom");
            emailReplyTo = formData.getString("emailReplyTo");
            emailServer = formData.getString("emailServer");
            emailUsername = formData.getString("emailUsername");
            emailPassword = formData.getString("emailPassword");
            showAllView = formData.getBoolean("showAllView");
            if (formData.containsKey("teamList")) {
                Object json = formData.get("teamList");
                if (json instanceof JSONArray) {
                    JSONArray jsonArray = formData.getJSONArray("teamList");
                    for (Object devForm : jsonArray) {
                        getTeamList().add(
                                new Team(((JSONObject) devForm).getString("name"), ((JSONObject) devForm)
                                        .getString("mail")));
                    }
                } else {
                    JSONObject devForm = formData.getJSONObject("teamList");
                    getTeamList()
                            .add(new Team(((JSONObject) devForm).getString("name"), ((JSONObject) devForm)
                                    .getString("mail")));
                }
            }

            client = new MailClient(emailServer, emailUsername, emailPassword, emailReplyTo, emailFrom,
                    this.getTeamList());

            save();

            return super.configure(req, formData);
        }

        public String getEmailFrom() {
            return emailFrom;
        }

        public void setEmailFrom(String emailFrom) {
            this.emailFrom = emailFrom;
        }

        public String getEmailReplyTo() {
            return emailReplyTo;
        }

        public void setEmailReplyTo(String emailReplyTo) {
            this.emailReplyTo = emailReplyTo;
        }

        public String getEmailServer() {
            return emailServer;
        }

        public void setEmailServer(String emailServer) {
            this.emailServer = emailServer;
        }

        public String getEmailUsername() {
            return emailUsername;
        }

        public void setEmailUsername(String emailUsername) {
            this.emailUsername = emailUsername;
        }

        public String getEmailPassword() {
            return emailPassword;
        }

        public void setEmailPassword(String emailPassword) {
            this.emailPassword = emailPassword;
        }

        public MailClient getClient() {
            return client;
        }

        public void setClient(MailClient client) {
            this.client = client;
        }

        public List<Team> getTeamList() {
            return teamList;
        }

        public void setTeamList(List<Team> teamList) {
            this.teamList = teamList;
        }

        public boolean isShowAllView() {
            return showAllView;
        }

        public void setShowAllView(boolean showAllView) {
            this.showAllView = showAllView;
        }

    }

}
