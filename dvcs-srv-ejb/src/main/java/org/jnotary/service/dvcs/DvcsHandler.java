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
package org.jnotary.service.dvcs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.jnotary.dvcs.CertEtcToken;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.ServiceType;
import org.jnotary.dvcs.util.DVCSException;
import org.jnotary.dvcs.util.ErrorResponseFactory;
import org.jnotary.dvcs.util.StatusInfoFactory;
import org.jnotary.service.util.CryptoService;
import org.jnotary.service.util.IGlobalResources;

@Stateless
@LocalBean
@Remote(IDvcsHandler.class)
public class DvcsHandler implements IDvcsHandler {
	@Inject
	private Logger log;	
	@Inject
	private CryptoService cryptoService;
	@Inject
	private IGlobalResources globalResources;
	
	@Override
	public byte[] handle(DVCSRequest request) throws Exception {
		DVCSResponse response = null;
		try {
			if(!globalResources.getServiceConfig().asAllowed(request.getRequestInformation().getService())) {
				throw new DVCSException(PKIStatus.REJECTION,
						"Service is disabled",
						PKIFailureInfo.systemUnavail);
			}
			
			switch(request.getRequestInformation().getService()) {
			case ServiceType.CPD:
				response = handleCpd(request);
				break;
			case ServiceType.CCPD:
				response = handleCcpd(request);
				break;
			case ServiceType.VPKC:
				response = handleVpkc(request);
				break;
			case ServiceType.VSD:
				response = handleVsd(request);			
				break;
			}			
		} catch (DVCSException e) {
			//Signature is bad
			log.severe(e.getLocalizedMessage());
			response = ErrorResponseFactory.getInstance(e);
		} catch (Exception e) {
			//Signature is bad
			log.severe(e.getLocalizedMessage());
			response = ErrorResponseFactory.getInstance(PKIStatus.REJECTION, e.getLocalizedMessage());
		}
					
		return cryptoService.sign(response.getEncoded());
	}
	
	private DVCSResponse handleCpd(DVCSRequest request) throws DVCSException {
		DvcsResponseHelper response = new DvcsResponseHelper(globalResources.getSerialNumber(), globalResources.getServiceConfig());
		byte[] digestData = response.getDigest(request.getData().getMessage().getOctets());
		DigestInfo messageImprint = response.getDigestInfo(digestData);
		return response.createResponse(request, messageImprint);
	}

	private DVCSResponse handleCcpd(DVCSRequest request) throws DVCSException {
		DvcsResponseHelper response = new DvcsResponseHelper(globalResources.getSerialNumber(), globalResources.getServiceConfig());
		return response.createResponse(request, request.getData().getMessageImprint());
	}

	private DVCSResponse handleVpkc(DVCSRequest request) throws DVCSException {
		PKIStatusInfo	dvStatus = null;
		byte[] certData = null;
		try {
			try {
				java.security.cert.X509Certificate certificate = getCertificate(request);								
				certData = request.getData().toASN1Primitive().getEncoded();
				cryptoService.verifyCerificate(certificate, true);
				dvStatus = StatusInfoFactory.getInstance(PKIStatus.GRANTED, null, null);
			} catch(DVCSException e) {
				dvStatus = StatusInfoFactory.getInstance(e.getPkiStatus(), e.getFreeText(), e.getPkiFailInfo());
			}

		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.systemFailure);	
		}	
		DvcsResponseHelper response = new DvcsResponseHelper(globalResources.getSerialNumber(), globalResources.getServiceConfig());
		byte[] digestData = response.getDigest(certData);
		DigestInfo messageImprint = response.getDigestInfo(digestData);
		return response.createResponse(request, messageImprint, dvStatus);
		
	}

	private DVCSResponse handleVsd(DVCSRequest request) throws DVCSException {
		PKIStatusInfo	dvStatus = null;					
		byte[] signedData = request.getData().getMessage().getOctets();
		try {
			cryptoService.verifySignedDocument(signedData);
			dvStatus = StatusInfoFactory.getInstance(PKIStatus.GRANTED, null, null);
		} catch(DVCSException e) {
			dvStatus = StatusInfoFactory.getInstance(e.getPkiStatus(), e.getFreeText(), e.getPkiFailInfo());
		}

		DvcsResponseHelper response = new DvcsResponseHelper(globalResources.getSerialNumber(), globalResources.getServiceConfig());
		byte[] digestData = response.getDigest(signedData);
		DigestInfo messageImprint = response.getDigestInfo(digestData);
		return response.createResponse(request, messageImprint, dvStatus);
	}

	private java.security.cert.X509Certificate getCertificate(
			DVCSRequest request) throws DVCSException {
		
		if(request.getData().getCerts() == null || request.getData().getCerts().length == 0) {
			throw new DVCSException(PKIStatus.REJECTION, 
					"No certificates in request",
					PKIFailureInfo.badRequest);			
		}
		
		CertEtcToken targetIn = request.getData().getCerts()[0].getTarget();
		if(targetIn == null)
			throw new DVCSException(PKIStatus.REJECTION, 
					"No certificates in request",
					PKIFailureInfo.badRequest);			

		java.security.cert.X509Certificate certificate = null;
		try {
			CertificateFactory rd = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(targetIn.getCertificate().getEncoded());
			certificate = (X509Certificate) rd.generateCertificate(in);
		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.badRequest);	
		}
		return certificate;
	}	
	
}
