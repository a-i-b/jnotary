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
package org.jnotary.service.web;

import java.util.ArrayList;
import java.util.List;


import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class MainPage extends WebPage {
		
	@Override
	protected void onInitialize() {
		
		add(new Label("message", "jNotary administration console"));
		
		// create a list of ITab objects used to feed the tabbed panel
		List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Settings"))
		{			
			@Override
			public Panel getPanel(String panelId)
			{
				return new SettingsPanel(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Trusted roots"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new TrustedRootsPanel(panelId);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Additional CRLs"))
		{
			@Override
			public Panel getPanel(String panelId)
			{
				return new CRLPanel(panelId);
			}
		});

		add(new AjaxTabbedPanel("tabs", tabs));		
		
		super.onInitialize();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "css/style.css")));
		
	}
}
