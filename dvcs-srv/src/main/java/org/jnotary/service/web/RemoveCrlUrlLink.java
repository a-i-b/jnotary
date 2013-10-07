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
package org.jnotary.service.web;

import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.jnotary.service.data.ICRLManager;
import org.jnotary.service.model.CrlDistributionPoint;
import org.jnotary.service.util.IGlobalResources;

public class RemoveCrlUrlLink extends Link<Void> {

	private static final long serialVersionUID = 1L;

	@Inject
	ICRLManager crlManager;
	@Inject
	IGlobalResources globalResources;
	
	private final CrlDistributionPoint crlUrl;
	private Page backPage;
 
	public RemoveCrlUrlLink(Page backPage, String componentId, CrlDistributionPoint crlUrl) {
	    super(componentId);
	    this.crlUrl = crlUrl;
	    this.backPage = backPage;
	}
	 
	@Override
	public void onClick() {
		try {
			crlManager.remove(crlUrl.getId());
			globalResources.initCrlStorage();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	 
	    getSession().info("Location '" + crlUrl.getCrlUrl() + "' was removed");
	    setResponsePage(backPage);
	}
}
