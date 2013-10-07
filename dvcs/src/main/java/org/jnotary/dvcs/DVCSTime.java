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

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.cms.ContentInfo;

/*
 DVCSTime ::= CHOICE  {
 genTime                      GeneralizedTime,
 timeStampToken               ContentInfo }
 */

public class DVCSTime extends ASN1Object implements ASN1Choice {
	private DERGeneralizedTime genTime = null; // GeneralizedTime
	private ContentInfo timeStampToken = null; // ContentInfo

	public DVCSTime(DERGeneralizedTime genTime){
		this.genTime = genTime;
	}

	public DVCSTime(ContentInfo timeStampToken){
		this.timeStampToken = timeStampToken;
	}
	
	@Override
	public ASN1Primitive toASN1Primitive() {
		if (genTime != null)
			return genTime.toASN1Primitive();
		if (timeStampToken != null)
			return timeStampToken.toASN1Primitive();
		return null;
	}

	private DVCSTime(Object obj) {
		if (obj instanceof ASN1GeneralizedTime)
			genTime = DERGeneralizedTime.getInstance(obj);
		else
			timeStampToken = ContentInfo.getInstance(obj);
	}

	public static DVCSTime getInstance(Object obj) {
        if (obj instanceof DVCSTime)
            return (DVCSTime)obj;
        else if (obj != null)
            return new DVCSTime(obj);
        
		return null;
	}
	
	public DERGeneralizedTime getGenTime() {
		return genTime;
	}
	
	public ContentInfo getTimeStampToken() {
		return timeStampToken;
	}
}
