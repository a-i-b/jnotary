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
package org.jnotary.service.data;

import org.jnotary.service.model.Configuration;

public interface IDvcsConfigurationManager {

	public abstract Configuration read() throws Exception;

	public abstract Configuration store(Configuration configuration)
			throws Exception;

}
