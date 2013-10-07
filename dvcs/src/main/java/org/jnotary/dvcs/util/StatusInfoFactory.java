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
package org.jnotary.dvcs.util;

import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIFreeText;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;

public class StatusInfoFactory {

	public static PKIStatusInfo getInstance(int pkiStatus, String freeText, Integer pkiFailInfo) {
		PKIStatus status = PKIStatus.getInstance(new DERInteger(pkiStatus));
		PKIFreeText    statusString = (freeText == null ? null : new PKIFreeText(freeText));
	    PKIFailureInfo failInfo = (pkiFailInfo == null ? null : new PKIFailureInfo(pkiFailInfo));
	        
		return new PKIStatusInfo(status, statusString, failInfo);
	}
	
}
