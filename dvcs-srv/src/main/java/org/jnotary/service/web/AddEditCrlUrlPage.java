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


import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.jnotary.service.data.ICRLManager;
import org.jnotary.service.model.CrlDistributionPoint;
import org.jnotary.service.util.IGlobalResources;

public class AddEditCrlUrlPage extends WebPage {

	private static final long serialVersionUID = 1L;
    
	@Inject
	ICRLManager crlManager;
	@Inject
	IGlobalResources globalResources;
	
    private ModalWindow window;
    
    private Map<String,Integer> descriptions = null;
	private CrlDistributionPoint cdp = null;

	
    public AddEditCrlUrlPage(Long crlUrlId, ModalWindow modal) {
        window = modal;
        if(crlUrlId != null) {
        	CrlDistributionPoint crlUrl = crlManager.getById(crlUrlId);
            setDefaultModel(new Model<CrlDistributionPoint>(crlUrl));
        } else 
            setDefaultModel(new Model<CrlDistributionPoint>(new CrlDistributionPoint()));
        
        initGui();
    }
    
	private void initGui() {
    	cdp = getLocationFromPageModel();
		createChoiceLists();		
    	
        Form<CrlDistributionPoint> addUrlForm = new Form<CrlDistributionPoint>("addUrlForm",
        		new CompoundPropertyModel<CrlDistributionPoint>((IModel<CrlDistributionPoint>) getDefaultModel()));
        add(addUrlForm);
               
        addUrlForm.add(new Label("issuerDescriptionLabel", new StringResourceModel("issuerDescription", this, null)));
		addUrlForm.add(createDropDownChoice());
        addUrlForm.add(new Label("nameLabel", new StringResourceModel("urlName", this, null)));
        addUrlForm.add(createLabelField());
        addUrlForm.add(createSubmit());
        addUrlForm.add(createCancel());
    }

	private AjaxButton createSubmit() {
		AjaxButton submitButton = new AjaxButton("submitButton") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {			
		    	CrlDistributionPoint cdp = getLocationFromPageModel();
				cdp.setIssuerHash(descriptions.get(cdp.getIssuerDescription()));
		            
		        if(cdp.getId() == null) {
		            getSession().info(new StringResourceModel("urlAdded", this, null).getString());
		        }
		        else {
		            getSession().info(new StringResourceModel("urlUpdated", this, null).getString());
		        }
		    	try {
					crlManager.store(cdp);
					globalResources.initCrlStorage();
				} catch (Exception e) {
					e.printStackTrace();
				}			
		    	window.close(target);
			}
        };
		return submitButton;
	}
	
	private AjaxButton createCancel() {
		AjaxButton cancelButton = new AjaxButton("cancelButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                getSession().info(new StringResourceModel("urlAdded", this, null).getString());
                window.close(target);
            }
        };
		return cancelButton;
	}
	
	private DropDownChoice<String> createDropDownChoice() {
		DropDownChoice<String> issuers = new DropDownChoice<String>(
			"issuerDescription", 
			new PropertyModel<String>(cdp, "issuerDescription"), 
			new ArrayList<String>(descriptions.keySet()));
		return issuers;
	}

	private void createChoiceLists() {
		List<X509Certificate> rootList = globalResources.getTrustedRootStore().getCertificates();		
		new ArrayList<Integer>(rootList.size());
		descriptions = new TreeMap<String, Integer>();
		
		for(X509Certificate cert: rootList) {
			StringBuilder description = new StringBuilder(cert.getSubjectDN().getName());
			description.append("; SN: ");
			description.append(cert.getSerialNumber());
			description.append("; From: ");
			description.append(cert.getNotBefore().toString());
			description.append("; To: ");
			description.append(cert.getNotAfter().toString());
			
			descriptions.put(description.toString(), cert.hashCode());
		}
	}

    private TextField<String> createLabelField() {
        TextField<String> nameField = new TextField<String>("crlUrl");
        nameField.setLabel(new StringResourceModel("urlName", this, null));        
        return nameField;
    }
    
    private CrlDistributionPoint getLocationFromPageModel() {
        return (CrlDistributionPoint) getDefaultModel().getObject();
    }
}
