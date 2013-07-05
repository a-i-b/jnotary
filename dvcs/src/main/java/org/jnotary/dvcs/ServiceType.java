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

/*
ServiceType ::= ENUMERATED { cpd(1), vsd(2), cpkc(3), ccpd(4) }
*/
public class ServiceType {
	public static final int CPD = 1;
	public static final int VSD = 2; 
	public static final int VPKC = 3;
	public static final int CCPD = 4;
}
