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

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jnotary.crypto.util.CryptoHelper;
import org.jnotary.service.data.IDvcsConfigurationManager;
import org.jnotary.service.model.Configuration;
import org.jnotary.service.util.IGlobalResources;

public class SettingsPanel extends Panel {

	private static final long serialVersionUID = 157285754028360758L;
	
	@Inject
	IDvcsConfigurationManager configurationManager;

	@Inject
	IGlobalResources globalResources;
	
	private Form<?> form;
	private List<String> signatures;
	private List<String> messageDigests;
	
	public SettingsPanel(String id) {
		super(id);
		signatures = CryptoHelper.getSignatures();
		messageDigests = CryptoHelper.getMessageDigests();
	}
	
	@Override
	protected void onInitialize() {
		Configuration configuration = null;
		try {
			configuration = configurationManager.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(configuration == null)
			configuration = new Configuration();
		
		form = new Form<Configuration>("configForm", new Model<Configuration>(configuration)) {
			private static final long serialVersionUID = 8242498687647454234L;

			@Override
			 protected void onSubmit() {
				 super.onSubmit();
				 try {
					 Configuration configuration = (Configuration)getModelObject();
					 configurationManager.store(configuration);
					 globalResources.initUserKeyStorage();
					 globalResources.initServiceProperties();
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		};
	
		form.add(new TextField<String>("keyStorePath", new PropertyModel<String>(configuration, "keyStorePath")));          
		form.add(new TextField<String>("keyStorePassword", new PropertyModel<String>(configuration, "keyStorePassword")));
		form.add(new TextField<String>("keyAlias", new PropertyModel<String>(configuration, "keyAlias")));
		form.add(new TextField<String>("keyAliasPassword", new PropertyModel<String>(configuration, "keyAliasPassword")));

		DropDownChoice<String> listSignatures = new DropDownChoice<String>(
				"signalg", 
				new PropertyModel<String>(configuration, "signatureAlgorithm"), signatures);
		form.add(listSignatures);
		DropDownChoice<String> listDigests = new DropDownChoice<String>(
				"hashalg", 
				new PropertyModel<String>(configuration, "hashAlgorithm"), messageDigests);
		form.add(listDigests);
		
		form.add(new CheckBox("verifycrl", new PropertyModel<Boolean>(configuration, "verifyCRL")));
		form.add(new CheckBox("addsigncert", new PropertyModel<Boolean>(configuration, "addSertificateToSignature")));
	
		form.add(new CheckBox("CpdAllowed", new PropertyModel<Boolean>(configuration, "CpdAllowed")));
		form.add(new CheckBox("CcpdAllowed", new PropertyModel<Boolean>(configuration, "CcpdAllowed")));
		form.add(new CheckBox("VsdAllowed", new PropertyModel<Boolean>(configuration, "VsdAllowed")));
		form.add(new CheckBox("VpkcAllowed", new PropertyModel<Boolean>(configuration, "VpkcAllowed")));

		add(form);		
		
		super.onInitialize();
	}

			
}
