package org.jenkinsci.plugins.attention.response;

import java.util.LinkedList;
import java.util.List;

import org.jenkinsci.plugins.attention.VolunteerCollection;

public class JavaScriptResponse extends BasicJavaScriptResponse {

    private List<VolunteerCollection> volunteerList = new LinkedList<VolunteerCollection>();

    public JavaScriptResponse(String message, boolean error, List<VolunteerCollection> voluntList) {
        super(message, error);
        this.setVolunteerList(voluntList);
    }

    public List<VolunteerCollection> getVolunteerList() {
        return volunteerList;
    }

    public void setVolunteerList(List<VolunteerCollection> volunteerList) {
        this.volunteerList = volunteerList;
    }

}
