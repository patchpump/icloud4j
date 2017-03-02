package com.github.tmyroadctfig.icloud4j.json;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * CKDatabase record.
 * 
 * @author patchpump
 */
public class CKRecord {

	private String recordName;
	private String recordType;
	private String recordChangeTag;
	private CKTimestamp created;
	private CKTimestamp modified;
	private CKZoneId zoneID;

	private Map<String,CKRecordFieldValue> fields;

	public String getRecordName() {
		return recordName;
	}

	public String getRecordType() {
		return recordType;
	}

	public CKTimestamp getCreated() {
		return created;
	}

	public CKTimestamp getModified() {
		return modified;
	}

	public String getRecordChangeTag() {
		return recordChangeTag;
	}

	public CKZoneId getZoneID() {
		return zoneID;
	}

	public Map<String, CKRecordFieldValue> getFields() {
		return fields;
	}

	public String getString(String key) {
		return getString(key, "");
	}
	
	public Long getLong(String key) {
		return getLong(key, 0L);
	}

	public String getString(String key, String defaultValue) {

		if(fields == null)
			return defaultValue;
		
		CKRecordFieldValue v = fields.get(key);
		if(v == null || v.value == null)
			return defaultValue;
		
		try {
			if("STRING".equals(v.type))
				return (String)v.value;
			if("TIMESTAMP".equals(v.type) || "INT64".equals(v.type))
				return Long.toString((Long)v.value);
			else if("ENCRYPTED_BYTES".equals(v.type))
				return new String(Base64.decodeBase64((String)v.value), "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
		}

		return defaultValue;
	}
	
	public Long getLong(String key, Long defaultValue) {

		if(fields == null)
			return defaultValue;
		
		CKRecordFieldValue v = fields.get(key);
		if(v == null || v.value == null)
			return defaultValue;
		
		if("TIMESTAMP".equals(v.type) || "INT64".equals(v.type))
			return ((Double)v.value).longValue();
		else if("STRING".equals(v.type))
			return Long.parseLong((String)v.value);

		return defaultValue;
	}

	public byte[] getBytes(String key) {
		if(fields == null)
			return new byte[0];
		
		CKRecordFieldValue v = fields.get(key);
		if(v == null || v.value == null || !"ENCRYPTED_BYTES".equals(v.type))
			return new byte[0];
		
		return Base64.decodeBase64((String)v.value);
	}

	@Override
	public String toString() {
		return "CKDatabaseRecord [recordName=" + recordName + ", recordType=" + recordType + ", created=" + created
			+ ", modified=" + modified + ", recordChangeTag=" + recordChangeTag + ", zoneID=" + zoneID + ", fields="
			+ fields + "]";
	}
	
	public static class CKRecordFieldValue {

		public Object value;
		public String type;
	}
}
