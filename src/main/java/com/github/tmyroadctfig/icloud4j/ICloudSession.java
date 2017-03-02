package com.github.tmyroadctfig.icloud4j;

import java.io.Serializable;
import java.util.Map;

import org.apache.http.client.CookieStore;

import com.github.tmyroadctfig.icloud4j.json.SerializableCookieStore;
import com.github.tmyroadctfig.icloud4j.util.ICloudUtils;

/**
 * Serializable iCloud session.
 * 
 * @author patchpump
 */
public class ICloudSession implements Serializable {

	public static final long MAX_DEFAULT_SESSION_AGE = 60000 * 5; // five mintues
	public static final long MAX_EXTENDED_SESSION_AGE = 86400000 * 60; // two months

	private static final long serialVersionUID = 1L;

	private SerializableCookieStore cookieStore = new SerializableCookieStore();
	private String clientId;
	private String dsid;
	private long createdAt;
	private Map<String, Object> dsInfoMap;
	private Map<String, Object> webServiceMap;
	private boolean hsaChallengeRequired;
	private boolean extendedLogin;

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
		return dsid != null && System.currentTimeMillis() - createdAt < getMaxSessionAge();
	}

	public boolean isExtendedLogin() {
		return extendedLogin;
	}

	public void setLoginInfo(Map<String, Object> loginInfo, boolean extendedLogin) {

		this.createdAt = System.currentTimeMillis();
		this.extendedLogin = extendedLogin;

		dsInfoMap = ICloudUtils.stringifyMap(loginInfo.get("dsInfo"));
		if(dsInfoMap == null)
			throw new ICloudException("iCloud authentication failed");

		webServiceMap = ICloudUtils.stringifyMap(loginInfo.get("webservices"));
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

	private long getMaxSessionAge() {
		return extendedLogin ? MAX_EXTENDED_SESSION_AGE : MAX_DEFAULT_SESSION_AGE;
	}

	@Override
	public String toString() {
		return "ICloudSession [clientId=" + clientId + ", dsid=" + dsid + ", createdAt=" + createdAt + ", cookies=" + cookieStore + "]";
	}
}
