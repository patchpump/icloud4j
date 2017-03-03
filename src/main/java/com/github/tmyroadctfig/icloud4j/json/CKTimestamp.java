package com.github.tmyroadctfig.icloud4j.json;

/**
 * CKDatabase timestamp.
 * 
 * @author patchpump
 */
public class CKTimestamp {

	private long timestamp;
	private String userRecordName;
	private String deviceID;

	public long getTimestamp() {
		return timestamp;
	}
	
	public String getUserRecordName() {
		return userRecordName;
	}
	
	public String getDeviceID() {
		return deviceID;
	}

	@Override
	public String toString() {
		return "[CKTimestamp:" + timestamp + "]";
	}
}
