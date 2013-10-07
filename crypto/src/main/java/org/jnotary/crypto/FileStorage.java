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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class FileStorage implements IKeyStorage{
	
	private String storePassword;
	
	private KeyStore keystore;
    
	public FileStorage(String pathToKeyStore, String storeType, String password) throws Exception {
		storePassword = password;
		keystore = KeyStore.getInstance(storeType);
        InputStream is = null;
        try {
    		File f = new File(pathToKeyStore);
    		String absolutePath = f.getAbsolutePath();
	        is = new FileInputStream(absolutePath);
			keystore.load(is, storePassword.toCharArray());
        } finally {
        	if(is != null)
        		is.close();
        }
	}

	public KeyStore getKeyStore() {
		return keystore;
	}
	
}
