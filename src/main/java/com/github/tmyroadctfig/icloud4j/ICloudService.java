/*
 *    Copyright 2016 Luke Quinane
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.tmyroadctfig.icloud4j;

import com.github.tmyroadctfig.icloud4j.json.TrustedDevice;
import com.github.tmyroadctfig.icloud4j.json.TrustedDeviceResponse;
import com.github.tmyroadctfig.icloud4j.json.TrustedDevices;
import com.github.tmyroadctfig.icloud4j.util.ICloudUtils;
import com.github.tmyroadctfig.icloud4j.util.JsonToMapResponseHandler;
import com.github.tmyroadctfig.icloud4j.util.StringResponseHandler;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The iCloud service.
 *
 * @author Luke Quinane
 */
public class ICloudService implements java.io.Closeable {
	/**
	 * A flag indicating whether to disable SSL checks.
	 */
	private static final boolean DISABLE_SSL_CHECKS = Boolean
		.parseBoolean(System.getProperty("tmyroadctfig.icloud4j.disableSslChecks", "false"));

	/**
	 * The proxy host to use.
	 */
	private static final String PROXY_HOST = System.getProperty("http.proxyHost");

	/**
	 * The proxy port to use.
	 */
	private static final Integer PROXY_PORT = Integer.getInteger("http.proxyPort");

	/**
	 * The end point.
	 */
	public static final String endPoint = "https://www.icloud.com";

	/**
	 * The setup end point.
	 */
	public static final String setupEndPoint = "https://setup.icloud.com/setup/ws/1";

	/**
	 * The HTTP client.
	 */
	private final CloseableHttpClient httpClient;

	/**
	 * The idmsa service.
	 */
	private final IdmsaService idmsaService;

	/**
	 * The iCloud session.
	 */
	private final ICloudSession session;

	/**
	 * Creates a new iCloud service instance
	 *
	 * @param clientId the client ID.
	 */
	public ICloudService(String clientId) {
		this(new ICloudSession(clientId));
	}

	/**
	 * Creates a new iCloud service instance
	 *
	 * @param clientId the client ID.
	 */
	@SuppressWarnings("deprecation")
	public ICloudService(ICloudSession session) {
		this.session = session;
		try {
			HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultCookieStore(getCookieStore());

			if (!Strings.isNullOrEmpty(PROXY_HOST)) {
				clientBuilder.setProxy(new HttpHost(PROXY_HOST, PROXY_PORT));
			}

			if (DISABLE_SSL_CHECKS) {
				clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, (x509CertChain, authType) -> true).build());
			}

			httpClient = clientBuilder.build();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		idmsaService = new IdmsaService(this);
	}

	/**
	 * Attempts to log in to iCloud.
	 *
	 * @param username the username.
	 * @param password the password.
	 * @return the map of values returned by iCloud.
	 */
	public Map<String, Object> authenticate(String username, char[] password) {
		Map<String, Object> params = ImmutableMap.of("apple_id", username, "password", new String(password), "extended_login", true);
		return authenticate(params);
	}

	/**
	 * Attempts to log in to iCloud.
	 *
	 * @param params the map of parameters to pass to login.
	 * @return the map of values returned by iCloud.
	 */
	public Map<String, Object> authenticate(Map<String, Object> params) {
		try {
			URIBuilder uriBuilder = new URIBuilder(setupEndPoint + "/login");
			populateUriParameters(uriBuilder);
			URI uri = uriBuilder.build();

			HttpPost post = new HttpPost(uri);
			post.setEntity(new StringEntity(ICloudUtils.toJson(params), Consts.UTF_8));
			populateRequestHeadersParameters(post);

			try (CloseableHttpResponse response = httpClient.execute(post)) {
				Map<String, Object> result = new JsonToMapResponseHandler().handleResponse(response);
				Object error = result.get("error");
				if (error != null)
					throw new RuntimeException("failed to log into iCloud: " + result.get("error"));
				session.setLoginInfo(result);
				return result;
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Checks whether two-factor authentication is enabled for this account.
	 *
	 * @return {@code true} if two-factor authentication is enabled.
	 */
	public boolean isTwoFactorEnabled() {
		return session.isHsaChallengeRequired();
	}

	/**
	 * Gets the trusted two-factor authentication devices for the current account.
	 *
	 * @return the list of trusted devices.
	 */
	public List<TrustedDevice> getTrustedDevices() {
		try {
			URIBuilder uriBuilder = new URIBuilder(setupEndPoint + "/listDevices");
			populateUriParameters(uriBuilder);
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			populateRequestHeadersParameters(httpGet);

			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				TrustedDevices trustedDevices = ICloudUtils.fromJson(new StringResponseHandler().handleResponse(response), TrustedDevices.class);
				return Arrays.asList(trustedDevices.devices);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * <p>Requests that a two-factor verification code be sent to the given trusted device. The value sent to the device
	 * should be submitted to {@link #validateManualVerificationCode(TrustedDevice, String, char[])} for verification.</p>
	 *
	 * <p>Note: newer devices will automatically display a verification code without manually requesting one, and that
	 *  must be submitted via {@link }.</p>
	 *
	 * @param device the device to send the verification code to.
	 */
	public void sendManualVerificationCode(TrustedDevice device) {
		try {
			URIBuilder uriBuilder = new URIBuilder(setupEndPoint + "/sendVerificationCode");
			populateUriParameters(uriBuilder);
			URI uri = uriBuilder.build();

			HttpPost post = new HttpPost(uri);
			post.setEntity(new StringEntity(ICloudUtils.toJson(device), Consts.UTF_8.name()));
			populateRequestHeadersParameters(post);

			Map<String, Object> response = httpClient.execute(post, new JsonToMapResponseHandler());

			if (!Boolean.TRUE.equals(response.get("success"))) {
				throw new IllegalStateException("Failed to send verification code: " + response.get("errorMessage"));
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Validates the manually requested verification code. See {@link #sendManualVerificationCode(TrustedDevice)}.
	 *
	 * @param device the device the code was sent to.
	 * @param code the code.
	 * @param password the user's password.
	 */
	public void validateManualVerificationCode(TrustedDevice device, String code, char[] password) {
		try {
			URIBuilder uriBuilder = new URIBuilder(setupEndPoint + "/validateManualVerificationCode");
			populateUriParameters(uriBuilder);
			URI uri = uriBuilder.build();

			TrustedDeviceResponse responseDevice = new TrustedDeviceResponse();
			responseDevice.areaCode = device.areaCode;
			responseDevice.deviceType = device.deviceType;
			responseDevice.deviceId = device.deviceId;
			responseDevice.phoneNumber = device.phoneNumber;
			responseDevice.verificationCode = code;
			responseDevice.trustBrowser = true;

			HttpPost post = new HttpPost(uri);
			post.setEntity(new StringEntity(ICloudUtils.toJson(responseDevice), Consts.UTF_8));
			populateRequestHeadersParameters(post);

			Map<String, Object> response = httpClient.execute(post, new JsonToMapResponseHandler());

			if (!Boolean.TRUE.equals(response.get("success"))) {
				if (Double.valueOf(-21669.0).equals(response.get("errorCode"))) {
					throw new RuntimeException("Invalid verification code");
				} else {
					throw new IllegalStateException("Failed to verify code: " + response.get("errorMessage"));
				}
			}

			// Re-authenticate, which will both update the two-factor authentication data, and ensure that we save the
			// X-APPLE-WEBAUTH-HSA-TRUST cookie
			Map<String, Object> dsInfo = session.getDsInfoMap();
			authenticate((String) dsInfo.get("appleId"), password);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Gets the iCloud storage usage.
	 *
	 * @return the map of storage usage details.
	 */
	public Map<String, Object> getStorageUsage() {
		try {
			URIBuilder uriBuilder = new URIBuilder(setupEndPoint + "/storageUsageInfo");
			populateUriParameters(uriBuilder);
			URI uri = uriBuilder.build();

			HttpPost post = new HttpPost(uri);
			populateRequestHeadersParameters(post);

			try (CloseableHttpResponse response = httpClient.execute(post)) {
				Map<String, Object> result = new JsonToMapResponseHandler().handleResponse(response);
				if (Boolean.FALSE.equals(result.get("success"))) {
					throw new RuntimeException("Failed to get storage usage info: " + result.get("error"));
				}

				return result;
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Gets the web services map.
	 *
	 * @return the web services map.
	 */
	public Map<String, Object> getWebServicesMap() {
		return session.getWebServicesMap();
	}

	/**
	 * Gets the HTTP client.
	 *
	 * @return the client.
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Gets the cookie store.
	 *
	 * @return the store.
	 */
	public CookieStore getCookieStore() {
		return session.getCookieStore();
	}

	/**
	 * Gets the client ID.
	 *
	 * @return the client ID.
	 */
	public String getClientId() {
		return session.getClientId();
	}

	/**
	 * Gets the 'idmsa' service.
	 *
	 * @return the service.
	 */
	public IdmsaService getIdmsaService() {
		return idmsaService;
	}

	/**
	 * Populates the URI parameters for a request.
	 *
	 * @param uriBuilder the URI builder.
	 */
	public void populateUriParameters(URIBuilder uriBuilder) {
		uriBuilder.addParameter("clientId", getClientId()).addParameter("clientBuildNumber", "14E45");

		String dsid = getSessionId();
		if (!Strings.isNullOrEmpty(dsid)) {
			uriBuilder.addParameter("dsid", dsid);
		}
	}

	/**
	 * Gets the session ID.
	 *
	 * @return the session ID.
	 */
	public String getSessionId() {
		return session.getSessionId();
	}

	/**
	 * Populates the HTTP request headers.
	 *
	 * @param request the request to populate.
	 */
	public void populateRequestHeadersParameters(HttpRequestBase request) {
		request.setHeader("Origin", endPoint);
		request.setHeader("Referer", endPoint + "/");
		request.setHeader("User-Agent", "Opera/9.52 (X11; Linux i686; U; en)");
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
	}

	public ICloudSession getSession() {
		return session;
	}
}
