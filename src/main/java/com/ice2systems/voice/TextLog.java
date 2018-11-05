package com.ice2systems.voice;

import java.util.LinkedList;
import java.util.List;

public class TextLog {
	private int id = -1;
	private TimeSlot startTime;
	private TimeSlot endTime;
	private List<Monolog> monologs = new LinkedList<Monolog>();
	
  public static class Builder {
	  private 	int id;
	  private TimeSlot startTime;
	  private TimeSlot endTime;
	  private List<Monolog> monologs = new LinkedList<Monolog>();

    public Builder id(int id) {
        this.id = id;
        return this;
    }

    public Builder startTime(TimeSlot startTime) {
        this.startTime = startTime;
        return this;
    }

    public Builder endTime(TimeSlot endTime) {
        this.endTime = endTime;
        return this;
    }

    public Builder monolog(Monolog monolog) {
	  		if(monolog != null) {
	  			this.monologs.add(monolog);
	  		}
      return this;
    }
    
    public TextLog build() {
        return new TextLog(this);
    }
  }

	private TextLog(Builder b) {
		this.id = b.id;
		this.startTime = b.startTime;
		this.endTime = b.endTime;
		this.monologs = b.monologs;
	}

	public int getId() {
		return id;
	}
	
	public TimeSlot getStartTime() {
		return startTime;
	}
	
	public TimeSlot getEndTime() {
		return endTime;
	}
	
	public List<Monolog> getMonologs() {
		return monologs;
	}	
	
	//used to adjust monologs
	public void setMonologs(List<Monolog> newMonologs) {
		monologs = newMonologs;
	}
	
	public boolean isComplete() {
		return id > 0 && startTime != null && endTime != null && monologs != null && !monologs.isEmpty();
	}
}
