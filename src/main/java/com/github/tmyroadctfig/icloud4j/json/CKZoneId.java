package com.github.tmyroadctfig.icloud4j.json;

/**
 * CKDatabase zoneID.
 * 
 * @author patchpump
 */
public class CKZoneId {

	private String zoneName;
	private String ownerRecordName;

	public String getZoneName() {
		return zoneName;
	}
	
	public String getOwnerRecordName() {
		return ownerRecordName;
	}
	
	@Override
	public String toString() {
		return "[CKZoneId:" + zoneName + "]";
	}
}
