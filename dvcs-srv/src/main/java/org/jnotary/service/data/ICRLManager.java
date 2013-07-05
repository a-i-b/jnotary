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
package org.jnotary.service.data;

import java.util.List;

import org.jnotary.service.model.CrlDistributionPoint;

public interface ICRLManager {

	public abstract List<CrlDistributionPoint> read() throws Exception;

	public abstract CrlDistributionPoint store(CrlDistributionPoint crl) throws Exception;

	public abstract void remove(Long urlId) throws Exception;

	public abstract CrlDistributionPoint getById(Long crlUrlId);

}
