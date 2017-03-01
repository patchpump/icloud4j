package com.github.tmyroadctfig.icloud4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

/**
 * Serializable iCloud session.
 * 
 * @author patchpump
 */
public class ICloudSession implements Serializable {

	public static final long MAX_SESSION_AGE = 86400000 * 60; // two months?

	private static final long serialVersionUID = 1L;

	private final BasicCookieStore cookieStore = new BasicCookieStore();
	private String clientId;
	private String dsid;
	private long createdAt;
	private Map<String, Object> dsInfoMap;
	private Map<String, Object> webServiceMap;
	private boolean hsaChallengeRequired;

	public ICloudSession() {
	}

	public ICloudSession(String clientId) {
		this.clientId = clientId;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public String getSessionId() {
		return dsid;
	}

	public Long getCreatedAt() {
		return createdAt;
	}
	
	public String getClientId() {
		return clientId;
	}

	public boolean isValid() {
		return dsid != null && System.currentTimeMillis() - createdAt < MAX_SESSION_AGE;
	}

	public void setLoginInfo(Map<String, Object> loginInfo) {

		this.createdAt = System.currentTimeMillis();

		dsInfoMap = stringifyMap(loginInfo.get("dsInfo"));
		if(dsInfoMap == null)
			throw new ICloudException("iCloud authentication failed");

		webServiceMap = stringifyMap(loginInfo.get("webservices"));
		if(webServiceMap == null)
			throw new ICloudException("iCloud authentication failed");

		dsid = (String)dsInfoMap.get("dsid");
		hsaChallengeRequired = Boolean.TRUE.equals(loginInfo.get("hsaChallengeRequired"));
	}
	
	public boolean isHsaChallengeRequired() {
		return hsaChallengeRequired;
	}

	public Map<String, Object> getWebServicesMap() {
		return webServiceMap;
	}

	public Map<String, Object> getDsInfoMap() {
		return dsInfoMap;
	}

	private Map<String,Object> stringifyMap(Object map) {

		if(map == null)
			return null;

		@SuppressWarnings("unchecked")
		Map<String,Object> m = (Map<String,Object>)map;
		Map<String,Object> t = new HashMap<String,Object>();
		for(Entry<String, Object> entry : m.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(value instanceof String)
				t.put(key, (String)value);
			else if(value instanceof Map)
				t.put(key, stringifyMap(value));
			else 
				t.put(key, value.toString());
		}
		return t;
	}

	@Override
	public String toString() {
		return "ICloudSession [clientId=" + clientId + ", dsid=" + dsid + ", createdAt=" + createdAt + "]";
	}
}
