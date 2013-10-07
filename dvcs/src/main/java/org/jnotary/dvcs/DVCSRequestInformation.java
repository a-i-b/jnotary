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
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREnumerated;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;


/*
Nonce ::= Integer

DVCSRequestInformation ::= SEQUENCE  {
    version                      INTEGER DEFAULT 1 ,
    service                      ServiceType,
    nonce                        Nonce OPTIONAL,
    requestTime                  DVCSTime OPTIONAL,
    requester                    [0] GeneralNames OPTIONAL,
    requestPolicy                [1] PolicyInformation OPTIONAL,
    dvcs                         [2] GeneralNames OPTIONAL,
    dataLocations                [3] GeneralNames OPTIONAL,
    extensions                   [4] IMPLICIT Extensions OPTIONAL
}
*/

public class DVCSRequestInformation extends ASN1Object{

    private DERInteger			version = null;	// INTEGER DEFAULT 1
    private DEREnumerated		service;				// ServiceType
    private DERInteger			nonce = null;			// Nonce OPTIONAL
    private DVCSTime			requestTime = null;		// DVCSTime OPTIONAL
    private GeneralNames		requester = null;		// [0] GeneralNames OPTIONAL
    private PolicyInformation	requestPolicy = null;	// [1] PolicyInformation OPTIONAL,
    private GeneralNames		dvcs = null;			// [2] GeneralNames OPTIONAL,
    private GeneralNames		dataLocations = null;	// [3] GeneralNames OPTIONAL,
    private Extensions			extensions = null;		// [4] IMPLICIT Extensions OPTIONAL
    
    public DVCSRequestInformation(int service){
    	this.service = new DEREnumerated(service); 
    }

    public DVCSRequestInformation(int service, int version){
    	this.service = new DEREnumerated(service); 
    	this.version = new DERInteger(version);
    }
       
	@SuppressWarnings("rawtypes")
    private DVCSRequestInformation(ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();

        Object obj = e.nextElement();
        if(obj instanceof ASN1Integer) {
        	this.version = DERInteger.getInstance(obj);
        	obj = e.nextElement();
        }
        if(obj instanceof ASN1Enumerated)
        	this.service = DEREnumerated.getInstance(obj);
 
        for(;e.hasMoreElements();) {        	
        	obj = e.nextElement();
        	if (obj instanceof ASN1TaggedObject) {
        		ASN1TaggedObject tagObj = (ASN1TaggedObject)obj;
        		switch(tagObj.getTagNo()) {
        		case 0:
        			requester = GeneralNames.getInstance(tagObj.getObject());
        			break;
        		case 1:
        			requestPolicy = PolicyInformation.getInstance(tagObj.getObject());
        			break;
        		case 2:
        			dvcs = GeneralNames.getInstance(tagObj.getObject());
        			break;
        		case 3:
        			dataLocations = GeneralNames.getInstance(tagObj.getObject());
        			break;
        		case 4:
        			extensions = Extensions.getInstance(tagObj.getObject());
        			break;
        		}
        	} else if (obj instanceof ASN1Integer){
                this.nonce = DERInteger.getInstance(obj);        		
        	} else {
                this.requestTime = DVCSTime.getInstance(obj);        		        		
        	}
        }
        
    }

    public static DVCSRequestInformation getInstance(ASN1TaggedObject obj, boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DVCSRequestInformation getInstance(Object obj)
    {
        if (obj instanceof DVCSRequestInformation)
        {
            return (DVCSRequestInformation)obj;
        }
        else if (obj != null)
        {
            return new DVCSRequestInformation(ASN1Sequence.getInstance(obj));
        }

        return null;
    }
        
	@Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        if(version != null)
        	v.add(version);
        v.add(service);

        if (nonce != null)
            v.add(nonce);
        if (requestTime != null)
            v.add(requestTime);
        if (requester != null)
        	v.add(new DERTaggedObject(true, 0, requester));
        if (requestPolicy != null)
        	v.add(new DERTaggedObject(true, 1, requestPolicy));
        if (dvcs != null)
        	v.add(new DERTaggedObject(true, 2, dvcs));
        if (dataLocations != null)
        	v.add(new DERTaggedObject(true, 3, dataLocations));
        if (extensions != null)
        	v.add(new DERTaggedObject(false, 4, extensions));

        return new DERSequence(v);
    }

	public DERInteger getVersion() {
		return version;
	}

	public int getService() {
		return service.getValue().intValue();
	}

	public DERInteger getNonce() {
		return nonce;
	}

	public void setNonce(DERInteger nonce) {
		this.nonce = nonce;
	}

	public DVCSTime getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(DVCSTime requestTime) {
		this.requestTime = requestTime;
	}

	public GeneralNames getRequester() {
		return requester;
	}

	public void setRequester(GeneralNames requester) {
		this.requester = requester;
	}

	public PolicyInformation getRequestPolicy() {
		return requestPolicy;
	}

	public void setRequestPolicy(PolicyInformation requestPolicy) {
		this.requestPolicy = requestPolicy;
	}

	public GeneralNames getDvcs() {
		return dvcs;
	}

	public void setDvcs(GeneralNames dvcs) {
		this.dvcs = dvcs;
	}

	public GeneralNames getDataLocations() {
		return dataLocations;
	}

	public void setDataLocations(GeneralNames dataLocations) {
		this.dataLocations = dataLocations;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public void setExtensions(Extensions extensions) {
		this.extensions = extensions;
	}

}
