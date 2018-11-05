package com.ice2systems.voices.srt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ice2systems.voice.Monolog;
import com.ice2systems.voice.TextContent;
import com.ice2systems.voice.TextLog;
import com.ice2systems.voice.TextLogFormat;
import com.ice2systems.voice.TimeSlot;

public class SRTParser {

	private SRTParser() {}

	private final static int UTF8_BOM_LENGTH = 3;
	
  private static boolean hasBOM(final byte[] bytes) {
    if ((bytes[0] & 0xFF) == 0xEF && 
        (bytes[1] & 0xFF) == 0xBB && 
        (bytes[2] & 0xFF) == 0xBF) {
        return true;
    }
    return false;
  }
  
  private static List<String> stream2lines(final InputStream in) throws IOException {
  	BufferedReader br = null;
		List<String> content = new LinkedList<String>();
		
		try {
			br = new BufferedReader(new InputStreamReader(in));
			String line;			
			int i = 0;
			
			while ((line = br.readLine()) != null) {
				line = StringUtils.trim(line).trim().replace("\n", "").replace("\r", "");
				
				if(!line.isEmpty() && !(line.startsWith("-") && line.length() == 1 )) {
					if(i==0) {
						final byte[] bytes = line.getBytes("UTF-8");
						
						if (hasBOM(bytes)) {
			        int length = bytes.length - UTF8_BOM_LENGTH;
			        byte[] barray = new byte[length];
			        System.arraycopy(bytes, UTF8_BOM_LENGTH, barray, 0, barray.length);
			        line = new String(barray, "UTF-8");
		        }
					}
					content.add(line);
				}
				i++;
			}
		} catch (IOException e) {
			throw e;
		}	
		finally {
			if(br != null) {
				br.close();
			}
		}
		return content;		
	}
	
	private static TextContent createRawContent(final String name, final InputStream in) throws IOException {
		final List<String> lines = stream2lines(in);
		
		List<TextLog> content = new LinkedList<TextLog>();
		
		TextLog.Builder currentBuilder = null;
		TextLogFormat currentLineType = null;
		
		int i = 0;
		
		for(String line: lines) {
			i++;
			
			if(line.isEmpty()) {
				continue;
			}
			
			//System.out.println(String.format("line=[%s] size=%d",line, line.length()));
			
			if(StringUtils.isNumeric(line)) {// BLOCK start			
				
				if( currentLineType == null ) {// first ever BLOCK	
					currentBuilder = new TextLog.Builder().id(Integer.parseInt(line));
				}
				else if (currentLineType == TextLogFormat.TEXT){// just another BLOCK 
					addTextLog(content, currentBuilder, i);
					//init a new builder
					currentBuilder = new TextLog.Builder().id(Integer.parseInt(line));			
				}			
				else {// format problem or text is not provided, which we can recover from
					if (currentLineType == TextLogFormat.TIMESLOT) {
					// consider empty text, so we try to produce silence for now (FIXME)
						currentLineType = TextLogFormat.TEXT;
						currentBuilder.monolog(new Monolog("..."));
						addTextLog(content, currentBuilder, i);
						currentBuilder = new TextLog.Builder().id(Integer.parseInt(line));	
					}
					else {
						throw new RuntimeException(String.format("SRT format problem-1 at line #%d: %s", i, line));
					}
				}

				currentLineType = TextLogFormat.ID;
			}
			else if (currentLineType == TextLogFormat.ID && line.contains("-->")) {//time slots
				currentLineType = TextLogFormat.TIMESLOT;
				
				String[] parts = line.split("-->");
				if(parts.length != 2) {
					throw new IllegalArgumentException(String.format("unsupported stringTimeSlot format at line #%d", i));
				}	
				
				currentBuilder.startTime(TimeSlot.build(parts[0])).endTime(TimeSlot.build(parts[1]));
			}
			else if (currentLineType == TextLogFormat.TIMESLOT || currentLineType == TextLogFormat.TEXT ) {
				// TEXT after TIMESLOT or TEXT after TEXT
				currentLineType = TextLogFormat.TEXT;
				
				currentBuilder.monolog(new Monolog(line));
			}
			else {
				throw new IllegalArgumentException(String.format("SRT format problem-2 at line #%d: %s", i, line));
			}			
		}
		
		addTextLog(content, currentBuilder, i);
			
		return new TextContent(name, content);
	}
	
	private static void addTextLog(List<TextLog> content, TextLog.Builder builder, int i) {
		TextLog tlog = builder.build();
		if(!tlog.isComplete()) {
			throw new RuntimeException(String.format("Incomplete block at line #%d", i));
		}
		//add BLOCK to content
		content.add(tlog);		
	}
	
	public static TextContent createContent(final String name, final InputStream in) throws IOException {
		TextContent content = createRawContent(name, in);
		
		List<TextLog> list = content.content;
		
		for(TextLog textLog: list) {
			rectifyTextLog(textLog);
		}
		
		return content;
	}
	
	private static void rectifyTextLog(final TextLog textLog) {
		List<Monolog> originalList = textLog.getMonologs();
		List<Monolog> rectifiedList = new LinkedList<Monolog>();
		
		StringBuffer buf = null;
		
		for(Monolog originalMonolog: originalList) {
			//System.out.println(originalMonolog.statement);
			
			if ( originalMonolog.statement.contains("<") && originalMonolog.statement.contains(">") ) {
				XmlData data = XmlHelper.getXMLData(purifyString(originalMonolog.statement));
				
				switch(data.state) {
				case FULL:
					if (buf == null) {
						rectifiedList.add(new Monolog(purifyString(data.content)));
					}
					else {
						throw new IllegalStateException(String.format("SRT content is broken at : [%s]",originalMonolog.statement));
					}
					break;
				case OPEN_TAG:
					if (buf == null) {
						buf = new StringBuffer(purifyString(data.content));
					}
					else {
						throw new IllegalStateException(String.format("SRT content is broken at : [%s]",originalMonolog.statement));
					}					
					break;		
				case CLOSE_TAG:
					if (buf != null) {
						buf.append(" ");
						buf.append(purifyString(data.content));
						rectifiedList.add(new Monolog(purifyString(buf.toString())));
						buf = null;
					}
					else {
						throw new IllegalStateException(String.format("SRT content is broken at : [%s]",originalMonolog.statement));
					}					
					break;		
				default:
					
				}
			}
			else {
				if (buf == null) {
					rectifiedList.add(new Monolog(purifyString(originalMonolog.statement)));
				}
				else {
					buf.append(" ");					
					buf.append(purifyString(originalMonolog.statement));
				}
			}
		}
		
		//finished, but tags not closed properly
		if (buf != null) {
			rectifiedList.add(new Monolog(purifyString(buf.toString())));
			buf = null;
		}
		
		textLog.setMonologs(rectifiedList);
	}
	
	private static String purifyString(String str) {
		return StringUtils.stripStart(str, "-").trim();
	}
}
