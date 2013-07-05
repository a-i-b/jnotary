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

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.x509.GeneralName;

/*
DVCSErrorNotice ::= SEQUENCE {
    transactionStatus           PKIStatusInfo ,
    transactionIdentifier       GeneralName OPTIONAL
}
*/
public class DVCSErrorNotice extends ASN1Object {
	PKIStatusInfo	transactionStatus;	// PKIStatusInfo
	GeneralName transactionIdentifier = null;	// GeneralName OPTIONAL
	
	public DVCSErrorNotice(PKIStatusInfo transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        v.add(transactionStatus);
        if(transactionIdentifier != null)
        	v.add(transactionIdentifier);

        return new DERSequence(v);
	}

	private DVCSErrorNotice(ASN1Sequence seq) {
        Enumeration e = seq.getObjects();
        transactionStatus = PKIStatusInfo.getInstance(e.nextElement());
        if(e.hasMoreElements())
        	transactionIdentifier = GeneralName.getInstance(e.nextElement());
    }

	public static DVCSErrorNotice getInstance(Object obj) {
        if (obj instanceof DVCSErrorNotice)
        {
            return (DVCSErrorNotice)obj;
        }
        else if (obj != null)
        {
            return new DVCSErrorNotice(ASN1Sequence.getInstance(obj));
        }

        return null;
       }

	public GeneralName getTransactionIdentifier() {
		return transactionIdentifier;
	}

	public void setTransactionIdentifier(GeneralName transactionIdentifier) {
		this.transactionIdentifier = transactionIdentifier;
	}

	public PKIStatusInfo getTransactionStatus() {
		return transactionStatus;
	}
}
