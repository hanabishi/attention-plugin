package org.jenkinsci.plugins.attention.pbe;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.listeners.RunListener;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.attention.VolunteerAction;
import org.jenkinsci.plugins.attention.extensions.AttentionExtension;

@Extension
public class CompleteRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onCompleted(final Run<?, ?> run, @Nonnull final TaskListener listener) {
        VolunteerAction volunteerAction = run.getAction(VolunteerAction.class);
        if (volunteerAction != null) {
            List<DetectedIssue> issues = new LinkedList<DetectedIssue>();
            for (AttentionExtension extension : Jenkins.getInstance().getExtensionList(AttentionExtension.class)) {
                issues.addAll(extension.getIssues(run));
            }
            volunteerAction.processIssues(issues);
        }
    }
}
