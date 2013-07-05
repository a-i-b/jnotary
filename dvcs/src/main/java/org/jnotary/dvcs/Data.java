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
package org.jnotary.dvcs;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.DigestInfo;

/*
Data ::= CHOICE {
    message           OCTET STRING ,
    messageImprint    DigestInfo,
    certs             SEQUENCE SIZE (1..MAX) OF
                          TargetEtcChain
}
*/
public class Data extends ASN1Object implements ASN1Choice {
	private DEROctetString		message = null;			// OCTET STRING
	private DigestInfo			messageImprint = null;	// DigestInfo,
	private TargetEtcChain[]	certs = null;			// SEQUENCE SIZE (1..MAX) OF TargetEtcChain
    
	public Data(DEROctetString message){
		this.message = message;
	}

	public Data(DigestInfo messageImprint){
		this.messageImprint = messageImprint;
	}

	public Data(TargetEtcChain[] certs){
		this.certs = certs;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
		if(message != null)
			return message.toASN1Primitive();
		if(messageImprint != null)
			return messageImprint.toASN1Primitive();
		if(certs != null) {
	        ASN1EncodableVector seq = new ASN1EncodableVector();
	        for (int i = 0; i < certs.length; i++) 
	        {
	        	seq.add(certs[i]);
	        }                            
	        return new DERSequence(seq).toASN1Primitive();			
		}
		return null;
	}
	
	private TargetEtcChain[] copyChain(TargetEtcChain[] src){
		TargetEtcChain[] res = new TargetEtcChain[src.length];
		for(int i = 0; i < src.length; ++i)
			res[i] = src[i];
		return res;
	}
	
	public DEROctetString getMessage() {
		return message;
	}

	public DigestInfo getMessageImprint() {
		return messageImprint;
	}

	public TargetEtcChain[] getCerts() {
		return certs;
	}
	
	private Data(ASN1Sequence seq) {
		certs = new TargetEtcChain[seq.size()];
		for(int i = 0; i < seq.size(); ++i){
			certs[i] = TargetEtcChain.getInstance(seq.getObjectAt(i));
		}
	}

	public static Data getInstance(int serviceType, Object obj) {
		switch(serviceType) {
		case ServiceType.CPD:
		case ServiceType.VSD:
			return new Data((DEROctetString)DEROctetString.getInstance(obj));
		case ServiceType.CCPD:
			return new Data(DigestInfo.getInstance(obj));
		case ServiceType.VPKC:
			return new Data(DERSequence.getInstance(obj));
		}
        
		return null;
	}	
}
