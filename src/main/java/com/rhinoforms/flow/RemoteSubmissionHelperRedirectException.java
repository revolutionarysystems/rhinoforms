package com.rhinoforms.flow;

@SuppressWarnings("serial")
public class RemoteSubmissionHelperRedirectException extends RemoteSubmissionHelperException {
	
    private String redirectTarget;
    
	public RemoteSubmissionHelperRedirectException(String message, String redirectTarget) {
		super(message);
        this.redirectTarget = redirectTarget;
	}

    public String getRedirectTarget() {
        return redirectTarget;
    }

    public void setRedirectTarget(String redirectTarget) {
        this.redirectTarget = redirectTarget;
    }
	
}
