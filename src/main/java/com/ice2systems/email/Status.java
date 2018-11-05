package com.ice2systems.email;

public enum Status {
	ACCEPTED("Accepted for processing: %s",
					 "Accepted for processing: %s"),

	PARSED("Parser file: %s<p/>dialog count: %d",
			 	 "Parser file: %s\ndialog count: %d"),
	
	SUCCESS("Wait a few minutes for text-to-voice process to finish<br/>You will receive another email informing about the process result<p/>-Thanks!",
					"Wait a few minutes for text-to-voice process to finish. You will receive another email informing about the process result. -Thanks!"), 

	VOICE_READY("Voice content is ready for %s",
		 	 				"Voice content is ready for %s"),
	
	FAILURE("Something went wrong:<p/>%s",
					"Something went wrong:\n%s");
	
	private final String html;
	private final String text;
	
	Status(final String html, final String text) {
		this.html = html;
		this.text = text;
	}
	
	public String getHtml() {
		return this.html;
	}
	
	public String getText() {
		return this.text;
	}	
}