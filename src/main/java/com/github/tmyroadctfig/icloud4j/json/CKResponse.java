package com.github.tmyroadctfig.icloud4j.json;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * CKDatabase response.
 * 
 * @author patchpump
 */
public class CKResponse {

	private static final ImmutableList<CKRecord> EMPTY_LIST = ImmutableList.of();

	private List<CKRecord> records;

	public List<CKRecord> getRecords() {
		return records != null ? records : EMPTY_LIST;
	}

	@Override
	public String toString() {
		return "CKDatabaseResponse [records=" + records + "]";
	}
}
