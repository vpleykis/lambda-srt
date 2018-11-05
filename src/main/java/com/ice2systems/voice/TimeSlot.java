package com.ice2systems.voice;

import org.apache.commons.lang3.StringUtils;

public class TimeSlot {

	private int hour;
	private int minute;
	private int second;
	private int millisecond;
	
	private TimeSlot(int hour,int minute,int second, int millisecond) {
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.millisecond = millisecond;
	}
	
	public static TimeSlot build(String stringTimeSlot) { 
		
		if(StringUtils.isEmpty(stringTimeSlot)) {
			throw new IllegalArgumentException("stringTimeSlot can not be empty");
		}
		
		//Comparability between formats
		String str = stringTimeSlot.replace('.', ',').trim();
		
		String[] parts =  str.split(",");
		
		if(parts.length > 2) {
			throw new IllegalArgumentException("unsupported stringTimeSlot format: too many [.]");
		}
		
		int millisecond = (parts.length == 1) ? 0 : Integer.parseInt(parts[1]);
		
		String[] bigParts =  parts[0].split(":");
		
		if(bigParts.length != 3) {
			throw new IllegalArgumentException("unsupported stringTimeSlot format too many [:]");
		}
		
		int hour = Integer.parseInt(bigParts[0]);
		int minute = Integer.parseInt(bigParts[1]);
		int second = Integer.parseInt(bigParts[2]);
		
		return new TimeSlot(hour, minute, second, millisecond);
	}
	
	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getSecond() {
		return second;
	}

	public int getMillisecond() {
		return millisecond;
	}
	
}
