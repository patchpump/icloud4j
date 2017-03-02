/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.github.tmyroadctfig.icloud4j.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;

/**
 * Serializable implementation of {@link CookieStore}
 * 
 * @author patchpump
 */
public class SerializableCookieStore implements CookieStore, Serializable {

	private static final long serialVersionUID = 1L;
	private static final CookieIdentityComparator cookieComparator = new CookieIdentityComparator();

	private List<SerializableClientCookie> cookies = new ArrayList<>();

	public SerializableCookieStore() {
	}

	/**
	 * Adds an {@link Cookie HTTP cookie}, replacing any existing equivalent cookies.
	 * If the given cookie has already expired it will not be added, but existing
	 * values will still be removed.
	 *
	 * @param cookie the {@link Cookie cookie} to be added
	 *
	 * @see #addCookies(Cookie[])
	 */
	@Override
	public synchronized void addCookie(final Cookie cookie) {
		if (cookie != null) {
			for(Iterator<SerializableClientCookie> i = cookies.iterator(); i.hasNext();) {
				Cookie c = i.next();
				if(cookieComparator.compare(c, cookie) == 0)
					i.remove();
			}
			if (!cookie.isExpired(new Date()))
				cookies.add(new SerializableClientCookie(cookie));
		}
	}

	/**
	 * Adds an array of {@link Cookie HTTP cookies}. Cookies are added individually and
	 * in the given array order. If any of the given cookies has already expired it will
	 * not be added, but existing values will still be removed.
	 *
	 * @param cookies the {@link Cookie cookies} to be added
	 *
	 * @see #addCookie(Cookie)
	 */
	public synchronized void addCookies(final Cookie[] cookies) {
		if (cookies != null) {
			for (final Cookie cooky : cookies)
				this.addCookie(cooky);
		}
	}

	/**
	 * Returns an immutable array of {@link Cookie cookies} that this HTTP
	 * state currently contains.
	 *
	 * @return an array of {@link Cookie cookies}.
	 */
	@Override
	public synchronized List<Cookie> getCookies() {
		return new ArrayList<Cookie>(cookies);
	}

	/**
	 * Removes all of {@link Cookie cookies} in this HTTP state
	 * that have expired by the specified {@link java.util.Date date}.
	 *
	 * @return true if any cookies were purged.
	 *
	 * @see Cookie#isExpired(Date)
	 */
	@Override
	public synchronized boolean clearExpired(final Date date) {
		if (date == null)
			return false;
		boolean removed = false;
		for (final Iterator<SerializableClientCookie> it = cookies.iterator(); it.hasNext();) {
			if (it.next().isExpired(date)) {
				it.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Clears all cookies.
	 */
	@Override
	public synchronized void clear() {
		cookies.clear();
	}

	@Override
	public synchronized String toString() {
		return cookies.toString();
	}
}
