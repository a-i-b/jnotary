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
package org.jnotary.service.dvcs;

import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.jnotary.crypto.Hasher;
import org.jnotary.dvcs.DVCSCertInfo;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSRequestInformation;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.DVCSTime;
import org.jnotary.dvcs.util.DVCSException;
import org.jnotary.service.util.ServiceConfig;

public class DvcsResponseHelper {
	
	private ServiceConfig config;
	private DERInteger serialNumber;

	public DvcsResponseHelper(Integer serialNumber, ServiceConfig config) {
		this.config = config;
		this.serialNumber = new DERInteger(serialNumber);
	}

	protected byte[] getDigest(byte[] data) throws DVCSException {
		byte[] digestData = null;
		try {
			digestData = Hasher.makeHash(config.getHashAlgorithm(), data);
		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.systemFailure);	
		}
		return digestData;
	}

	protected DigestInfo getDigestInfo(byte[] digestData) throws DVCSException {
		DigestInfo messageImprint = new DigestInfo(
				new AlgorithmIdentifier(config.getHashAlgorithm()),
				digestData);
		return messageImprint;
	}
	
	protected DVCSResponse createResponse(DVCSRequest request, DigestInfo messageImprint) throws DVCSException {
		PKIStatus status = PKIStatus.getInstance(new DERInteger(PKIStatus.GRANTED));		        
		PKIStatusInfo	dvStatus = new PKIStatusInfo(status, null, null);
		return createResponse(request, messageImprint, dvStatus);
	}
	
	protected DVCSResponse createResponse(DVCSRequest request, DigestInfo messageImprint, PKIStatusInfo	dvStatus) throws DVCSException {
		
			DVCSRequestInformation requestInformation = new DVCSRequestInformation(request.getRequestInformation().getService());
			requestInformation.setNonce(request.getRequestInformation().getNonce());
			DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
			requestInformation.setRequestTime(requestTime);
				
			DVCSTime responseTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
	
			DVCSCertInfo dvCertInfo = new DVCSCertInfo(requestInformation, messageImprint, serialNumber, responseTime);
			dvCertInfo.setDvStatus(dvStatus);
			return new DVCSResponse(dvCertInfo);
		}

}
