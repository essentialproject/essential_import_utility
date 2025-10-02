/**
 * Copyright (c)2019 Enterprise Architecture Solutions ltd. and the Essential Project
 * contributors.
 * This file is part of Essential Architecture Manager, 
 * the Essential Architecture Meta Model and The Essential Project.
 *
 * Essential Architecture Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Essential Architecture Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Essential Architecture Manager.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.enterprise_architecture.essential.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author David Kumar
 * 
 * Utils class to help with http calls
 * 
 */
public class HttpUtils {
	@SuppressWarnings("unused")
	private static final Logger myLog = LoggerFactory.getLogger(HttpUtils.class);
	

	/**
	 * Lambda for processing HTTP response
	 */
	public interface ResponseProcessor {
		void process(String theResponseStr);
	}

	public static void doHttpEnclosingEntityRequest(HttpEntityEnclosingRequestBase theHttpRequest, ResponseProcessor theResponseProcessor) throws IOException {
		CloseableHttpClient anHttpclient = HttpClients.createDefault();
		CloseableHttpResponse response = anHttpclient.execute(theHttpRequest);
		try {
			HttpEntity entity = response.getEntity();
			StatusLine status = response.getStatusLine();
			
			if (status.getStatusCode() < 200 || status.getStatusCode() >= 300) {
				if (entity != null) {
					String responseStr = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
					throw new IllegalStateException("error response, status: "+status+":"+responseStr);
				} else {
					throw new IllegalStateException("error response, status: "+status+":no error message returned");
				}
			}
			//success, unpack response
			if (entity != null) {
				String aResponseStr = EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
				theResponseProcessor.process(aResponseStr);
				EntityUtils.consume(entity);
			} else {
				throw new IllegalStateException("success but no response body returned");
			}
			
		} finally {
			response.close();
		}
	}

}
