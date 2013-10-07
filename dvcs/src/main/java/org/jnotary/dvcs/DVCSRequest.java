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
package org.jnotary.dvcs;

import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;

/*
DVCSRequest ::= SEQUENCE  {
    requestInformation         DVCSRequestInformation,
    data                       Data,
    transactionIdentifier      GeneralName OPTIONAL
}
*/

public class DVCSRequest extends ASN1Object{
	private DVCSRequestInformation	requestInformation = null;		// DVCSRequestInformation
	private Data					data = null;					// Data
	private GeneralName				transactionIdentifier = null;	// GeneralName OPTIONAL
	
	public DVCSRequest(DVCSRequestInformation requestInformation, Data data) {
		this.requestInformation = requestInformation;
		this.data = data;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        v.add(requestInformation);
        v.add(data);

        if (transactionIdentifier != null)
            v.add(transactionIdentifier);

        return new DERSequence(v);
	}

	public GeneralName getTransactionIdentifier() {
		return transactionIdentifier;
	}

	public void setTransactionIdentifier(GeneralName transactionIdentifier) {
		this.transactionIdentifier = transactionIdentifier;
	}
	
    public DVCSRequestInformation getRequestInformation() {
		return requestInformation;
	}

	public Data getData() {
		return data;
	}

	@SuppressWarnings("rawtypes")
	private DVCSRequest(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        requestInformation = DVCSRequestInformation.getInstance(e.nextElement());
        data = Data.getInstance(requestInformation.getService(), e.nextElement());
        if(e.hasMoreElements()) {
        	transactionIdentifier = GeneralName.getInstance(e.nextElement());
        }
    }
    
    public static DVCSRequest getInstance(Object obj)
    {
        if (obj instanceof DVCSRequest)
        {
            return (DVCSRequest)obj;
        }
        else if (obj != null)
        {
            return new DVCSRequest(ASN1Sequence.getInstance(obj));
        }

        return null;
    }	
}
