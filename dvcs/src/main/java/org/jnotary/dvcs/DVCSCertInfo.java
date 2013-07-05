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
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cms.SignerInfo;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.PolicyInformation;


/*
DVCSCertInfo::= SEQUENCE  {
         version             Integer DEFAULT 1 ,
         dvReqInfo           DVCSRequestInformation,
         messageImprint      DigestInfo,
         serialNumber        Integer,
         responseTime        DVCSTime,
         dvStatus            [0] PKIStatusInfo OPTIONAL,
         policy              [1] PolicyInformation OPTIONAL,
         reqSignature        [2] SignerInfos  OPTIONAL,
         certs               [3] SEQUENCE SIZE (1..MAX) OF
                                 TargetEtcChain OPTIONAL,
         extensions          Extensions OPTIONAL
}
*/
public class DVCSCertInfo  extends ASN1Object {
    private DERInteger				version = null;			// Integer DEFAULT 1
    private DVCSRequestInformation	dvReqInfo = null;		// DVCSRequestInformation
    private DigestInfo				messageImprint = null;	// DigestInfo
    private DERInteger				serialNumber = null;	// Integer
    private DVCSTime				responseTime = null;	// DVCSTime
    private PKIStatusInfo			dvStatus = null;		// [0] PKIStatusInfo OPTIONAL
    private PolicyInformation		policy = null;			// [1] PolicyInformation OPTIONAL
    private SignerInfo				reqSignature = null;	// [2] SignerInfos  OPTIONAL
    private TargetEtcChain[]		certs = null;			// [3] SEQUENCE SIZE (1..MAX) OF TargetEtcChain OPTIONAL
    private Extensions				extensions = null;		// Extensions OPTIONAL
	
    public DVCSCertInfo(DVCSRequestInformation dvReqInfo,
    	    DigestInfo messageImprint,
    	    DERInteger serialNumber,
    	    DVCSTime responseTime){
    	this.dvReqInfo = dvReqInfo;
    	this.messageImprint = messageImprint;
    	this.serialNumber = serialNumber;
    	this.responseTime = responseTime;
    }
    
    @Override
	public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector    v = new ASN1EncodableVector();
        
        if(version != null)
        	v.add(version);
        v.add(dvReqInfo);
        v.add(messageImprint);
        v.add(serialNumber);
        v.add(responseTime);

        if (getDvStatus() != null)
        	v.add(new DERTaggedObject(true, 0, getDvStatus()));
        if (getPolicy() != null)
        	v.add(new DERTaggedObject(true, 1, getPolicy()));
        if (getReqSignature() != null)
        	v.add(new DERTaggedObject(true, 2, getReqSignature()));
        if (getCerts() != null && getCerts().length > 0) {
	        ASN1EncodableVector seq = new ASN1EncodableVector();
	        for (int i = 0; i < getCerts().length; i++) 
	        {
	        	seq.add(getCerts()[i].toASN1Primitive());
	        }                                    	
        	v.add(new DERTaggedObject(true, 3, new DERSequence(seq)));
        }
        if (getExtensions() != null)
        	v.add(getExtensions());

        return new DERSequence(v);
	}

	public PKIStatusInfo getDvStatus() {
		return dvStatus;
	}

	public void setDvStatus(PKIStatusInfo dvStatus) {
		this.dvStatus = dvStatus;
	}

	public PolicyInformation getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyInformation policy) {
		this.policy = policy;
	}

	public SignerInfo getReqSignature() {
		return reqSignature;
	}

	public void setReqSignature(SignerInfo reqSignature) {
		this.reqSignature = reqSignature;
	}

	public TargetEtcChain[] getCerts() {
		return certs;
	}

	public void setCerts(TargetEtcChain[] certs) {
		this.certs = certs;
	}

	public Extensions getExtensions() {
		return extensions;
	}

	public void setExtensions(Extensions extensions) {
		this.extensions = extensions;
	}
	
	private DVCSCertInfo(ASN1Sequence seq){
        Enumeration e = seq.getObjects();

        Object obj = e.nextElement();
        if(obj instanceof ASN1Integer) {
        	this.version = DERInteger.getInstance(obj);
        	obj = e.nextElement();
        }
       	this.dvReqInfo = DVCSRequestInformation.getInstance(obj);
       	this.messageImprint = DigestInfo.getInstance(e.nextElement());
       	this.serialNumber = DERInteger.getInstance(e.nextElement());
       	this.responseTime = DVCSTime.getInstance(e.nextElement());
 
        for(;e.hasMoreElements();) {        	
        	obj = e.nextElement();
        	if (obj instanceof ASN1TaggedObject) {
        		ASN1TaggedObject tagObj = (ASN1TaggedObject)obj;
        		switch(tagObj.getTagNo()) {
        		case 0:
        			dvStatus = dvStatus.getInstance(tagObj.getObject());
        			break;
        		case 1:
        			policy = policy.getInstance(tagObj.getObject());
        			break;
        		case 2:
        			reqSignature = reqSignature.getInstance(tagObj.getObject());
        			break;
        		case 3:
        			ASN1Sequence seqCerts = DERSequence.getInstance(tagObj.getObject());
        			certs = new TargetEtcChain[seqCerts.size()];
        			for(int i = 0; i < seqCerts.size(); ++i){
        				certs[i] = TargetEtcChain.getInstance(seqCerts.getObjectAt(i));
        			}

        			break;
        		}
        	} else {
    			extensions.getInstance(obj);
        	}
        }
		
	}
	
    public static DVCSCertInfo getInstance(Object obj)
    {
        if (obj instanceof DVCSCertInfo)
        {
            return (DVCSCertInfo)obj;
        }
        else if (obj != null)
        {
            return new DVCSCertInfo(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

	public DERInteger getVersion() {
		return version;
	}

	public DVCSRequestInformation getRequestInformation() {
		return dvReqInfo;
	}

	public DigestInfo getMessageImprint() {
		return messageImprint;
	}

	public DERInteger getSerialNumber() {
		return serialNumber;
	}

	public DVCSTime getResponseTime() {
		return responseTime;
	}
}
