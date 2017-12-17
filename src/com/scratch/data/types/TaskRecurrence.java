package com.scratch.data.types;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskRecurrence implements Parcelable {
	
	// The Recurrence type
	private RecurrenceType mType;
	
	// The occurrence period
	private int mOccurenceRegularity;
	
	// The day of month that this task occurs on
	private int mDayOfMonth;
	
	// True if this task occurs on Monday
	private boolean mOnMonday;
	
	// True if this task occurs on Tuesday
	private boolean mOnTuesday;
	
	// True if this task occurs on Wednesday
	private boolean mOnWednesday;
	
	// True if this task occurs on Thursday
	private boolean mOnThursday;
	
	// True if this task occurs on Friday
	private boolean mOnFriday;
	
	// True if this task occurs on Saturday
	private boolean mOnSaturday;
	
	// True if this task occurs on Sunday
	private boolean mOnSunday;
	
	// Week of the month
	public enum WeekNumber {NONE, FIRST, SECOND, THIRD, FOURTH, LAST};
	
	// Week of the month
	private WeekNumber mWeekNum;
	
	// The month of the year
	private int mMonth;
	
	// True if the day of the month/year should be used, False if the selected days 
	// of the week should be used
	private boolean mUseDay;
	
	// The time of day that this occurs
	private long mTimeOfDay;
	
	public TaskRecurrence() {
		mType = RecurrenceType.NONE;
		mOccurenceRegularity = 1;
		mDayOfMonth = 1;
		mOnMonday = false;
		mOnTuesday = false;
		mOnWednesday = false;
		mOnThursday = false;
		mOnFriday = false;
		mOnSaturday = false;
		mOnSunday = false;
		mWeekNum = WeekNumber.NONE;
		mMonth = 1;
		mUseDay = true;
		long mTimeOfDay = 0;
	}
	
	/**
	 * @param pType
	 * @param mOccurenceRegularity
	 * @param mDayOfMonth
	 * @param mOnMonday
	 * @param mOnTuesday
	 * @param mOnWednesday
	 * @param mOnThursday
	 * @param mOnFriday
	 * @param mOnSaturday
	 * @param mOnSunday
	 * @param mWeekNum
	 * @param mMonth
	 * @param mUseDay
	 * @param mTimeOfDay
	 */
	public TaskRecurrence(RecurrenceType pType, int pOccurenceRegularity, 
			int pDayOfMonth,
			boolean pOnMonday, boolean pOnTuesday, boolean pOnWednesday,
			boolean pOnThursday, boolean pOnFriday, boolean pOnSaturday,
			boolean pOnSunday, WeekNumber pWeekNum, int pMonth,
			boolean pUseDayOfMonth, long pTimeOfDay) {
		super();
		this.mType = pType;
		this.mOccurenceRegularity = pOccurenceRegularity;
		this.mDayOfMonth = pDayOfMonth;
		this.mOnMonday = pOnMonday;
		this.mOnTuesday = pOnTuesday;
		this.mOnWednesday = pOnWednesday;
		this.mOnThursday = pOnThursday;
		this.mOnFriday = pOnFriday;
		this.mOnSaturday = pOnSaturday;
		this.mOnSunday = pOnSunday;
		this.mWeekNum = pWeekNum;
		this.mMonth = pMonth;
		this.mUseDay = pUseDayOfMonth;
		this.mTimeOfDay = pTimeOfDay;
	}

	protected TaskRecurrence(Parcel pIn){
		mType = RecurrenceType.valueOf(pIn.readString());
		mOccurenceRegularity = pIn.readInt();
		mDayOfMonth = pIn.readInt();
		mOnMonday = Boolean.valueOf(pIn.readString());
		mOnTuesday = Boolean.valueOf(pIn.readString());
		mOnWednesday = Boolean.valueOf(pIn.readString());
		mOnThursday = Boolean.valueOf(pIn.readString());
		mOnFriday = Boolean.valueOf(pIn.readString());
		mOnSaturday = Boolean.valueOf(pIn.readString());
		mOnSunday = Boolean.valueOf(pIn.readString());
		mWeekNum = WeekNumber.valueOf(pIn.readString());
		mMonth = pIn.readInt();
		mUseDay = Boolean.valueOf(pIn.readString());
		mTimeOfDay = pIn.readLong();
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel pDest, int pArg1) {
		pDest.writeString(mType.name());
		pDest.writeInt(mOccurenceRegularity);
		pDest.writeInt(mDayOfMonth);
		pDest.writeString(Boolean.toString(mOnMonday));
		pDest.writeString(Boolean.toString(mOnTuesday));
		pDest.writeString(Boolean.toString(mOnWednesday));
		pDest.writeString(Boolean.toString(mOnThursday));
		pDest.writeString(Boolean.toString(mOnFriday));
		pDest.writeString(Boolean.toString(mOnSaturday));
		pDest.writeString(Boolean.toString(mOnSunday));
		pDest.writeString(mWeekNum.toString());
		pDest.writeInt(mMonth);
		pDest.writeString(Boolean.toString(mUseDay));
		pDest.writeLong(mTimeOfDay);
	}
	
	/**
	 * 
	 * @return the recurrence type
	 */
	public RecurrenceType getRecurrenceType() {
		return mType;
	}

	/**
	 * Set the recurrence type
	 * @param pType
	 */
	public void setRecurrenceType(RecurrenceType pType){
		mType = pType;
	}
	
	/**
	 * @return the mOccurenceRegularity
	 */
	public int getOccurenceRegularity() {
		return mOccurenceRegularity;
	}

	/**
	 * @param mOccurenceRegularity the mOccurenceRegularity to set
	 */
	public void setOccurenceRegularity(int pOccurenceRegularity) {
		this.mOccurenceRegularity = pOccurenceRegularity;
	}

	/**
	 * @return the mDayOfMonth
	 */
	public int getDayOfMonth() {
		return mDayOfMonth;
	}

	/**
	 * @param mDayOfMonth the mDayOfMonth to set
	 */
	public void setDayOfMonth(int pDayOfMonth) {
		this.mDayOfMonth = pDayOfMonth;
	}

	/**
	 * @return the mOnMonday
	 */
	public boolean isOnMonday() {
		return mOnMonday;
	}

	/**
	 * @param mOnMonday the mOnMonday to set
	 */
	public void setOnMonday(boolean pOnMonday) {
		this.mOnMonday = pOnMonday;
	}

	/**
	 * @return the mOnTuesday
	 */
	public boolean isOnTuesday() {
		return mOnTuesday;
	}

	/**
	 * @param mOnTuesday the mOnTuesday to set
	 */
	public void setOnTuesday(boolean pOnTuesday) {
		this.mOnTuesday = pOnTuesday;
	}

	/**
	 * @return the mOnWednesday
	 */
	public boolean isOnWednesday() {
		return mOnWednesday;
	}

	/**
	 * @param mOnWednesday the mOnWednesday to set
	 */
	public void setOnWednesday(boolean pOnWednesday) {
		this.mOnWednesday = pOnWednesday;
	}

	/**
	 * @return the mOnThursday
	 */
	public boolean isOnThursday() {
		return mOnThursday;
	}

	/**
	 * @param mOnThursday the mOnThursday to set
	 */
	public void setOnThursday(boolean pOnThursday) {
		this.mOnThursday = pOnThursday;
	}

	/**
	 * @return the mOnFriday
	 */
	public boolean isOnFriday() {
		return mOnFriday;
	}

	/**
	 * @param pOnFriday the mOnFriday to set
	 */
	public void setOnFriday(boolean pOnFriday) {
		this.mOnFriday = pOnFriday;
	}

	/**
	 * @return the mOnSaturday
	 */
	public boolean isOnSaturday() {
		return mOnSaturday;
	}

	/**
	 * @param mOnSaturday the mOnSaturday to set
	 */
	public void setOnSaturday(boolean pOnSaturday) {
		this.mOnSaturday = pOnSaturday;
	}

	/**
	 * @return the mOnSunday
	 */
	public boolean isOnSunday() {
		return mOnSunday;
	}

	/**
	 * @param mOnSunday the mOnSunday to set
	 */
	public void setOnSunday(boolean pOnSunday) {
		this.mOnSunday = pOnSunday;
	}

	/**
	 * @return the mWeekNum
	 */
	public WeekNumber getWeekNum() {
		return mWeekNum;
	}

	/**
	 * @param mWeekNum the mWeekNum to set
	 */
	public void setWeekNum(WeekNumber pWeekNum) {
		this.mWeekNum = pWeekNum;
	}

	/**
	 * @return the mMonth
	 */
	public int getMonth() {
		return mMonth;
	}

	/**
	 * @param mMonth the mMonth to set
	 */
	public void setMonth(int pMonth) {
		this.mMonth = pMonth;
	}

	/**
	 * @return the mUseDay
	 */
	public boolean isUseDay() {
		return mUseDay;
	}

	/**
	 * @param mUseDay the mUseDay to set
	 */
	public void setUseDay(boolean pUseDay) {
		this.mUseDay = pUseDay;
	}

	/**
	 * @return the mTimeOfDay
	 */
	public long getTimeOfDay() {
		return mTimeOfDay;
	}

	/**
	 * @param mTimeOfDay the mTimeOfDay to set
	 */
	public void setTimeOfDay(long pTimeOfDay) {
		this.mTimeOfDay = pTimeOfDay;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskRecurrence [mType=" + mType.name() + ", mOccurenceRegularity=" 
	            + mOccurenceRegularity
				+ ", mDayOfMonth=" + mDayOfMonth + ", mOnMonday=" + mOnMonday
				+ ", mOnTuesday=" + mOnTuesday + ", mOnWednesday="
				+ mOnWednesday + ", mOnThursday=" + mOnThursday
				+ ", mOnFriday=" + mOnFriday + ", mOnSaturday=" + mOnSaturday
				+ ", mOnSunday=" + mOnSunday + ", mWeekNum=" + mWeekNum
				+ ", mMonth=" + mMonth + ", mUseDay=" + mUseDay
				+ ", mTimeOfDay=" + mTimeOfDay + "]";
	}

}
