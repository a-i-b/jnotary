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
package org.jnotary.service.dvcs;

import java.util.Map;

import org.jnotary.dvcs.DVCSRequest;


public interface IDvcsHandler {
	byte[] handle(DVCSRequest request) throws Exception;
	byte[] handle(int serviceType, byte[] inputData, Map<String,String> properties) throws Exception;
}
