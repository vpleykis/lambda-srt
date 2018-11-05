package com.ice2systems.voice.lambda_srt;

import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.ice2systems.email.EmailSender;
import com.ice2systems.voice.Monolog;
import com.ice2systems.voice.TextContent;
import com.ice2systems.voice.TextLog;
import com.ice2systems.voices.srt.SRTParser;
import com.ice2systems.voices.srt.TextContentPublisher;

public class SRTHandler implements RequestHandler<S3Event, Object> {

	private AmazonS3 s3;
	private AmazonSNS sns;
	private TextContentPublisher textContentPublisher;
	private EmailSender emailNotifier;
	private static String progressTopicArn = "arn:aws:sns:<region here>:<id here>:<name here>";
	
	public SRTHandler() {
		s3 = AmazonS3ClientBuilder.standard().build();
		sns = AmazonSNSClientBuilder.standard().build();
		textContentPublisher = new TextContentPublisher(s3, sns);
		emailNotifier = EmailSender.getInstance();
	}
	
	public Object handleRequest(S3Event event, Context context) {
		textContentPublisher.setContext(context);
		
    String bucket = event.getRecords().get(0).getS3().getBucket().getName();
    String key = event.getRecords().get(0).getS3().getObject().getKey();
    String username = null;
    
    context.getLogger().log(String.format("Key %s from bucket %s.", key, bucket));
    
    try {
    	String name = key.split(".srt")[0];
      
			S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
			
			Map<String, String> meta = response.getObjectMetadata().getUserMetadata();
			
			username = meta.get("username");
			
			context.getLogger().log(String.format("username: %s", username));
			
      emailNotifier.sendAccept(name, username);
      
			TextContent content = SRTParser.createContent(name, response.getObjectContent());
			
			if(content == null) {
				throw new RuntimeException("Unable to parse file content");
			}
			
			context.getLogger().log(String.format("size=%d.", content.content.size()));
			
			emailNotifier.sendParsed(name, content.content.size(), username);
			
			textContentPublisher.publishContent(content);
			
			emailNotifier.sendSuccess(username);

			startProgressTracking(context, name, username, content);
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			context.getLogger().log(String.format("Error processing object=%s from bucket=%s", key, bucket));
			emailNotifier.sendFailure(e.getMessage(), username);
		}
    
		return null;
	}
	
	@SuppressWarnings("unchecked")	
	private void startProgressTracking(Context context, final String name, final String username, final TextContent content) {
		JSONObject message = new JSONObject();
		message.put("username", username);
		message.put("list", getContentList(content));
		
		PublishResult publishResult =  sns.publish(new PublishRequest(progressTopicArn, message.toJSONString(), name));		
	
		context.getLogger().log(String.format("MessageId=%s from item=%s", publishResult.getMessageId(), name));
	}
	
	private String getContentList(final TextContent content) {
		StringBuffer buff = new StringBuffer();
		// no more than 9 monologs per textLog 
		// can be encoded
		// Example string for 1st textLog with 2 monologs 
		// and 2nd textLog with 3 monolog: "23" 
		for(TextLog tlog: content.content) {
			int count = 0;
			for(@SuppressWarnings("unused") Monolog monolog: tlog.getMonologs()) {
				count++;
			}
			buff.append(count);
		}
		
		return buff.toString();
	}
}
