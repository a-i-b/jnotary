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
package org.jnotary.service.util;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.cms.SignerInformation;
import org.jnotary.crypto.CRLStorage;
import org.jnotary.dvcs.util.DVCSException;
import org.jnotary.crypto.Signer;
import org.jnotary.crypto.TrustedStore;
import org.jnotary.crypto.Verifier;
import org.jnotary.crypto.Verifier.VerifyResult;

@Stateless
public class CryptoService {

	@Inject
	IGlobalResources globalResources;
	@Inject
	private Logger log;
	
	private Verifier verifier = new Verifier();		
	private Signer signer;
		
	@PostConstruct
	public void initialize() throws IOException {
		signer = new Signer(globalResources.getServiceConfig().getSignerAlgorithm());
	}
	
	public VerifyResult verifySignature(byte[] signedData) throws DVCSException {
		
		TrustedStore trustedUsers = null; //globalResources.getTrustedUsers();
		try {
			return verifier.verifySignature(signedData, trustedUsers);
		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.badMessageCheck);
		}
	}

	public void verifyCerificates(VerifyResult result, boolean checkCRL) throws DVCSException {
				
		for(SignerInformation signerInfo: result.getSigners()) {
			X509Certificate cert = result.getSignerCertificate(signerInfo.getSID());
			verifyCerificate(cert, checkCRL);
		}
	}
	
	public void verifyCerificate(X509Certificate certificate, boolean checkCRL) throws DVCSException {
		
		CRLStorage crlStorage = globalResources.getCrlStorage();		
		TrustedStore trustedRoots = globalResources.getTrustedRootStore();
		
		try {
			verifier.verifyCertificate(trustedRoots, certificate);
		} catch(Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.badCertId);
		}
		if(checkCRL) {
			try {
				verifier.verifyCertificateCRLs(crlStorage, certificate);
			} catch(Exception e) {
				log.info(e.getLocalizedMessage());
				throw new DVCSException(PKIStatus.REJECTION, 
						e.getLocalizedMessage(),
						PKIFailureInfo.certRevoked);
			}
		}
	}
	
	public void verifySignedDocument(byte[] signedData) throws DVCSException {
		
		VerifyResult result = verifySignature(signedData);		
		verifyCerificates(result, true);		
	}
	
	public byte[] sign(byte[] data) throws DVCSException {

		Signer.Parameters signerParameters = signer.getDefaultParameters();
		signerParameters.setAddSignerSertificate(globalResources.getServiceConfig().isAddSignCertificate());
		signerParameters.setDetached(false);

        try {
			return signer.sign(globalResources.getMyKeyStore(), data, signerParameters);
		} catch (Exception e) {
			throw new DVCSException(PKIStatus.REJECTION, 
					e.getLocalizedMessage(),
					PKIFailureInfo.systemFailure);
		}
	}
}
