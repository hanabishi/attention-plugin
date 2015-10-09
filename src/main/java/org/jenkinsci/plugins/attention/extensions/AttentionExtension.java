package org.jenkinsci.plugins.attention.extensions;

import hudson.ExtensionPoint;
import hudson.model.Run;

import java.util.List;

import org.jenkinsci.plugins.attention.pbe.DetectedIssue;

public abstract class AttentionExtension implements ExtensionPoint {

    public abstract List<DetectedIssue> getIssues(Run<?, ?> run);

}
