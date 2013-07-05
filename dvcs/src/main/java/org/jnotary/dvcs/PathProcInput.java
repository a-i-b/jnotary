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

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBoolean;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.PolicyInformation;


/*
PathProcInput ::= SEQUENCE {
    acceptablePolicySet          SEQUENCE SIZE (1..MAX) OF
                                    PolicyInformation,
    inhibitPolicyMapping         BOOLEAN DEFAULT FALSE,
    explicitPolicyReqd           BOOLEAN DEFAULT FALSE
}
*/

public class PathProcInput extends ASN1Object {
	PolicyInformation[] acceptablePolicySet;	// SEQUENCE SIZE (1..MAX) OF PolicyInformation
	DERBoolean	inhibitPolicyMapping = DERBoolean.getInstance(false);	// BOOLEAN DEFAULT FALSE
	DERBoolean	explicitPolicyReqd = DERBoolean.getInstance(false);		// BOOLEAN DEFAULT FALSE
	
	public PathProcInput(PolicyInformation[] acceptablePolicySet, boolean inhibitPolicyMapping, boolean explicitPolicyReqd) {
		this.acceptablePolicySet = acceptablePolicySet;
		this.inhibitPolicyMapping = DERBoolean.getInstance(inhibitPolicyMapping);
		this.explicitPolicyReqd = DERBoolean.getInstance(explicitPolicyReqd);
	}

	public PathProcInput(PolicyInformation[] acceptablePolicySet) {
		this.acceptablePolicySet = acceptablePolicySet;
	}
	
	public DERBoolean getInhibitPolicyMapping() {
		return inhibitPolicyMapping;
	}
	
	public DERBoolean getExplicitPolicyReqd() {
		return explicitPolicyReqd;
	}
	
	private PathProcInput(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
		ASN1Sequence policies = DERSequence.getInstance(e.nextElement());
		acceptablePolicySet = new PolicyInformation[policies.size()];
		for(int i = 0; i < policies.size(); ++i){
			acceptablePolicySet[i] = PolicyInformation.getInstance(policies.getObjectAt(i));
		}
        inhibitPolicyMapping = DERBoolean.getInstance(e.nextElement());
        explicitPolicyReqd = DERBoolean.getInstance(e.nextElement());
    }
	
    public static PathProcInput getInstance(Object obj)
    {
        if (obj instanceof PathProcInput)
        {
            return (PathProcInput)obj;
        }
        else if (obj != null)
        {
            return new PathProcInput(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

	@Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        ASN1EncodableVector seq = new ASN1EncodableVector();
        for (int i = 0; i < acceptablePolicySet.length; i++) 
        {
        	seq.add(acceptablePolicySet[i].toASN1Primitive());
        }                                    	
    	v.add(new DERSequence(seq));
    
        v.add(inhibitPolicyMapping);
        v.add(explicitPolicyReqd);
        
        return new DERSequence(v);
	}	
	
}
