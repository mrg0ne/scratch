package com.scratch.data.types;

public enum SortOrder {
	DUE_DATE,
	NAME,
	COMPLETE_DATE;
	
	public String toString() {
		return this.name();
	}

}
