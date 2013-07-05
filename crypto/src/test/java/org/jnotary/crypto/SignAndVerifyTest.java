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
package org.jnotary.crypto;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cms.SignerInformation;
import org.jnotary.crypto.Signer;
import org.jnotary.crypto.Verifier;
import org.jnotary.crypto.Verifier.VerifyResult;
import org.junit.Test;


// create trusted user store
// keytool -export -alias Client1 -storetype PKCS12  -keystore Client1.p12  -file Client1.cer
// keytool -import -alias Client1  -keystore trustedusers.jks  -file Client1.cer
// keytool -list -v -keystore trustedusers.jks 

// create trusted root store
// keytool -import -alias ca1  -keystore trustedroots.jks  -file Client1_ca.cer

public class SignAndVerifyTest {

	private void sign(boolean addCert) throws Exception {
		Signer signer = new Signer("SHA1withRSA");

		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(getClass().getClassLoader().getSystemResource("Client1.p12").getPath(), "PKCS12","12345678"),
				"Client1", "12345678");

		Signer.Parameters signerParameters = signer.getDefaultParameters();
		signerParameters.setAddSignerSertificate(addCert);
		signerParameters.setDetached(false);
		byte[] signed = signer.sign(userKeyStorage, "Hello world!!".getBytes(), signerParameters);
		
        try{
        	FileOutputStream fos = new FileOutputStream("/tmp/s1.dat");
        	fos.write(signed);
        }catch (Exception e){//Catch exception if any
        	  System.err.println("Error: " + e.getMessage());
        }
	}
	
	private void verify(TrustedStore trustedUserCertificateStore) throws Exception {
		
		File file = new File("/tmp/s1.dat");
		byte [] signedData = new byte[(int)file.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		dis.readFully(signedData);
		dis.close();		
		
		Verifier verifier = new Verifier();		
		VerifyResult result = verifier.verifySignature(signedData, trustedUserCertificateStore);
		assertTrue("Data is incorrect", Arrays.equals(result.getContent(), "Hello world!!".getBytes()));	
		for(SignerInformation signerInfo: result.getSigners()) {
			final Attribute attribute = (Attribute)signerInfo.getSignedAttributes().getAll(CMSAttributes.signingTime).get(0);
			final Date date = Time.getInstance(attribute.getAttrValues().getObjectAt(0)).getDate();
			
			System.out.println(date.toString() + " " +signerInfo.getSID());
		} 

	}

	@Test
	public void signWithCertAndVerifyWithCertInMessage() throws Exception {
		sign(true);
		verify(null);
	}

	@Test (expected=Exception.class)
	public void signWithoutCertAndVerifyWithCertInMessage() throws Exception {
		sign(false);
		verify(null);
	}
 
	@Test
	public void signWithoutCertAndVerifyWithCertInStore() throws Exception {
		sign(false);
		TrustedStore trustedUserCertificateStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("trustedusers.jks").getPath(),
						"JKS", "12345678"));
		verify(trustedUserCertificateStore);
	}

	@Test
	public void verifyCertificate() throws Exception {
		TrustedStore trustedRootStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("cacerts").getPath(),
						"JKS", "changeit"));

		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(getClass().getClassLoader().getSystemResource("Client1.p12").getPath(), "PKCS12","12345678"),
				"Client1", "12345678");
		
		Verifier verifier = new Verifier();		
		try {
			verifier.verifyCertificate(trustedRootStore, userKeyStorage.getUserCertificate());
		} catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
			throw e;
		}
	}
	
	@Test(expected=Exception.class)
	public void verifyCertificateWithoutRootInStore() throws Exception {
		TrustedStore trustedRootStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("trustedusers.jks").getPath(),
						"JKS", "12345678"));
		
		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(getClass().getClassLoader().getSystemResource("Client1.p12").getPath(), "PKCS12","12345678"),
				"Client1", "12345678");
		
		Verifier verifier = new Verifier();		
		try {
			verifier.verifyCertificate(trustedRootStore, userKeyStorage.getUserCertificate());
		} catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
			throw e;
		}
	}	

	@Test
	public void verifyNormalCertWithCRL() throws Exception {
		CRLStorage crlStorage = new CRLStorage();
		crlStorage.addCRLSource(
				CertUtil.loadCertificate(getClass().getClassLoader().getSystemResource("Client1_ca.cer").getPath()).hashCode(), 
				getClass().getClassLoader().getSystemResource("AdminCA1.crl").getPath());

		TrustedStore trustedRootStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("cacerts").getPath(),
						"JKS", "changeit"));
		
		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(getClass().getClassLoader().getSystemResource("Client1.p12").getPath(), "PKCS12","12345678"),
				"Client1", "12345678");
		
		Verifier verifier = new Verifier();		
		verifier.verifyCertificate(trustedRootStore, userKeyStorage.getUserCertificate());
		verifier.verifyCertificateCRLs(crlStorage, userKeyStorage.getUserCertificate());
	}

	@Test(expected=Exception.class)
	public void verifyRevokedCertWithCRL() throws Exception {
		CRLStorage crlStorage = new CRLStorage();
		crlStorage.addCRLSource(
				CertUtil.loadCertificate(getClass().getClassLoader().getSystemResource("Client1_ca.cer").getPath()).hashCode(), 
				getClass().getClassLoader().getSystemResource("AdminCA1.crl").getPath());

		TrustedStore trustedRootStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("cacerts").getPath(),
						"JKS", "changeit"));

		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(getClass().getClassLoader().getSystemResource("Gesperrt.p12").getPath(), "PKCS12","12345678"),
				"Gesperrt", "12345678");
		
		Verifier verifier = new Verifier();
		try {
			verifier.verifyCertificate(trustedRootStore, userKeyStorage.getUserCertificate());
			verifier.verifyCertificateCRLs(crlStorage, userKeyStorage.getUserCertificate());
		} catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
			throw e;
		}
	}

	@Test
	public void verifyCertWithUrlCRL() throws Exception {
		CRLStorage crlStorage = new CRLStorage();
		crlStorage.addCRLSource(
				CertUtil.loadCertificate(getClass().getClassLoader().getSystemResource("Client1_ca.cer").getPath()).hashCode(), 
				getClass().getClassLoader().getSystemResource("AdminCA1.crl").getPath());

		TrustedStore trustedRootStore = new TrustedStore(
				new FileStorage(
						getClass().getClassLoader().getSystemResource("trustedroots.jks").getPath(),
						"JKS", "12345678"));
		
		for(java.security.cert.X509Certificate cert: trustedRootStore.getCertificates()) {
			if(cert.getSubjectDN().getName().equalsIgnoreCase("CN=TC TrustCenter Class 2 CA II, OU=TC TrustCenter Class 2 CA, O=TC TrustCenter GmbH, C=DE")) {		
				Verifier verifier = new Verifier();
				try {
					verifier.verifyCertificateCRLs(crlStorage, cert);
				} catch(Exception e) {
					System.out.println(e.getLocalizedMessage());
					throw e;
				}
				break;
			}
		}
	}


	
}
