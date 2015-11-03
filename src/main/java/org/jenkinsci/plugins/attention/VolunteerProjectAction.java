package org.jenkinsci.plugins.attention;

import hudson.model.Action;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class VolunteerProjectAction implements Action {

    private VolunteerHistory history;

    public VolunteerProjectAction(VolunteerHistory history) {
        this.history = history;
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
        return null;
    }

    public String getURL() {
        return "plugin/attention";
    }

    @Exported(visibility = 2)
    public VolunteerHistory getHistory() {
        return history;
    }
}
