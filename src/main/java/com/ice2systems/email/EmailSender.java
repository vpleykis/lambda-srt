package com.ice2systems.email;

import org.apache.commons.lang3.StringUtils;

//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest; 

public class EmailSender {
	private final String SUBJECT = "Text processing status: %s";
	
	private static final String FROM = "<email address here>";
	private static final String TO = "<email address here>";
	
	private static AmazonSimpleEmailService client;
	
	private static EmailSender instance;
	
	private EmailSender() {
    client = 
        AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}
	
	public static EmailSender getInstance() {
    if (instance == null) {
        instance = new EmailSender();
    }
    return instance;
	}
	
	public boolean send(final String subject, final String to, final String bodyHtml, final String bodyTxt) {
		try {
			if(StringUtils.isEmpty(to) || !to.contains("@")) {
				throw new RuntimeException("destination email is malformed");
			}
			
      SendEmailRequest request = new SendEmailRequest()
          .withDestination(
              new Destination().withToAddresses(to))
          .withMessage(new Message()
              .withBody(new Body()
                  .withHtml(new Content()
                      .withCharset("UTF-8").withData(bodyHtml))
                  .withText(new Content()
                      .withCharset("UTF-8").withData(bodyTxt)))
              .withSubject(new Content()
                  .withCharset("UTF-8").withData(subject)))
          .withSource(FROM);
      client.sendEmail(request);

      return true;
    } catch (Exception ex) {
      return false;
    }
	}
	
	public boolean sendSuccess(final String to) {
		return send(
				String.format(SUBJECT,Status.SUCCESS.name()), 
				StringUtils.isEmpty(to) ? TO : to, 
  			Status.SUCCESS.getHtml(), 
  			Status.SUCCESS.getText()
  			);
	}
	
	public boolean sendFailure(final String error, final String to) {
		return send(
				String.format(SUBJECT,Status.FAILURE.name()), 
				StringUtils.isEmpty(to) ? TO : to, 
				String.format(Status.FAILURE.getHtml(),error), 
				String.format(Status.FAILURE.getText(),error)
  			);
	}	
	
	public boolean sendAccept(final String fileName, final String to) {
		return send(
				String.format(SUBJECT,Status.ACCEPTED.name()), 
				StringUtils.isEmpty(to) ? TO : to, 
				String.format(Status.ACCEPTED.getHtml(),fileName), 
				String.format(Status.ACCEPTED.getText(),fileName)
  			);
	}		
	
	public boolean sendParsed(final String fileName, final int size, final String to) {
		return send(
				String.format(SUBJECT,Status.PARSED.name()), 
				StringUtils.isEmpty(to) ? TO : to, 
				String.format(Status.PARSED.getHtml(),fileName, size), 
				String.format(Status.PARSED.getText(),fileName, size)
  			);
	}	
	
	public boolean sendVoiceReady(final String fileName, final String to) {
		return send(
				String.format(SUBJECT,Status.VOICE_READY.name()), 
				StringUtils.isEmpty(to) ? TO : to, 
				String.format(Status.VOICE_READY.getHtml(),fileName), 
				String.format(Status.VOICE_READY.getText(),fileName)
  			);
	}		
}
