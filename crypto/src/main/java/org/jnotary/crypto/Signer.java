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

import java.security.Security;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class Signer {

	private String algorithm = "SHA1withRSA";
	
    static {
    	Security.addProvider(new BouncyCastleProvider());
    }
    
    public class Parameters {
    	private boolean detached = false;
    	private boolean addSignerSertificate = true;
    	
    	public Parameters() {    		
    	}
    	
    	public boolean isDetached() {
			return detached;
		}
		public void setDetached(boolean detached) {
			this.detached = detached;
		}
		public boolean isAddSignerSertificate() {
			return addSignerSertificate;
		}
		public void setAddSignerSertificate(boolean addSignerSertificate) {
			this.addSignerSertificate = addSignerSertificate;
		}
    }
    
    public Signer(String algorithm) {
    	this.algorithm = algorithm;
    }
    
    public Parameters getDefaultParameters() {
    	return new Parameters();
    }
    
	public byte[] sign(UserKeyStore myStorage, byte[] content) throws Exception {
		return sign(myStorage, content, getDefaultParameters());
	}
	
	public byte[] sign(UserKeyStore myStorage, byte[] content, Parameters parameters) throws Exception {

		CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
		ContentSigner sha1Signer = new JcaContentSignerBuilder(algorithm).
				setProvider("BC").build(myStorage.getPrivateKey());

		gen.addSignerInfoGenerator(
				new JcaSignerInfoGeneratorBuilder(
						new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
				.build(sha1Signer, myStorage.getUserCertificate()));

		if(parameters.isAddSignerSertificate())
			gen.addCertificates(myStorage.getCertStore());

		CMSTypedData msg = new CMSProcessableByteArray(content);
		CMSSignedData sigData = gen.generate(msg, !parameters.isDetached());
		return sigData.getEncoded();
	}

}
