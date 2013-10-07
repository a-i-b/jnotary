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
import java.io.OutputStream;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class Hasher {
	public static byte[] makeHash(ASN1ObjectIdentifier algorithm, byte[] data) throws OperatorCreationException, IOException {
		DigestCalculatorProvider calcProvider = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
		DigestCalculator calc = calcProvider.get(new AlgorithmIdentifier(algorithm));
		OutputStream stream = null;
		try {
			stream = calc.getOutputStream();
			stream.write(data);		
		} finally {
			stream.close();
		}
		return calc.getDigest();			
	}
}
