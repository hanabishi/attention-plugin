package org.jenkinsci.plugins.attention.response;


public class BasicJavaScriptResponse {

    private boolean error = false;
    private String message = "";

    public BasicJavaScriptResponse(String message, boolean error) {
        this.setMessage(message);
        this.setError(error);
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
