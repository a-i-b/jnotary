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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.jnotary.service.data.ICRLManager;
import org.jnotary.service.model.Configuration;
import org.jnotary.service.model.CrlDistributionPoint;
import org.jnotary.service.util.IGlobalResources;

public class CRLPanel extends Panel {

	private static final long serialVersionUID = -4129620856966156409L;
	
	@Inject
	ICRLManager crlManager;

	@Inject
	IGlobalResources globalResources;
	
	private Map<Integer, X509Certificate> certMap = null;
	private List<CrlDistributionPoint> crlUrls = null;
	private int rowIndex = 0;
	
	private WebMarkupContainer wmc = null;

	public CRLPanel(String id) {
		super(id);
	}
	
	private void initGui() throws Exception {
		addURLsModule();
		final ModalWindow modalWindow = createModalWindow("modalAdd", null);
        add(modalWindow);	
        add(new AjaxLink<Void>("addUrlLink")
        {
			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target)
            {
            	modalWindow.show(target);
            }
        });
	}

	private ModalWindow createModalWindow(final String windowName, final Long crlUrlId) {
		final ModalWindow modalWindow = new ModalWindow(windowName);
        
        modalWindow.setCookieName(windowName);
        modalWindow.setPageCreator(new ModalWindow.PageCreator()
        {
			private static final long serialVersionUID = 1L;

			public Page createPage()
            {
                return new AddEditCrlUrlPage(crlUrlId, modalWindow);
            }
        });
        modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
        {
			private static final long serialVersionUID = 1L;

			public void onClose(AjaxRequestTarget target)
            {
				wmc.setOutputMarkupId(true);
				initListData();
                target.add(wmc);
            }
        });
        modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback()
        {
			private static final long serialVersionUID = 1L;

			public boolean onCloseButtonClicked(AjaxRequestTarget target)
            {
                return true;
            }
        });

		return modalWindow;
	}
	
	@Override
	protected void onInitialize() {
		try {	
			List<X509Certificate> roots = globalResources.getTrustedRootStore().getCertificates();
			certMap = new TreeMap<Integer, X509Certificate>();
			for(X509Certificate cert: roots) {
				certMap.put(cert.hashCode(), cert);
			}
			initGui();
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onInitialize();
	}

    private void addURLsModule() throws Exception {	
		wmc = new WebMarkupContainer("listWmc");
		wmc.setOutputMarkupId(true);
		add(wmc);	
		
		wmc.add(createListView(wmc));

    }

	private void initListData() {
		try {
			crlUrls = crlManager.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		rowIndex = 0;
	}
	
	private AjaxLink<Void> createRemoveButton(final WebMarkupContainer wmc, final Long urlId) {
		AjaxLink<Void> removeLink = new AjaxLink<Void>("removeUrlLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					crlManager.remove(urlId);
					globalResources.initCrlStorage();
					crlUrls = crlManager.read();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				target.add(wmc);
			}
			
		};
		return removeLink;
	}


	private ListView<CrlDistributionPoint> createListView(final WebMarkupContainer wmc) throws Exception {
		initListData();
		
		return new ListView<CrlDistributionPoint>("repeating", new PropertyModel<List<CrlDistributionPoint>>(this, "crlUrls")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CrlDistributionPoint> item) {
				final CrlDistributionPoint cp = item.getModel().getObject(); 
				
	        	if(cp.getIssuerHash() == null || certMap.get(cp.getIssuerHash()) == null)
	        		item.add(new Label("issuerDescription", "<p style=\"color:red\">NOT FOUND!!!: "+cp.getIssuerDescription()+"</p>").setEscapeModelStrings(false));
	        	else
	        		item.add(new Label("issuerDescription", new PropertyModel<CrlDistributionPoint>(cp, "issuerDescription")).setEscapeModelStrings(false));  
	        	item.add(new Label("crlUrl", new PropertyModel<CrlDistributionPoint>(cp, "crlUrl")));      
	            
	        	final Long crlUrlId = cp.getId();
	    		final ModalWindow modalWindow = createModalWindow("modalEdit", crlUrlId);
	    		item.add(modalWindow);	
	            item.add(new AjaxLink<Void>("editUrlLink")
	            {
	    			private static final long serialVersionUID = 1L;

	    			@Override
	                public void onClick(AjaxRequestTarget target)
	                {
	                	modalWindow.show(target);
	                }
	            });	            
	            item.add(createRemoveButton(wmc, cp.getId()));
	            
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
