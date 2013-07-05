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
package org.jnotary.dvcs.util;

public class DVCSException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private Integer pkiStatus;
	private String freeText = null;
	private Integer pkiFailInfo = null;
	
	public DVCSException(int pkiStatus) {
		this.pkiStatus = pkiStatus;
	}
	
	public DVCSException(int pkiStatus, String freeText) {
		this.pkiStatus = pkiStatus;
		this.freeText = freeText;
	}

	public DVCSException(int pkiStatus, String freeText, Integer pkiFailInfo) {
		this.pkiStatus = pkiStatus;
		this.freeText = freeText;
		this.pkiFailInfo = pkiFailInfo;		
	}
	

	public Integer getPkiStatus() {
		return pkiStatus;
	}

	public String getFreeText() {
		return freeText;
	}

	public Integer getPkiFailInfo() {
		return pkiFailInfo;
	}	
	
	@Override
	public String getMessage() {
		java.lang.StringBuilder sb = new StringBuilder("PKIStatus: ");
		sb.append(pkiStatus);
		
		if(freeText != null) {
			sb.append(", FreeText: ");
			sb.append(freeText);
		}
		
		if(pkiFailInfo != null) {
			sb.append(", PKIFailerInfo: ");
			sb.append(pkiFailInfo);
		}

		return sb.toString();		
	}
	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
		
}
