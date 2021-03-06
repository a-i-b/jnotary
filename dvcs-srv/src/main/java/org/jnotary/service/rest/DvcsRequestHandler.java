/*******************************************************************************
 * Copyright (c) 2013 aib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     aib - initial API and implementation
 ******************************************************************************/
package org.jnotary.service.rest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.jnotary.crypto.Verifier.VerifyResult;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.util.DVCSException;
import org.jnotary.dvcs.util.ErrorResponseFactory;
import org.jnotary.service.dvcs.IDvcsHandler;
import org.jnotary.service.util.CryptoService;
import org.jnotary.service.util.IGlobalResources;

import java.util.logging.Logger;

/**
 * 
 * This class produces a RESTful service to read the contents of the members table.
 */


@Path("/dvcs")

public class DvcsRequestHandler {

	@Inject
	private Logger log;
	
	@Inject
	CryptoService cryptoService;
	
	@Inject
	IDvcsHandler dvcsHandler;

	@Inject
	private IGlobalResources globalResources;
	
	@POST
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response call(byte[] incomingRequest, @Context HttpServletRequest request) {
		log.info("Remote address: " + request.getRemoteAddr());
		if(incomingRequest == null || incomingRequest.length == 0) {
			log.severe("Bad HTTP-request");
			return Response.status(400).build();
		}
		
		byte[] signedResponse = null;
		try {
			log.fine("Signature verification..");
			VerifyResult dvcsReqVerifyResult = cryptoService.verifySignature(incomingRequest);
			//TODO: checkCertificateEnabled(dvcsReqVerifyResult);
			cryptoService.verifyCerificates(dvcsReqVerifyResult, globalResources.getServiceConfig().isVerifyCRL());
		
			log.fine("Message parsing..");
			DVCSRequest dvcsRequest = getDVCSRequest(dvcsReqVerifyResult.getContent());
			log.info("Service: " + dvcsRequest.getRequestInformation().getService());
			log.info(dvcsRequest.getRequestInformation().getNonce().getPositiveValue().toString());
			log.info(dvcsRequest.getRequestInformation().getRequestTime().getGenTime().getTimeString());

			signedResponse = dvcsHandler.handle(dvcsRequest);
		} catch(DVCSException e) {
			try {
				DVCSResponse response = ErrorResponseFactory.getInstance(e);
				signedResponse = cryptoService.sign(response.getEncoded());
			} catch (Exception ex) {
				log.severe(ex.getLocalizedMessage());
				return Response.status(500).build();
			}
		
		} catch (Exception e) {
			log.severe(e.getLocalizedMessage());
			DVCSResponse response = ErrorResponseFactory.getInstance(PKIStatus.REJECTION, e.getLocalizedMessage());
			try {
				signedResponse = cryptoService.sign(response.getEncoded());
			} catch (Exception ex) {
				log.severe(ex.getLocalizedMessage());
				return Response.status(500).build();
			}
		}
		
		return Response.status(200).entity(signedResponse).build();
	}
	
	public DVCSRequest getDVCSRequest(byte[] requestBlob) throws DVCSException {
		try {
			return DVCSRequest.getInstance(requestBlob);
		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.badRequest);
		}
	}
}
