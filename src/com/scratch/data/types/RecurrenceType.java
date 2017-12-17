package com.scratch.data.types;

public enum RecurrenceType {
	
	NONE,
	DAILY,
	WEEKLY,
	BIWEEKLY,
	MONTHLY,
	SIX_MONTHS,
	YEARLY;
	
	public String toString() {
		String retVal = new String("");
		
		if (this.equals(NONE)) {
			retVal = "NONE";
		} else if (this.equals(DAILY)) {
			retVal = "DAILY";
		} else if (this.equals(WEEKLY)) {
			retVal = "WEEKLY";
		} else if (this.equals(BIWEEKLY)) {
			retVal = "BI-WEEKLY";
		} else if (this.equals(MONTHLY)) {
			retVal = "MONTHLY";
		} else if (this.equals(SIX_MONTHS)) {
			retVal = "SIX MONTHS";
		} else if (this.equals(YEARLY)) {
			retVal = "YEARLY";
		} else {
			retVal = "UNKNOWN";
		}
		
		return retVal;
	}
}
