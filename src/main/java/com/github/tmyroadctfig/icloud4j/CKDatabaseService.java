package com.github.tmyroadctfig.icloud4j;

import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.github.tmyroadctfig.icloud4j.json.CKResponse;
import com.github.tmyroadctfig.icloud4j.util.JsonResponseHandler;
import com.google.common.base.Throwables;

/**
 * CloudKit database service.
 * 
 * https://developer.apple.com/reference/cloudkit/
 * 
 * @author patchpump
 */
public class CKDatabaseService {

	public static final String ENDPOINT_PHOTOS = "/database/1/com.apple.photos.cloud/production/private/records";

	private static final JsonResponseHandler<CKResponse> RESPONSE_HANDLER = new JsonResponseHandler<CKResponse>(CKResponse.class);

	private static final String CLIENT_VERSION = "17AHotfix3";
	private static final String CKJS_BUILD_VERSION = "17AProjectDev84";
	private static final String CKJS_VERSION = "2.0.34";

	/**
	 * The iCloud service.
	 */
	private final ICloudService iCloudService;

	/**
	 * Service root URL.
	 */
	private final String serviceRoot;

	/**
	 * Service end point.
	 */
	private final String endPoint;

	/**
	 * Creates a new CKDatabase service.
	 *
	 * @param iCloudService the iCloud service.
	 * @param endpoint database endpoint
	 */
	public CKDatabaseService(ICloudService iCloudService, String endPoint) {
		this.iCloudService = iCloudService;
		@SuppressWarnings("unchecked")
		Map<String, Object> settings = (Map<String, Object>) iCloudService.getWebServicesMap().get("ckdatabasews");
		this.serviceRoot = (String) settings.get("url");
		this.endPoint = serviceRoot + endPoint;
	}

	/**
	 * Populates the URI parameters for a request.
	 *
	 * @param uriBuilder the URI builder.
	 */
	public void populateUriParameters(URIBuilder uriBuilder) {
		uriBuilder.addParameter("dsid", iCloudService.getSessionId())
			.addParameter("ckjsBuildVersion", CKJS_BUILD_VERSION)
			.addParameter("ckjsVersion", CKJS_VERSION)
			.addParameter("getCurrentSyncToken", "true")
			.addParameter("clientBuildNumber", CLIENT_VERSION)
			.addParameter("clientMasteringNumber", CLIENT_VERSION)
			.addParameter("remapEnums", "true")
			.addParameter("clientId", iCloudService.getClientId())
			.addParameter("clientInstanceId", iCloudService.getClientId());
	}

	/**
	 * Perform CKDatabase query.
	 *
	 * @param query in JSON format
	 * @return the list of albums.
	 */
	public CKResponse query(String query) {
		try {
			URIBuilder uriBuilder = new URIBuilder(endPoint + "/query");
			populateUriParameters(uriBuilder);
			HttpPost post = new HttpPost(uriBuilder.build());
			iCloudService.populateRequestHeadersParameters(post);
			post.addHeader("clientMasteringNumber", CLIENT_VERSION);
			post.setEntity(new StringEntity(query));
			return iCloudService.getHttpClient().execute(post, RESPONSE_HANDLER);

		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
