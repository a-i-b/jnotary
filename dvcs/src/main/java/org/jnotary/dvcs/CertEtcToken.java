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
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.CertStatus;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.smime.SMIMECapabilities;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.X509Extension;

/*
CertEtcToken ::= CHOICE {

    certificate                  [0] IMPLICIT Certificate ,
    esscertid                    [1] ESSCertId ,
    pkistatus                    [2] IMPLICIT PKIStatusInfo ,
    assertion                    [3] ContentInfo ,
    crl                          [4] IMPLICIT CertificateList,
    ocspcertstatus               [5] IMPLICIT CertStatus,
    oscpcertid                   [6] IMPLICIT CertId ,
    oscpresponse                 [7] IMPLICIT OCSPResponse,
    capabilities                 [8] SMIMECapabilities,
    extension                    Extension }
*/
public class CertEtcToken extends ASN1Object implements ASN1Choice {
	private Certificate certificate = null;
	private ESSCertID	esscertid = null;
	private PKIStatusInfo	pkistatus = null;
	private ContentInfo	assertion = null;
	private CertificateList	crl = null;
	private CertStatus	ocspcertstatus = null;
	private CertID	oscpcertid = null;
	private OCSPResponse	oscpresponse = null;
	private SMIMECapabilities	capabilities = null;
	private Extension	extension = null;
	
	public CertEtcToken(Certificate certificate) {
		this.certificate = certificate;
	}
	public CertEtcToken(ESSCertID esscertid) {
		this.esscertid = esscertid;
	}
	public CertEtcToken(PKIStatusInfo pkistatus) {
		this.pkistatus = pkistatus;
	}
	public CertEtcToken(ContentInfo assertion) {
		this.assertion = assertion;
	}
	public CertEtcToken(CertificateList crl) {
		this.crl = crl;
	}
	public CertEtcToken(CertStatus ocspcertstatus) {
		this.ocspcertstatus = ocspcertstatus;
	}
	public CertEtcToken(CertID oscpcertid) {
		this.oscpcertid = oscpcertid;
	}
	public CertEtcToken(OCSPResponse oscpresponse) {
		this.oscpresponse = oscpresponse;
	}
	public CertEtcToken(SMIMECapabilities capabilities) {
		this.capabilities = capabilities;
	}
	public CertEtcToken(Extension extension) {
		this.extension = extension;
	}

	public Certificate getCertificate() {
		return certificate;
	}
	public ESSCertID getEsscertid() {
		return esscertid;
	}
	public PKIStatusInfo getPkistatus() {
		return pkistatus;
	}
	public ContentInfo getAssertion() {
		return assertion;
	}
	public CertificateList getCrl() {
		return crl;
	}
	public CertStatus getOcspcertstatus() {
		return ocspcertstatus;
	}
	public CertID getOscpcertid() {
		return oscpcertid;
	}
	public OCSPResponse getOscpresponse() {
		return oscpresponse;
	}
	public SMIMECapabilities  getCapabilities() {
		return capabilities;
	}
	public Extension getExtension() {
		return extension;
	}
	
	
    private CertEtcToken(Object obj)
    {
    	if(obj instanceof X509Extension) {
    		extension = null; //TODO: Das muss verstanden werden
    	} else {
    		ASN1TaggedObject tagObj = (ASN1TaggedObject)obj;
    		switch(tagObj.getTagNo()) {
    		case 0:
    			certificate = Certificate.getInstance(tagObj.getObject());
    			break;
    		case 1:
    			esscertid = ESSCertID.getInstance(tagObj.getObject());
    			break;
    		case 2:
    			pkistatus = PKIStatusInfo.getInstance(tagObj.getObject());
    			break;
    		case 3:
    			assertion = ContentInfo.getInstance(tagObj.getObject());
    			break;
    		case 4:
    			crl = CertificateList.getInstance(tagObj.getObject());
    			break;
    		case 5:
    			ocspcertstatus = CertStatus.getInstance(tagObj.getObject());
    			break;
    		case 6:
    			oscpcertid = CertID.getInstance(tagObj.getObject());
    			break;
    		case 7:
    			oscpresponse = OCSPResponse.getInstance(tagObj.getObject());
    			break;
    		case 8:
    			capabilities = SMIMECapabilities.getInstance(tagObj.getObject());
    			break;
    		}
   		
    	}
        
    }

    public static CertEtcToken getInstance(Object obj)
    {
        if (obj instanceof CertEtcToken)
        {
            return (CertEtcToken)obj;
        }
        else if (obj != null)
        {
            return new CertEtcToken(obj);
        }

        return null;
    }
        
	@Override
	public ASN1Primitive toASN1Primitive() {
		
		if(certificate != null)
			return new DERTaggedObject(true, 0, certificate).toASN1Primitive();
		if(esscertid != null)
			return new DERTaggedObject(true, 1, esscertid).toASN1Primitive();
		if(pkistatus != null)
			return new DERTaggedObject(true, 2, pkistatus).toASN1Primitive();
		if(assertion != null)
			return new DERTaggedObject(true, 3, assertion).toASN1Primitive();
		if(crl != null)
			return new DERTaggedObject(true, 4, crl).toASN1Primitive();
		if(ocspcertstatus != null)
			return new DERTaggedObject(true, 5, ocspcertstatus).toASN1Primitive();
		if(oscpcertid != null)
			return new DERTaggedObject(true, 6, oscpcertid).toASN1Primitive();
		if(oscpresponse != null)
			return new DERTaggedObject(true, 7, oscpresponse).toASN1Primitive();
		if(capabilities != null)
			return new DERTaggedObject(true, 8, capabilities).toASN1Primitive();
		if(extension != null)
			return extension.getParsedValue().toASN1Primitive();
		return null;
    }
}
