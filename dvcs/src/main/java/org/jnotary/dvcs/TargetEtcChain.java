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

import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBoolean;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.PolicyInformation;

/*
TargetEtcChain ::= SEQUENCE {
    target                       CertEtcToken,
    chain                        SEQUENCE SIZE (1..MAX) OF
                                    CertEtcToken OPTIONAL,
    pathProcInput                [0] PathProcInput OPTIONAL
}
*/

public class TargetEtcChain extends ASN1Object {
	CertEtcToken		target;	// CertEtcToken
	CertEtcToken[]		chain;	// SEQUENCE SIZE (1..MAX) OF CertEtcToken OPTIONAL
	PathProcInput		pathProcInput;	// [0] PathProcInput OPTIONAL
	
	public TargetEtcChain(CertEtcToken target, CertEtcToken[] chain, PathProcInput pathProcInput) {
		this.target = target;
		this.chain = chain;
		this.pathProcInput = pathProcInput;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        v.add(target);
        if(chain != null) {
	        ASN1EncodableVector seq = new ASN1EncodableVector();
	        for (int i = 0; i < chain.length; i++) 
	        {
	        	seq.add(chain[i].toASN1Primitive());
	        }                                    	
	    	v.add(new DERSequence(seq));
        }
        if(pathProcInput != null)
        	v.add(new DERTaggedObject(true, 0, pathProcInput));
        
        return new DERSequence(v);
	}
	
	private TargetEtcChain(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        
        target = CertEtcToken.getInstance(e.nextElement());
        
        if(!e.hasMoreElements())
        	return;
       	Object obj = e.nextElement();
        
        if(!(obj instanceof ASN1TaggedObject)) {
			ASN1Sequence policies = DERSequence.getInstance(obj);
			chain = new CertEtcToken[policies.size()];
			for(int i = 0; i < policies.size(); ++i){
				chain[i] = CertEtcToken.getInstance(policies.getObjectAt(i));
			}

			if(!e.hasMoreElements())
	        	return;
	       	obj = e.nextElement();
	    }
        
        ASN1TaggedObject tagObj = (ASN1TaggedObject)obj;
		pathProcInput = PathProcInput.getInstance(tagObj.getObject());
    }
	
    public static TargetEtcChain getInstance(Object obj)
    {
        if (obj instanceof TargetEtcChain)
        {
            return (TargetEtcChain)obj;
        }
        else if (obj != null)
        {
            return new TargetEtcChain(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

	public CertEtcToken getTarget() {
		return target;
	}
}
