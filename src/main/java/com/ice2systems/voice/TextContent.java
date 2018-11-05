package com.ice2systems.voice;

import java.util.LinkedList;
import java.util.List;

public class TextContent {
	public String name;
	public List<TextLog> content = new LinkedList<TextLog>();
	
	public TextContent(final String name, final List<TextLog> content) {
		this.name = name;
		
		if(content != null && !content.isEmpty()) {
			this.content.addAll(content);
		}
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		for(TextLog log: content) {
			buff.append(log.getId());
			buff.append("\n");
			for(Monolog mono: log.getMonologs()) {
				buff.append(mono.statement);
				buff.append("\n");
			}
		}
		
		return buff.toString();
	}	
	
}
