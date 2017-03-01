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

package com.github.tmyroadctfig.icloud4j.util;

import com.github.tmyroadctfig.icloud4j.ICloudException;
import com.github.tmyroadctfig.icloud4j.json.SerializableBasicClientCookie;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * iCloud utilities.
 *
 * @author Luke Quinanne
 */
public class ICloudUtils {

	static final Gson gson = new GsonBuilder().registerTypeAdapter(Cookie.class, new CookieInstanceCreator()).create();

	/**
	 * Parses a JSON response from the request.
	 *
	 * @param httpClient the HTTP client.
	 * @param post the request.
	 * @param responseClass the type of JSON object to parse the values into.
	 * @param <T> the type to parse into.
	 * @return the object.
	 * @throws ICloudException if there was an error returned from the request.
	 */
	public static <T> T parseJsonResponse(CloseableHttpClient httpClient, HttpPost post, Class<T> responseClass) {
		String rawResponseContent = "<no content>";
		try (CloseableHttpResponse response = httpClient.execute(post)) {
			rawResponseContent = new StringResponseHandler().handleResponse(response);

			try {
				return fromJson(rawResponseContent, responseClass);
			} catch (JsonSyntaxException e1) {
				Map<String, Object> errorMap = fromJson(rawResponseContent, Map.class);
				System.err.println(rawResponseContent);
				throw new ICloudException(response, errorMap);
			}
		} catch (IOException e) {
			System.err.println(rawResponseContent);
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Parses a JSON response from the request.
	 *
	 * @param httpClient the HTTP client.
	 * @param httpGet the request.
	 * @param responseClass the type of JSON object to parse the values into.
	 * @param <T> the type to parse into.
	 * @return the object.
	 * @throws ICloudException if there was an error returned from the request.
	 */
	public static <T> T parseJsonResponse(CloseableHttpClient httpClient, HttpGet httpGet, Class<T> responseClass) {
		try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
			String rawResponseContent = new StringResponseHandler().handleResponse(response);

			try {
				return fromJson(rawResponseContent, responseClass);
			} catch (JsonSyntaxException e1) {
				Map<String, Object> errorMap = fromJson(rawResponseContent, Map.class);
				throw new ICloudException(response, errorMap);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static String toJson(Object o) {
		return gson.toJson(o);
	}
	
	public static <T> T fromJson(String s, Class<?> type) {
		return gson.<T> fromJson(s, type);
	}

	public static <T> T fromJson(String s, Type type) {
		return gson.<T> fromJson(s, type);
	}
	
	private static class CookieInstanceCreator implements InstanceCreator<Cookie> {

		@Override
		public Cookie createInstance(Type type) {
			return new SerializableBasicClientCookie();	
		}
	}
}
