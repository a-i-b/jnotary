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
package org.jnotary.service.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.jcajce.provider.util.DigestFactory;
import org.jnotary.service.model.Configuration;

public class ServiceConfig {
	private ASN1ObjectIdentifier hashAlgorithm = NISTObjectIdentifiers.id_sha256; 
	private String signerAlgorithm = "SHA1withRSA";
	private boolean verifyCRL = false;
	private boolean addSignCertificate = true;
	private List<Boolean> allowedServices;
	
	public void load(Configuration configuration) throws IOException {
		signerAlgorithm = configuration.getSignatureAlgorithm();
		hashAlgorithm = DigestFactory.getOID(configuration.getHashAlgorithm());
		verifyCRL = configuration.getVerifyCRL();
		addSignCertificate = configuration.getAddSertificateToSignature();
		allowedServices = new ArrayList<Boolean>(4);
		//Beachten die Reihenfolge!!!
		allowedServices.add(configuration.getCpdAllowed());
		allowedServices.add(configuration.getVsdAllowed());
		allowedServices.add(configuration.getVpkcAllowed());
		allowedServices.add(configuration.getCcpdAllowed());
	}

	public ASN1ObjectIdentifier getHashAlgorithm() {
		return hashAlgorithm;
	}

	public String getSignerAlgorithm() {
		return signerAlgorithm;
	}

	public boolean isVerifyCRL() {
		return verifyCRL;
	}

	public boolean isAddSignCertificate() {
		return addSignCertificate;
	}

	public boolean asAllowed(int service) {
		if(service > allowedServices.size())
			return false;
		return allowedServices.get(service-1);
	}
}
