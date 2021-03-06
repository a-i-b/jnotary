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

/*
ServiceType ::= ENUMERATED { cpd(1), vsd(2), cpkc(3), ccpd(4) }
*/
public class ServiceType {
	public static final int CPD = 1;
	public static final int VSD = 2; 
	public static final int VPKC = 3;
	public static final int CCPD = 4;
	
	public static String toString(int type) {
		switch(type) {
		case CPD:
			return "CPD";
		case VSD:
			return "VSD";
		case VPKC:
			return "VPKC";
		case CCPD:
			return "CCPD";
		}		
		return "Unknown type";
	}
}
