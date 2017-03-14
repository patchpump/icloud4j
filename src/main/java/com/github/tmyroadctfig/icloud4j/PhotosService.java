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

import com.github.tmyroadctfig.icloud4j.json.PhotosAlbumsResponse;
import com.github.tmyroadctfig.icloud4j.json.PhotosFolder;
import com.github.tmyroadctfig.icloud4j.util.ICloudUtils;
import com.github.tmyroadctfig.icloud4j.util.StringResponseHandler;
import com.google.common.base.Throwables;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Access to the photos service.
 *
 * @author Luke Quinane
 */
public class PhotosService {
	/**
	 * The iCloud service.
	 */
	private final ICloudService iCloudService;

	/**
	 * The service root URL.
	 */
	private final String serviceRoot;

	/**
	 * The service end point.
	 */
	private final String endPoint;

	/**
	 * The sync token.
	 */
	private final String syncToken;

	/**
	 * Creates a new photos service.
	 *
	 * @param iCloudService the iCloud service.
	 */
	public PhotosService(ICloudService iCloudService) {
		this.iCloudService = iCloudService;
		@SuppressWarnings("unchecked")
		Map<String, Object> photosSettings = (Map<String, Object>) iCloudService.getWebServicesMap().get("photos");
		serviceRoot = (String) photosSettings.get("url");
		endPoint = serviceRoot + "/ph";
		syncToken = getSyncToken();
	}

	/**
	 * Gets the sync token.
	 *
	 * @return the sync token.
	 */
	private String getSyncToken() {
		try {
			URIBuilder uriBuilder = new URIBuilder(endPoint + "/startup");
			iCloudService.populateUriParameters(uriBuilder);
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			iCloudService.populateRequestHeadersParameters(httpGet);

			String rawResponse = iCloudService.getHttpClient().execute(httpGet, new StringResponseHandler());

			Type type = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, Object> responseMap = ICloudUtils.fromJson(rawResponse, type);

			return (String) responseMap.get("syncToken");
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Populates the URI parameters for a request.
	 *
	 * @param uriBuilder the URI builder.
	 */
	public void populateUriParameters(URIBuilder uriBuilder) {
		uriBuilder.addParameter("dsid", iCloudService.getSessionId()).addParameter("clientBuildNumber", "14E45")
			.addParameter("clientInstanceId", iCloudService.getClientId()).addParameter("syncToken", syncToken);
	}

	/**
	 * Gets the all-photos album.
	 *
	 * @return the album.
	 */
	public PhotosFolder getAllPhotosAlbum() {
		return getAlbums().stream().filter(folder -> "all-photos".equals(folder.serverId)).findFirst().get();
	}

	/**
	 * Gets a list of albums.
	 *
	 * @return the list of albums.
	 */
	public List<PhotosFolder> getAlbums() {
		try {
			URIBuilder uriBuilder = new URIBuilder(endPoint + "/folders");
			populateUriParameters(uriBuilder);
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			iCloudService.populateRequestHeadersParameters(httpGet);

			String rawResponse = iCloudService.getHttpClient().execute(httpGet, new StringResponseHandler());
			PhotosAlbumsResponse photosAlbumsResponse = ICloudUtils.fromJson(rawResponse, PhotosAlbumsResponse.class);

			return Arrays.stream(photosAlbumsResponse.folders).filter(folder -> "album".equals(folder.type))
				.collect(Collectors.toList());
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
