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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class ClientCryptoConfig {
	private String keyPath;
	private String containerType;
	private String alias;
	private String storePassword;
	private String aliasPassword;
	private AlgorithmIdentifier hashAlgorithm = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256); 
	
	public void load(String configPath) throws Exception {
		Properties clientProperties = new Properties();
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(configPath));
			clientProperties.load(stream);
			keyPath = clientProperties.getProperty("store-path");
			containerType = clientProperties.getProperty("store-type");
			alias = clientProperties.getProperty("alias");
			storePassword = clientProperties.getProperty("store-password");
			aliasPassword = clientProperties.getProperty("alias-password");
		} catch (Exception e) {
			throw new Exception("Can't read key store properties: " + e.getLocalizedMessage());
		} finally {
			if(stream != null)
				stream.close();
		}
	}

	public String getKeyPath() {
		return keyPath;
	}

	public String getContainerType() {
		return containerType;
	}

	public String getAlias() {
		return alias;
	}

	public String getStorePassword() {
		return storePassword;
	}

	public String getAliasPassword() {
		return aliasPassword;
	}

	public AlgorithmIdentifier getHashAlgorithm() {
		return hashAlgorithm;
	}
}
