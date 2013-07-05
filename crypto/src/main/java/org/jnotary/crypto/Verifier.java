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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.NamingException;

import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

public class Verifier {

	java.security.cert.X509Certificate trustedIssuer = null;
	
    public class VerifyResult {
        private final List<SignerInformation> signers;
        private final byte[] content;
        private final Map<SignerId, java.security.cert.X509Certificate> certificates;
        
        public VerifyResult(byte[] content, List<SignerInformation> signers,  Map<SignerId, java.security.cert.X509Certificate> certificates) {
            this.content = content;
            this.signers = signers;
            this.certificates = certificates;
        }
        
        public java.security.cert.X509Certificate getSignerCertificate(SignerId signerId) {
        	return certificates.get(signerId);
        }

		public List<SignerInformation> getSigners() {
			return signers;
		}

		public byte[] getContent() {
			return content;
		}
    }
	
    static {
    	Security.addProvider(new BouncyCastleProvider());
    }
    
    
	public VerifyResult verifySignature(byte[] signedData, TrustedStore trustedUserCertificateStore) throws Exception {
		CMSSignedData sdata = new CMSSignedData(signedData);
		Store                   certStore = sdata.getCertificates();
		SignerInformationStore  signersStore = sdata.getSignerInfos();
		Collection              signers = signersStore.getSigners();
		Iterator                it = signers.iterator();
		
		final Map<SignerId, java.security.cert.X509Certificate> certificates = new HashMap<SignerId, java.security.cert.X509Certificate>();
		
		List<SignerInformation> signerInfoList = new ArrayList<SignerInformation>();
		while (it.hasNext())
		{
			SignerInformation   signer = (SignerInformation)it.next();
			signerInfoList.add(signer);
			X509CertificateHolder cert = getCertificateHolder(
					trustedUserCertificateStore, certStore, signer);
			ByteArrayInputStream certBais = new ByteArrayInputStream(cert.getEncoded());
			java.security.cert.X509Certificate x509cert = 
					(java.security.cert.X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(certBais);
			certificates.put(signer.getSID(), x509cert);
			
			verifyDate(signer, x509cert);
			
			if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert)))
				throw new Exception("Signature verification failed for " + cert.getSubject().toString());
		}
		CMSTypedData ctd = sdata.getSignedContent();
		if(ctd == null)
			throw new Exception("Data not exists");
		return new VerifyResult((byte[])ctd.getContent(), signerInfoList, certificates);
	}

	private X509CertificateHolder getCertificateHolder(
			TrustedStore trustedUserCertificateStore, Store certStore,
			SignerInformation signer) throws Exception {
		Collection          certCollection = certStore.getMatches(signer.getSID());
		if(certCollection.isEmpty() && trustedUserCertificateStore != null)
			certCollection = trustedUserCertificateStore.getCertStore().getMatches(signer.getSID());
		if(certCollection.isEmpty())
			throw new Exception("Certificate not found for " + signer.getSID().toString());
			
		Iterator              certIt = certCollection.iterator();
		X509CertificateHolder cert = (X509CertificateHolder)certIt.next();
		return cert;
	}

	private void verifyDate(SignerInformation signer, java.security.cert.X509Certificate cert)
			throws Exception {
		final Attribute attribute = (Attribute)signer.getSignedAttributes().getAll(CMSAttributes.signingTime).get(0);
		Date date = null;
		if(attribute != null)
			date = Time.getInstance(attribute.getAttrValues().getObjectAt(0)).getDate();

		if(date != null) {
			Date notAfter = cert.getNotAfter();
			Date notBefore = cert.getNotBefore();
			if(notAfter != null && date.compareTo(notAfter) > 0) {
				throw new Exception("Signature verification failed (bad signing time) for " + cert.getSubjectDN().toString());					
			}
			if(notBefore != null && date.compareTo(notBefore) < 0) {
				throw new Exception("Signature verification failed (bad signing time) for " + cert.getSubjectDN().toString());					
			}
		}
	}
	
	public void verifyCertificate(TrustedStore rootStore, java.security.cert.X509Certificate certificate) throws Exception {

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
	    CertPath cp = cf.generateCertPath(Collections.singletonList(certificate));

	    CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

	    PKIXParameters params = new PKIXParameters(rootStore.getKeyStore());
	    params.setRevocationEnabled(false);
	    PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp, params);	
	    TrustAnchor anc = result.getTrustAnchor();
	    trustedIssuer = anc.getTrustedCert();
	}


	public void verifyCertificateCRLs(CRLStorage crlStorage, java.security.cert.X509Certificate cert) throws CertificateVerificationException, CertificateException, CRLException, IOException, NamingException {
		List<X509CRL> crls = crlStorage.get(cert);
		if(crls.isEmpty())
			crls = getByIssuer(crlStorage, cert);
		
		if(crls.isEmpty())
			throw new CertificateVerificationException("CRL is not found for " + cert.getSubjectDN().toString());

		
		for (X509CRL crl : crls) {
			if (crl.isRevoked(cert)) {
				throw new CertificateVerificationException(
						"The certificate is revoked by CRL: " + cert.getSubjectDN());
			}
		}

	}
	
	public List<X509CRL> getByIssuer(CRLStorage crlStorage,java.security.cert.X509Certificate userCertificate) throws IOException, CertificateException, CRLException, NamingException, CertificateVerificationException {

		if(trustedIssuer == null)
			throw new CertificateVerificationException("You must call verifyCertificate before");

		List<X509CRL> crlList = new LinkedList<X509CRL>();
		X509CRL crl = crlStorage.getByIssuer(trustedIssuer);
		if(crl != null)
			crlList.add(crl);
		return crlList;		
	}
	
}
