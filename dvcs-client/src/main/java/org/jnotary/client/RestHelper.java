/*******************************************************************************
 * Copyright (c) 2013 A&B.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Alexander Balashov (balaschow.a@gmail.com) - initial API and implementation
 ******************************************************************************/
package org.jnotary.client;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class RestHelper {

	static {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}
	static byte[] call(String url, byte[] requestData) throws Exception {
    	ClientRequest request = new ClientRequest(url);
    	request.accept(MediaType.APPLICATION_OCTET_STREAM);
    	request.body(MediaType.APPLICATION_OCTET_STREAM, requestData);
    	ClientResponse<byte[]> response;
		try {
			response = request.post(byte[].class);
			if(response.getResponseStatus().getStatusCode() != 200)
				throw new Exception("HTTP error: " + response.getResponseStatus().getStatusCode());
			return response.getEntity();
    				
		} catch (Exception e) {
			throw new Exception("Can't call DVCS-service: " + e.getLocalizedMessage());
		}
		
	}
}
