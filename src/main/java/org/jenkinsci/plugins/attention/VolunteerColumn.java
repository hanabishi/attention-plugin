package org.jenkinsci.plugins.attention;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumnDescriptor;
import hudson.views.ListViewColumn;

import org.kohsuke.stapler.DataBoundConstructor;

@Extension
public class VolunteerColumn extends ListViewColumn {

    @DataBoundConstructor
    public VolunteerColumn() {
        super();
    }

    public String getVolunteerReport(Job<?, ?> build) {
        Run<?, ?> lastCompletedBuild = build.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return "";
        }
        VolunteerAction va = lastCompletedBuild.getAction(VolunteerAction.class);
        if (va == null) {
            return "";
        }
        return va.formatVolunteerString();
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        @Override
        public String getDisplayName() {
            return "Volunteer Column";
        }
    }

}