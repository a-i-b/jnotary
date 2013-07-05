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
package org.jnotary.dvcs.util;

import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.jnotary.dvcs.CertEtcToken;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSRequestInformation;
import org.jnotary.dvcs.DVCSTime;
import org.jnotary.dvcs.Data;
import org.jnotary.dvcs.ServiceType;
import org.jnotary.dvcs.TargetEtcChain;

public class DvcsHelper {
	
	static {
//		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}
	
	private static DVCSRequest createRequestWithOctets(int serviceType, byte[] reqdata, Long nonce) {
		DEROctetString message = new DEROctetString(reqdata);
		Data data = new Data(message);		
		DVCSRequestInformation requestInformation = new DVCSRequestInformation(serviceType);
		requestInformation.setNonce(new DERInteger(nonce));
		DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
		requestInformation.setRequestTime(requestTime);
		return new DVCSRequest(requestInformation, data);		
	}
	
	public static DVCSRequest createCpd(byte[] reqdata, Long nonce) throws Exception {
		try {
			return createRequestWithOctets(	ServiceType.CPD, reqdata, nonce);
		} catch (Exception e) {
			throw new Exception("Can't create cpd request: " + e.getLocalizedMessage());
		}
	}

	public static DVCSRequest createVsd(byte[] reqdata, Long nonce) throws Exception {
		try {
			return createRequestWithOctets(	ServiceType.VSD, reqdata, nonce);
		} catch (Exception e) {
			throw new Exception("Can't create vsd request: " + e.getLocalizedMessage());
		}
	}

	public static DVCSRequest createCcpd(DigestInfo messageImprint, Long nonce) throws Exception {
		try {
			Data data = new Data(messageImprint);		
			DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.CCPD);
			requestInformation.setNonce(new DERInteger(nonce));
			DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
			requestInformation.setRequestTime(requestTime);
			return new DVCSRequest(requestInformation, data);		
		} catch (Exception e) {
			throw new Exception("Can't create ccpd request: " + e.getLocalizedMessage());
		}
	}

	public static DVCSRequest createVpkc(byte[] reqdata, Long nonce) throws Exception {
		try {		
			TargetEtcChain[] chain = new TargetEtcChain[1];
			CertEtcToken target = new CertEtcToken(Certificate.getInstance(reqdata));		
			chain[0] = new TargetEtcChain(target, null, null);
			Data data = new Data(chain);

			DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.VPKC);
			requestInformation.setNonce(new DERInteger(nonce));
			DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
			requestInformation.setRequestTime(requestTime);
			return new DVCSRequest(requestInformation, data);		
		} catch (Exception e) {
			throw new Exception("Can't create vpkc request: " + e.getLocalizedMessage());
		}
	}
}
