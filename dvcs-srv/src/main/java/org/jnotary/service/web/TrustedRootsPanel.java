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

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;
import org.jnotary.service.data.IDvcsConfigurationManager;
import org.jnotary.service.model.Configuration;
import org.jnotary.service.util.IGlobalResources;

public class TrustedRootsPanel extends Panel {

	private static final long serialVersionUID = -2641616333829879880L;
	
	@Inject
	IDvcsConfigurationManager configurationManager;

	@Inject
	IGlobalResources globalResources;
	
	private Configuration configuration = null;
	private Form<?> form;
	private List<String> storeTypes = Arrays.asList("PKCS12", "JKS");
	private	 List<X509Certificate> rootList = null; 
	private	 int rowIndex = 0;

	
	public TrustedRootsPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		try {
			configuration = configurationManager.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(configuration == null)
			configuration = new Configuration();
		
		form = new Form<Configuration>("configForm", new PropertyModel<Configuration>(this, "configuration"));

		form.add(new TextField<String>("trustedRootStorePath", new PropertyModel<String>(configuration, "trustedRootStorePath")));          
		form.add(new TextField<String>("trustedRootStorePassword", new PropertyModel<String>(configuration, "trustedRootStorePassword")));

		final WebMarkupContainer wmc = new WebMarkupContainer("listWmc");
		wmc.setOutputMarkupId(true);
		add(wmc);	
		
		form.add(createSubmitButton(wmc));
		
		add(form);		
		
		wmc.add(createListView());
		
		super.onInitialize();
	}

	private AjaxSubmitLink createSubmitButton(final WebMarkupContainer wmc) {
		AjaxSubmitLink submitButton = new AjaxSubmitLink("submitButton") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					Configuration configuration = (Configuration)form.getModelObject();
					configurationManager.store(configuration);
					globalResources.initTrustedRoots();
					rootList = globalResources.getTrustedRootStore().getCertificates();
					rowIndex = 0;					 
				} catch (Exception e) {
					e.printStackTrace();
				}
				target.add(wmc);
				super.onSubmit(target, form);
			}
			
		};
		return submitButton;
	}


	private ListView<X509Certificate> createListView() {
		rootList = globalResources.getTrustedRootStore().getCertificates();
		rowIndex = 0;
		
		return new ListView<X509Certificate>("repeating", new PropertyModel<List<X509Certificate>>(this, "rootList")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<X509Certificate> item) {
				X509Certificate cert = item.getModel().getObject();
	            item.add(new Label("dn", cert.getSubjectDN().getName()));
	            item.add(new Label("serial", cert.getSerialNumber().toString(16)));
	            item.add(new Label("datefrom", cert.getNotBefore().toString()));
	            item.add(new Label("dateto", cert.getNotAfter().toString()));
	            
	            item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>()
	            {
	                private static final long serialVersionUID = 1L;

	                @Override
	                public String getObject()
	                {
	                    return (rowIndex++ % 2 == 1) ? "even" : "odd";
	                }
	            }));
	            
			}
		};
	}
}
