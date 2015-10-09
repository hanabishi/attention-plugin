package org.jenkinsci.plugins.attention.response;

import java.util.LinkedList;
import java.util.List;

import org.jenkinsci.plugins.attention.buildfailure.ReportObject;

public class ReportJavaScriptResponse extends BasicJavaScriptResponse {

    private List<ReportObject> buildList = new LinkedList<ReportObject>();

    public ReportJavaScriptResponse(String message, boolean error, List<ReportObject> buildList) {
        super(message, error);
        this.setBuildList(buildList);
    }

    public List<ReportObject> getBuildList() {
        return buildList;
    }

    public void setBuildList(List<ReportObject> buildList) {
        this.buildList = buildList;
    }

}
