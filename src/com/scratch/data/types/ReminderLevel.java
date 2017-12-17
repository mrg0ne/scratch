package com.scratch.data.types;

public enum ReminderLevel {
	OFF,
	LOW,
	MED,
	HIGH;
	
	public String toString() {
		String retVal = new String("");
		
		if (this.equals(OFF)) {
			retVal = "OFF";
		} else if (this.equals(LOW)) {
			retVal = "LOW";
		} else if (this.equals(MED)) {
			retVal = "MED";
		} else if (this.equals(HIGH)) {
			retVal = "HIGH";
		} else {
			retVal = "UNKNOWN";
		}
		
		return retVal;
	}
}
