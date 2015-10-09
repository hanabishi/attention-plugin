package org.jenkinsci.plugins.attention.pbe;

public class DetectedIssue {

    public static String DEFAULT_CATEGORY = "Unspecified";

    private String detectedInPlugin;
    private String errorHeader;
    private String errorMessage;
    private int failedSinceBuild = -1;
    private int failedCount = -1;

    public DetectedIssue() {
        detectedInPlugin = "Volunteer";
        errorHeader = DEFAULT_CATEGORY;
        errorMessage = "";
    }

    public DetectedIssue(String detectedInPlugin) {
        this.setDetectedInPlugin(detectedInPlugin);
    }

    public void setupTestFailure(String errorHeader, String errorMessage) {
        this.setErrorHeader(errorHeader);
        this.setErrorMessage(errorMessage);
    }

    public void setupTestFailure(String errorHeader, String errorMessage, int failedSinceBuild, int failedCount) {
        this.setFailedCount(failedCount);
        this.setFailedSinceBuild(failedSinceBuild);
        this.setErrorHeader(errorHeader);
        this.setErrorMessage(errorMessage);
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public String getDetectedInPlugin() {
        return detectedInPlugin;
    }

    public void setDetectedInPlugin(String detectedInPlugin) {
        this.detectedInPlugin = detectedInPlugin;
    }

    public String getErrorHeader() {
        return errorHeader;
    }

    public void setErrorHeader(String errorHeader) {
        this.errorHeader = errorHeader;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getFailedSinceBuild() {
        return failedSinceBuild;
    }

    public void setFailedSinceBuild(int failedSinceBuild) {
        this.failedSinceBuild = failedSinceBuild;
    }

}
