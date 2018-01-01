package com.scratch.data.types;

public enum Operation {
	ADD,
	UPDATE,
	REMOVE,
	SET_TASK_COMPLETE;
	
	public String toString() {
		String retVal = new String("");
		
		if (this.equals(ADD)) {
			retVal = "ADD";
		} else if (this.equals(UPDATE)) {
			retVal = "UPDATE";
		} else if (this.equals(REMOVE)) {
			retVal = "REMOVE";
		} else if (this.equals(SET_TASK_COMPLETE)){
			retVal = "SET_TASK_COMPLETE";
		} else {
			retVal = "UNKNOWN";
		}
		
		return retVal;
	}
}
