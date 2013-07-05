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
package org.jnotary.crypto.util;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnotary.crypto.FileStorage;
import org.jnotary.crypto.Signer;
import org.jnotary.crypto.UserKeyStore;
import org.jnotary.crypto.Verifier;
import org.jnotary.crypto.Verifier.VerifyResult;
import org.jnotary.crypto.util.ClientCryptoConfig;

public class CryptoHelper {
	
	static final String signAlgorithm = "SHA1withRSA";
	
	public static byte[] sign(byte[] request, ClientCryptoConfig config) throws Exception {
		return sign(request, true, config);
	}
	public static byte[] sign(byte[] request, boolean addCert, ClientCryptoConfig config) throws Exception {
		Signer signer = new Signer(signAlgorithm);
		
		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(config.getKeyPath(), config.getContainerType(), config.getStorePassword()),
				config.getAlias(), config.getAliasPassword());

		Signer.Parameters signerParameters = signer.getDefaultParameters();
		signerParameters.setAddSignerSertificate(addCert);
		signerParameters.setDetached(false);
		return signer.sign(userKeyStorage, request, signerParameters);
	}
	
	public static byte[] removeSignature(byte[] signedData) throws Exception {
		Verifier verifier = new Verifier();		
		VerifyResult result = verifier.verifySignature(signedData, null);
		
		return result.getContent();		
	}  
	
	public static List<String> getSignatures() {
		Provider[] providers = Security.getProviders();
		List<String> signatures = new ArrayList<String>();
		for (int i = 0; i != providers.length; i++) {
		     Iterator it = providers[i].keySet().iterator();
		
		     while (it.hasNext()) {
		    	 String entry = (String) it.next();
		    	 if (entry.startsWith("Signature.")) {
		             signatures.add(entry.substring("Signature.".length()));
		    	 } 
		     }
		}		
		return signatures;
	}

	public static List<String> getMessageDigests() {
		Provider[] providers = Security.getProviders();
		List<String> messageDigests = new ArrayList<String>();
		for (int i = 0; i != providers.length; i++) {
		     Iterator it = providers[i].keySet().iterator();
		
		     while (it.hasNext()) {
		    	 String entry = (String) it.next();
		    	 if (entry.startsWith("MessageDigest.")) {
		    		 messageDigests.add(entry.substring("MessageDigest.".length()));
		    	 } 
		     }
		}		
		return messageDigests;
	}
	

}
