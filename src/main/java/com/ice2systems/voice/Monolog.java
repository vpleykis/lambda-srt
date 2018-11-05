package com.ice2systems.voice;

import org.apache.commons.lang3.StringUtils;

public class Monolog {
	public String statement;
	
	public Monolog(String statement) {
		if(StringUtils.isEmpty(statement)) {
			throw new IllegalArgumentException("statement can not be empty");
		}
		
		this.statement = statement;
	}
	
}
