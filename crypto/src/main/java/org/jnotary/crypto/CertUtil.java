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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class CertUtil {
	public static X509Certificate loadCertificate(String path) throws CertificateException, NoSuchProviderException, FileNotFoundException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
		return (X509Certificate) factory.generateCertificate(new FileInputStream(path));  
	}
}
