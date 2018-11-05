package com.ice2systems.voices.srt;

public class XmlData {

	public static enum XMLState{
		FULL, OPEN_TAG, CLOSE_TAG, NOT_XML
	}
	
	public String tag;
	public String content;
	public XMLState state;
	
	public XmlData(String tag, String content, XMLState state) {
		this.tag = tag;
		this.content = content;
		this.state = state;
	}
	
	public static XmlData notXML() {
		return new XmlData(null, null, XMLState.NOT_XML);
	}	
}
