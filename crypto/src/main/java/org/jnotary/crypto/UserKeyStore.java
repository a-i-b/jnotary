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
import java.security.PrivateKey;

import java.util.Arrays;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.util.Store;

public class UserKeyStore {

	private java.security.cert.Certificate[] chain = null;
	private PrivateKey myPrivateKey = null;
	private java.security.cert.Certificate myCert = null;
	
	public UserKeyStore(IKeyStorage storage, String alias, String aliasPassword) throws Exception {
		
	    KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)storage.getKeyStore().getEntry(alias, 
	    	new KeyStore.PasswordProtection(aliasPassword.toCharArray()));
	    if(pkEntry == null)
	    	throw new Exception("Private key entry isn't found");
	    
	    myPrivateKey = pkEntry.getPrivateKey();
	    chain = pkEntry.getCertificateChain();    
	    myCert = pkEntry.getCertificate();
	}
	
	public final PrivateKey getPrivateKey() {
		return myPrivateKey;
	}

	public final java.security.cert.Certificate[] getChain() {
    	return chain;
    }
    
    public final java.security.cert.X509Certificate getUserCertificate() {
    	return (java.security.cert.X509Certificate)myCert;
    }
	
    public final Store getCertStore() throws Exception {
		return new JcaCertStore(Arrays.asList(chain));
    }	
}
