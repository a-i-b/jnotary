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

import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.util.Store;

public class TrustedStore {
	
	private List<java.security.cert.X509Certificate> certificates = null;
	private Store store = null;
	private KeyStore keyStore = null;

	public TrustedStore(IKeyStorage storage) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, CertificateEncodingException {
		
		certificates = new ArrayList<java.security.cert.X509Certificate>();
		keyStore = storage.getKeyStore();
		  // List the aliases
	    Enumeration aliases = keyStore.aliases();
	    for (; aliases.hasMoreElements(); ) {
	        String alias = (String)aliases.nextElement();
	        // Does alias refer to a trusted certificate?
	        if(keyStore.isCertificateEntry(alias)) {
	    	    KeyStore.TrustedCertificateEntry pkEntry = (KeyStore.TrustedCertificateEntry)storage.getKeyStore().getEntry(alias, null); 
//	    		    	new KeyStore.PasswordProtection(aliasPassword.toCharArray()));	
	    	    certificates.add((java.security.cert.X509Certificate)pkEntry.getTrustedCertificate());
	        }
	    }
	    store = new JcaCertStore(certificates);		
	}
	
    public Store getCertStore() {   	
    	return store;
    }
    
	public KeyStore getKeyStore() {
		return keyStore;
	}

	public List<java.security.cert.X509Certificate> getCertificates() {
		return certificates;
	}    
}
