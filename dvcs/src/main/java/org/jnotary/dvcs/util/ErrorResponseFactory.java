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

import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.jnotary.dvcs.DVCSErrorNotice;
import org.jnotary.dvcs.DVCSResponse;

public class ErrorResponseFactory {

	public static DVCSResponse getInstance(int pkiStatus, String freeText, Integer pkiFailInfo) {	        
		PKIStatusInfo	transactionStatus = StatusInfoFactory.getInstance(pkiStatus, freeText, pkiFailInfo); 
		
		DVCSErrorNotice dvErrorNote = new DVCSErrorNotice(transactionStatus);
		DVCSResponse respOut = new DVCSResponse(dvErrorNote);
		return respOut;		
	}

	public static DVCSResponse getInstance(int pkiStatus, String freeText) {
		return getInstance(pkiStatus, freeText, null);
	}

	public static DVCSResponse getInstance(int pkiStatus) {
		return getInstance(pkiStatus, null, null);
	}

	public static DVCSResponse getInstance(DVCSException e) {
		return getInstance(e.getPkiStatus(), e.getFreeText(), e.getPkiFailInfo());
		
	}
	
}
