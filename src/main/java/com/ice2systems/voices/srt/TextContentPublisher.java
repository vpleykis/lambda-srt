package com.ice2systems.voices.srt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.ice2systems.voice.Monolog;
import com.ice2systems.voice.TextContent;
import com.ice2systems.voice.TextLog;

public class TextContentPublisher {

	private static String bucketName = "<bucket here>";
	private static String topicArn = "arn:aws:sns:<region here>:<id here>:<name here>";
	
	private AmazonS3 s3;
	private AmazonSNS sns;
	private Context context = null;
	
	public TextContentPublisher(final AmazonS3 s3, final AmazonSNS sns) {
		this.s3 = s3;
		this.sns = sns;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public void publishContent(final TextContent content) {
		JSONObject voiceJObject = new JSONObject();
	// structured version of the refined SRT
		JSONObject subtitleJObject = new JSONObject(); 
		
		voiceJObject.put("name", content.name);
		subtitleJObject.put("name", content.name);
		
		JSONArray arrayVoice = new JSONArray();
		JSONArray arraySubtitles = new JSONArray();
		
		for(TextLog log: content.content) {
			JSONObject itemVoice = new JSONObject();
			JSONObject itemSubtitle = new JSONObject();
			
			itemVoice.put("id", log.getId());
			itemSubtitle.put("id", log.getId());
			
			JSONObject startTS = new JSONObject();
			
			startTS.put("h", log.getStartTime().getHour());
			startTS.put("m", log.getStartTime().getMinute());
			startTS.put("s", log.getStartTime().getSecond());
			startTS.put("ms", log.getStartTime().getMillisecond());

			itemVoice.put("startTS", startTS);
			itemSubtitle.put("startTS", startTS);
			
			JSONObject endTS = new JSONObject();

			endTS.put("h", log.getEndTime().getHour());
			endTS.put("m", log.getEndTime().getMinute());
			endTS.put("s", log.getEndTime().getSecond());
			endTS.put("ms", log.getEndTime().getMillisecond());
			
			itemVoice.put("endTS", endTS);
			itemSubtitle.put("endTS", endTS);
			
			JSONArray voiceMonologs = new JSONArray();
			JSONArray subtitleMonologs = new JSONArray();
			
			int i = 1;
			for(Monolog mono: log.getMonologs()) {
				String voiceName = String.format("%d_%d.pcm", log.getId(), i);
				voiceMonologs.add(voiceName);
				subtitleMonologs.add(mono.statement);
				text2voice(String.format("%s/%s",content.name, voiceName), mono.statement);
				i++;
			}
			
			itemVoice.put("voices", voiceMonologs);
			itemSubtitle.put("subtitles", subtitleMonologs);
			
			arrayVoice.add(itemVoice);
			arraySubtitles.add(itemSubtitle);
		}
		
		voiceJObject.put("content", arrayVoice);
		subtitleJObject.put("content", arraySubtitles);
		
		write2file(voiceJObject, content, "voice.ini");
		write2file(subtitleJObject, content, content.name+".json");		
	}
	
	public void write2file(final JSONObject jObject, final TextContent content, final String fileName) {
		byte[] bytes = jObject.toJSONString().getBytes(StandardCharsets.UTF_8);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(Long.valueOf(bytes.length));
		
		s3.putObject(new PutObjectRequest(bucketName, content.name+"/"+fileName, inputStream, metadata));		
	}
	
	private void text2voice(final String name, final String text) {
		PublishResult publishResult = sns.publish(new PublishRequest(topicArn, text, name));
		
		if(context!=null) {
			context.getLogger().log(String.format("MessageId=%s from item=%s", publishResult.getMessageId(), name));
		}
	}
}
