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
package org.jnotary.dvcs;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.openssl.PEMParser;
import org.jnotary.dvcs.CertEtcToken;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSRequestInformation;
import org.jnotary.dvcs.DVCSTime;
import org.jnotary.dvcs.Data;
import org.jnotary.dvcs.ServiceType;
import org.jnotary.dvcs.TargetEtcChain;
import org.jnotary.dvcs.util.DvcsHelper;
import org.junit.Test;

public class SimpleRequestTest {

	String testData = "test data 1234567890";
	SecureRandom random = new SecureRandom();
	
	@Test
	public void cpd() throws IOException {
		
		DEROctetString message = new DEROctetString(testData.getBytes());
		Data data = new Data(message );
		
		DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.CPD);
		
		requestInformation.setNonce(new DERInteger(random.nextLong()));

		DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
		requestInformation.setRequestTime(requestTime );
		
		DVCSRequest reqOut = new DVCSRequest(requestInformation, data);
		
		DVCSRequest reqIn = DVCSRequest.getInstance(reqOut.getEncoded());
		assertTrue("Service type is incorrect", reqIn.getRequestInformation().getService() == ServiceType.CPD);
		assertTrue("Nonce is incorrect", reqIn.getRequestInformation().getNonce().equals(reqOut.getRequestInformation().getNonce()));
		assertTrue("Request Time is incorrect", reqIn.getRequestInformation().getRequestTime().equals(reqOut.getRequestInformation().getRequestTime()));
		assertTrue("Data is incorrect", reqIn.getData().getMessage().equals(reqOut.getData().getMessage()));	
	}

	@Test
	public void ccpd() throws IOException {
		
		byte[] req_data = testData.getBytes();
		Digest digest = new SHA256Digest();
		digest.update(req_data, 0, req_data.length);
		
		byte[] digestData = new byte[digest.getDigestSize()];
		digest.doFinal(digestData, 0);
        
		DigestInfo messageImprint = new DigestInfo(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), digestData);
		Data data = new Data(messageImprint);
		
		DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.CCPD);
		
		requestInformation.setNonce(new DERInteger(random.nextLong()));

		DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
		requestInformation.setRequestTime(requestTime );
		
		DVCSRequest reqOut = new DVCSRequest(requestInformation, data);
		
		DVCSRequest reqIn = DVCSRequest.getInstance(reqOut.getEncoded());
		assertTrue("Service type is incorrect", reqIn.getRequestInformation().getService() == ServiceType.CCPD);
		assertTrue("Nonce is incorrect", reqIn.getRequestInformation().getNonce().equals(reqOut.getRequestInformation().getNonce()));
		assertTrue("Request Time is incorrect", reqIn.getRequestInformation().getRequestTime().equals(reqOut.getRequestInformation().getRequestTime()));
		assertTrue("Digest alg is incorrect", reqIn.getData().getMessageImprint().getAlgorithmId().equals(reqOut.getData().getMessageImprint().getAlgorithmId()));	
		assertTrue("Digest value is incorrect", Arrays.equals(reqIn.getData().getMessageImprint().getDigest(), reqOut.getData().getMessageImprint().getDigest()));	
	}
	
	@Test
	public void vpkc() throws Exception {

		X509Certificate cert = loadCert("testdvcs.crt");
	
		TargetEtcChain[] chain = new TargetEtcChain[1];
		CertEtcToken target = new CertEtcToken(Certificate.getInstance(cert.getEncoded()));
		assertNotNull("Target is null", target.toASN1Primitive());
		
		chain[0] = new TargetEtcChain(target, null, null);
		Data data = new Data(chain);
		assertNotNull("Data is null", data.toASN1Primitive());
		
		DVCSRequestInformation requestInformation = new DVCSRequestInformation(ServiceType.VPKC);
		
		requestInformation.setNonce(new DERInteger(random.nextLong()));

		DVCSTime requestTime = new DVCSTime(new DERGeneralizedTime(new java.util.Date()));
		requestInformation.setRequestTime(requestTime );
		
		DVCSRequest reqOut = new DVCSRequest(requestInformation, data);
		
		DVCSRequest reqIn = DVCSRequest.getInstance(reqOut.getEncoded());
		assertTrue("Service type is incorrect", reqIn.getRequestInformation().getService() == ServiceType.VPKC);
		assertTrue("Nonce is incorrect", reqIn.getRequestInformation().getNonce().equals(reqOut.getRequestInformation().getNonce()));
		assertTrue("Request Time is incorrect", reqIn.getRequestInformation().getRequestTime().equals(reqOut.getRequestInformation().getRequestTime()));
		assertTrue("No certificates", reqIn.getData().getCerts() != null && reqIn.getData().getCerts().length != 0);
		
		CertEtcToken targetIn = reqIn.getData().getCerts()[0].getTarget();
		assertNotNull("No target", targetIn);
              
		assertTrue("Certificate is incorrect", Arrays.equals(targetIn.getCertificate().getEncoded(), cert.getEncoded()));	
	}

    private X509Certificate loadCert(String certName) throws Exception
    {
        CertificateFactory rd = CertificateFactory.getInstance("X.509");
        return (X509Certificate)rd.generateCertificate(getClass().getClassLoader().getResourceAsStream(certName));
    }
    
	public static byte[] getBytes(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}
    
	// Examples in RFC have wrong structure
	@Test(expected=Exception.class)
	public void parseCpkcRFCExample() throws IOException, CMSException {
		
		Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("ccpdReqRfc.pem"));
		PEMParser pemParser = new PEMParser(reader);
		byte[] content = pemParser.readPemObject().getContent();
		CMSSignedData signedData = new CMSSignedData(content);
		CMSTypedData data = signedData.getSignedContent();
		
		DVCSRequest reqIn = DVCSRequest.getInstance(data.getContent());
		assertTrue("Service type is incorrect", reqIn.getRequestInformation().getService() == ServiceType.CCPD);
	}

	@Test
	public void createVpkcWithHelper() throws Exception {	
		X509Certificate cert = loadCert("testdvcs.crt");
		DVCSRequest reqOut = DvcsHelper.createVpkc(cert.getEncoded(), random.nextLong());
		assertNotNull("Vpkc is not created", reqOut);
	}
}
