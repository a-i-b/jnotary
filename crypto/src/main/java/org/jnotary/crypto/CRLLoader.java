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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;

/**
 * Class that verifies CRLs for given X509 certificate. Extracts the CRL
 * distribution points from the certificate (if available) and checks the
 * certificate revocation status against the CRLs coming from the
 * distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
 * 
 * Thanks to Svetlin Nakov
 */
public class CRLLoader {


	/**
	 * Downloads CRL from given URL. Supports http, https, ftp and ldap based URLs.
	 * @throws IOException 
	 * @throws CRLException 
	 * @throws CertificateException 
	 * @throws MalformedURLException 
	 * @throws CertificateVerificationException 
	 * @throws NamingException 
	 */
	public static X509CRL loadCRL(final String crlURL) throws MalformedURLException, CertificateException, CRLException, IOException, NamingException, CertificateVerificationException {
		if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
				|| crlURL.startsWith("ftp://")) {
			X509CRL crl = downloadCRLFromWeb(crlURL);
			return crl;
		} else if (crlURL.startsWith("ldap://")) {
			X509CRL crl = downloadCRLFromLDAP(crlURL);
			return crl;
		} else {
			return CRLLoader.loadCRLFromFile(crlURL);
		}
	}

	public static X509CRL loadCRLFromFile(String path)
			throws MalformedURLException, IOException, CertificateException,
			CRLException {
		DataInputStream crlStream = null;
		try {
			crlStream = new DataInputStream(new FileInputStream(new File(path)));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
			return crl;
		} finally {
			crlStream.close();
		}
	}
	
	/**
	 * Downloads a CRL from given LDAP url, e.g.
	 * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
	 */
	private static X509CRL downloadCRLFromLDAP(String ldapURL) 
			throws CertificateException, NamingException, CRLException, 
			CertificateVerificationException {
		Hashtable<String , String> env = new Hashtable<String , String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, 
				"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        DirContext ctx = new InitialDirContext(env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        byte[] val = (byte[])aval.get();
        if ((val == null) || (val.length == 0)) {
        	throw new CertificateVerificationException(
        			"Can not download CRL from: " + ldapURL);
        } else {
        	InputStream inStream = new ByteArrayInputStream(val);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
        	X509CRL crl = (X509CRL)cf.generateCRL(inStream);
        	return crl;
        }
	}
	
	/**
	 * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
	 * http://crl.infonotary.com/crl/identity-ca.crl
	 */
	private static X509CRL downloadCRLFromWeb(String crlURL)
			throws MalformedURLException, IOException, CertificateException,
			CRLException {
		URL url = new URL(crlURL);
		InputStream crlStream = url.openStream();
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509CRL crl = (X509CRL) cf.generateCRL(crlStream);
			return crl;
		} finally {
			crlStream.close();
		}
	}

	/**
	 * Extracts all CRL distribution point URLs from the "CRL Distribution Point"
	 * extension in a X.509 certificate. If CRL distribution point extension is
	 * unavailable, returns an empty list. 
	 */
	public static List<String> getCrlDistributionPoints(X509Certificate cert) throws CertificateParsingException, IOException {
		byte[] crldpExt = cert.getExtensionValue(X509Extension.cRLDistributionPoints.getId());
		if (crldpExt == null) {
			return Collections.emptyList();
		}
		ASN1InputStream oAsnInStream = null;
		ASN1InputStream oAsnInStream2 = null;
		List<String> crlUrls = new ArrayList<String>();

		try {
			oAsnInStream = new ASN1InputStream(
					new ByteArrayInputStream(crldpExt));
			ASN1Primitive derObjCrlDP = oAsnInStream.readObject();
			DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
			byte[] crldpExtOctets = dosCrlDP.getOctets();
			oAsnInStream2 = new ASN1InputStream(
					new ByteArrayInputStream(crldpExtOctets));
			ASN1Primitive derObj2 = oAsnInStream2.readObject();
			CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
			for (DistributionPoint dp : distPoint.getDistributionPoints()) {
	            DistributionPointName dpn = dp.getDistributionPoint();
	            // Look for URIs in fullName
	            if (dpn != null) {
	                if (dpn.getType() == DistributionPointName.FULL_NAME) {
	                    GeneralName[] genNames = GeneralNames.getInstance(
	                        dpn.getName()).getNames();
	                    // Look for an URI
	                    for (int j = 0; j < genNames.length; j++) {
	                        if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
	                            String url = DERIA5String.getInstance(
	                                genNames[j].getName()).getString();
	                            crlUrls.add(url);
	                        }
	                    }
	                }
	            }
			}
		} finally {
			if(oAsnInStream != null)
				oAsnInStream.close();
			if(oAsnInStream2 != null)
				oAsnInStream2.close();
		}
		return crlUrls;
	}

}
