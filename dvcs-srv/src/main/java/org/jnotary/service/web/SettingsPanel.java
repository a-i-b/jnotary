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
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
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
	private List<String> storeTypes = Arrays.asList("PKCS12", "JKS");
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
		
		form = new Form<Configuration>("configForm", new Model(configuration)) {
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
	
		form.add(new TextField<String>("keyStorePath", new PropertyModel(configuration, "keyStorePath")));

		if(configuration.getKeyStoreType() == null || configuration.getKeyStoreType().isEmpty())
			configuration.setKeyStoreType(storeTypes.get(0));
		RadioChoice<String> keyStoreType = new RadioChoice<String>("keyStoreType",
				new PropertyModel<String>(configuration, "keyStoreType"), storeTypes);
		form.add(keyStoreType);
          
		form.add(new TextField<String>("keyStorePassword", new PropertyModel(configuration, "keyStorePassword")));
		form.add(new TextField<String>("keyAlias", new PropertyModel(configuration, "keyAlias")));
		form.add(new TextField<String>("keyAliasPassword", new PropertyModel(configuration, "keyAliasPassword")));

		DropDownChoice<String> listSignatures = new DropDownChoice<String>(
				"signalg", 
				new PropertyModel<String>(configuration, "signatureAlgorithm"), signatures);
		form.add(listSignatures);
		DropDownChoice<String> listDigests = new DropDownChoice<String>(
				"hashalg", 
				new PropertyModel<String>(configuration, "hashAlgorithm"), messageDigests);
		form.add(listDigests);
		
		form.add(new CheckBox("verifycrl", new PropertyModel(configuration, "verifyCRL")));
		form.add(new CheckBox("addsigncert", new PropertyModel(configuration, "addSertificateToSignature")));
	
		add(form);		
		
		super.onInitialize();
	}

			
}
