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
package org.jnotary.service.util;

import org.jnotary.crypto.CRLStorage;
import org.jnotary.crypto.TrustedStore;
import org.jnotary.crypto.UserKeyStore;

public interface IGlobalResources {
	
	public abstract void initServiceProperties() throws Exception;
	public abstract void initTrustedUsers() throws Exception;
	public abstract void initTrustedRoots() throws Exception;
	public abstract void initUserKeyStorage() throws Exception;
	public abstract void initCrlStorage() throws Exception;

	public abstract ServiceConfig getServiceConfig();

	public abstract CRLStorage getCrlStorage();

	public abstract TrustedStore getTrustedRootStore();

	public abstract UserKeyStore getMyKeyStore();

	public abstract Integer getSerialNumber();

}
