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

import java.io.IOException;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;


/*
DVCSResponse ::= CHOICE
{
    dvCertInfo         DVCSCertInfo ,
    dvErrorNote        [0] DVCSErrorNotice
}
*/

public class DVCSResponse extends ASN1Object implements ASN1Choice {
	DVCSCertInfo	dvCertInfo = null;		// DVCSCertInfo
	DVCSErrorNotice	dvErrorNote = null;		// [0] DVCSErrorNotice
	
	public DVCSResponse(DVCSCertInfo dvCertInfo) {
		this.dvCertInfo = dvCertInfo;
	}

	public DVCSResponse(DVCSErrorNotice dvErrorNote) {
		this.dvErrorNote = dvErrorNote;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
		if(dvCertInfo != null)
			return dvCertInfo.toASN1Primitive();
		if(dvErrorNote != null)
			return new DERTaggedObject(true, 0, dvErrorNote).toASN1Primitive();
		return null;
	}
	
	public DVCSErrorNotice getDvErrorNote() {
		return dvErrorNote;
	}
	
	public DVCSCertInfo getDvCertInfo() {
		return dvCertInfo;
	}
	
	private DVCSResponse(Object obj) {
        if(obj instanceof ASN1TaggedObject) {
        	ASN1TaggedObject tagObj = (ASN1TaggedObject)obj;
        	this.dvErrorNote = DVCSErrorNotice.getInstance(tagObj.getObject());
        } else {
        	this.dvCertInfo = DVCSCertInfo.getInstance(obj);
        }
	}
	
    public static DVCSResponse getInstance(Object obj) throws IOException
    {
        if (obj instanceof DVCSResponse)
        {
            return (DVCSResponse)obj;
        }
        else if (obj instanceof byte[])
        {
            return new DVCSResponse(ASN1Primitive.fromByteArray((byte[])obj));
        }

        return null;
    }	
}
