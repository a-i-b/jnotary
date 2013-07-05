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
package org.jnotary.dvcs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.SecureRandom;

import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIFreeText;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.jnotary.dvcs.DVCSCertInfo;
import org.jnotary.dvcs.DVCSErrorNotice;
import org.jnotary.dvcs.DVCSRequestInformation;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.DVCSTime;
import org.jnotary.dvcs.ServiceType;
import org.junit.Test;

public class SimpleResponseTest {

	String testData = "test data 1234567890";
	SecureRandom random = new SecureRandom();
	
	@Test
	public void allGoodResponses() throws IOException {
		
		DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.CPD);
		requestInformation.setNonce(new DERInteger(random.nextLong()));
		DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
		requestInformation.setRequestTime(requestTime );

		byte[] req_data = testData.getBytes();
		Digest digest = new SHA256Digest();
		digest.update(req_data, 0, req_data.length);		
		byte[] digestData = new byte[digest.getDigestSize()];
		digest.doFinal(digestData, 0);        
		DigestInfo messageImprint = new DigestInfo(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), digestData);

		DERInteger serialNumber = new DERInteger(random.nextLong());
		DVCSTime responseTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));

		DVCSCertInfo dvCertInfo = new DVCSCertInfo(requestInformation, messageImprint, serialNumber, responseTime);
		DVCSResponse respOut = new DVCSResponse(dvCertInfo );
		
		DVCSResponse respIn = DVCSResponse.getInstance(respOut.getEncoded());
		assertTrue("Service type is incorrect", respIn.getDvCertInfo().getRequestInformation().getService() == ServiceType.CPD);
		assertTrue("Nonce is incorrect", respIn.getDvCertInfo().getRequestInformation().getNonce().equals(respOut.getDvCertInfo().getRequestInformation().getNonce()));
		assertTrue("Request Time is incorrect", respIn.getDvCertInfo().getRequestInformation().getRequestTime().equals(respOut.getDvCertInfo().getRequestInformation().getRequestTime()));
		assertTrue("Message imprint is incorrect", respIn.getDvCertInfo().getMessageImprint().equals(respOut.getDvCertInfo().getMessageImprint()));	
		assertTrue("Serial number is incorrect", respIn.getDvCertInfo().getSerialNumber().equals(respOut.getDvCertInfo().getSerialNumber()));	
		assertTrue("Response time is incorrect", respIn.getDvCertInfo().getResponseTime().equals(respOut.getDvCertInfo().getResponseTime()));			
	}
	
	@Test
	public void errorResponse() throws IOException {

		PKIStatus status = PKIStatus.getInstance(new DERInteger(PKIStatus.REJECTION));
		PKIFreeText    statusString = new PKIFreeText("Free text");
	    PKIFailureInfo failInfo = new PKIFailureInfo(PKIFailureInfo.BAD_REQUEST);
	        
		PKIStatusInfo	transactionStatus = new PKIStatusInfo(status, statusString, failInfo);
		
		DVCSErrorNotice dvErrorNote = new DVCSErrorNotice(transactionStatus);
		DVCSResponse respOut = new DVCSResponse(dvErrorNote);
		
		DVCSResponse respIn = DVCSResponse.getInstance(respOut.getEncoded());
		assertTrue("Status igetInstances incorrect", respIn.getDvErrorNote().getTransactionStatus().getStatus().equals(respOut.getDvErrorNote().getTransactionStatus().getStatus()));			
		assertTrue("Status string is incorrect", respIn.getDvErrorNote().getTransactionStatus().getStatusString().equals(respOut.getDvErrorNote().getTransactionStatus().getStatusString()));			
		assertTrue("Status is incorrect", respIn.getDvErrorNote().getTransactionStatus().getFailInfo().equals(respOut.getDvErrorNote().getTransactionStatus().getFailInfo()));			
		
	}
}
