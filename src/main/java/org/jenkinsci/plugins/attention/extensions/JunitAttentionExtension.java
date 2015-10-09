package org.jenkinsci.plugins.attention.extensions;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.CaseResult;

import java.util.LinkedList;
import java.util.List;

import org.jenkinsci.plugins.attention.pbe.DetectedIssue;

@Extension
public class JunitAttentionExtension extends AttentionExtension {

    @Override
    public List<DetectedIssue> getIssues(Run<?, ?> run) {
        List<TestResultAction> actions = run.getActions(TestResultAction.class);
        List<DetectedIssue> issues = new LinkedList<DetectedIssue>();

        for (TestResultAction action : actions) {
            List<CaseResult> failedTests = action.getFailedTests();
            for (CaseResult result : failedTests) {
                DetectedIssue issue = new DetectedIssue(TestResultAction.class.getName());
                issue.setupTestFailure(result.getTitle(), result.getErrorDetails(), result.getFailedSince(),
                        result.getFailCount());
                issues.add(issue);
            }
        }

        return issues;
    }

}
