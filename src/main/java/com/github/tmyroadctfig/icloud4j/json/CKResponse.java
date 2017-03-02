package com.github.tmyroadctfig.icloud4j.json;

import java.util.List;

/**
 * CKDatabase response.
 * 
 * @author patchpump
 */
public class CKResponse {

	private List<CKRecord> records;

	public List<CKRecord> getRecords() {
		return records;
	}

	@Override
	public String toString() {
		return "CKDatabaseResponse [records=" + records + "]";
	}
}
