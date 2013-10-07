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
package org.jnotary.crypto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

public class CRLStorage {
	Map<String, X509CRL> crlCashe = Collections.synchronizedMap(new HashMap<String, X509CRL>()); 
	Map<Integer, String> crlSources = Collections.synchronizedMap(new HashMap<Integer, String>()); 
		
	public void addCRLSource(Integer issuerHash, String path) throws MalformedURLException, CertificateException, CRLException, IOException, NamingException, CertificateVerificationException {
		crlSources.put(issuerHash, path);
	}

	public List<X509CRL> get(java.security.cert.X509Certificate cert) throws IOException, CertificateException, CRLException, NamingException, CertificateVerificationException {
		List<X509CRL> crlList = new LinkedList<X509CRL>();
		List<String> crlDistPoints = CRLLoader.getCrlDistributionPoints(cert);
				
		for (String crlDP : crlDistPoints) {
			try {
				X509CRL crl = getByUrl(crlDP);
				if(crl == null)
					continue;

				if(!crlList.contains(crl))
					crlList.add(crl);

			} catch(Exception e){}
		}
		return crlList;		
	}

	public X509CRL getByIssuer(java.security.cert.X509Certificate issuer) throws IOException, CertificateException, CRLException, NamingException, CertificateVerificationException {
		try {
			String crlDP = crlSources.get(issuer.hashCode());		
			X509CRL crl = getByUrl(crlDP);
			return crl;
		} catch(Exception e){			
		}
		return null;
	}
	
	private X509CRL getByUrl(String url) throws MalformedURLException, CertificateException, CRLException, IOException, NamingException, CertificateVerificationException {
		
		X509CRL crl = crlCashe.get(url);
		
		if(crl == null || crl.getNextUpdate().before(new Date())) {
			X509CRL newCrl = CRLLoader.loadCRL(url);
			crl = newCrl == null ? crl : newCrl;
			if(crl == null)
				return null;

			crlCashe.put(url, crl);
		}
		if(crl.getNextUpdate().before(new Date()))
			System.out.println("CRL is too old. NextUpdate:" + crl.getNextUpdate().toString());
			
		return crl;		
	}
}
