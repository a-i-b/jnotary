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
package org.jnotary.client.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.util.DvcsHelper;
import org.jnotary.service.dvcs.DvcsHandler;
import org.jnotary.service.dvcs.IDvcsHandler;

public class MainPage extends WebPage {
 
	private static final long serialVersionUID = 5307954263442479430L;
	
	@EJB(lookup = "ejb:dvcs-srv/ejb//IDvcsHandler!org.jnotary.service.dvcs.DvcsHandler")
	IDvcsHandler dvcsHandler;	
	
	private AtomicInteger nonce = new AtomicInteger(0);
	
	private class FileUploadForm extends Form<Void>
    {
		private static final long serialVersionUID = -468955705580910412L;
		FileUploadField fileUploadField;

        /**
         * Construct.
         * 
         * @param name
         *            Component name
         */
        public FileUploadForm(String name)
        {
            super(name);

            // set this form to multipart mode (allways needed for uploads!)
            setMultiPart(true);

	        this.add(new Button("vsd") {
				private static final long serialVersionUID = 9190936463132890437L;

				public void onSubmit() {
	            	try {
	            		final IDvcsHandler dvcsHandler = null;
		            	byte[] inputData = getFileData();
						DVCSRequest reqOut = DvcsHelper.createVsd(inputData, new Long(nonce.incrementAndGet()));
		            	MainPage.this.info("VSD request is created");
		            	final byte[] response = dvcsHandler.handle(reqOut);
		                AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
							private static final long serialVersionUID = 2941559584154055746L;

							@Override
							public void write(OutputStream output)
									throws IOException {
		                        output.write(response);
							}
		                };
		         
		                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, "vsd.dvcs");        
		                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        	
	        });

	        this.add(new Button("cpd") {
				private static final long serialVersionUID = -6814312868044825303L;

				public void onSubmit() {
	            	try {
	            		final IDvcsHandler dvcsHandler = null;
		            	byte[] inputData = getFileData();
						DVCSRequest reqOut = DvcsHelper.createCpd(inputData, new Long(nonce.incrementAndGet()));
		            	MainPage.this.info("CPD request is created");
		            	final byte[] response = dvcsHandler.handle(reqOut);
		                AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
							private static final long serialVersionUID = 362721243973608457L;

							@Override
							public void write(OutputStream output)
									throws IOException {
		                        output.write(response);
							}
		                };
		         
		                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(rstream, "vsd.dvcs");        
		                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        	
	        });

	        this.add(new Button("vpkc") {
				private static final long serialVersionUID = 1121637539603606951L;

				public void onSubmit() {
					try {
						final IDvcsHandler dvcsHandler = null;
		            	byte[] inputData = getFileData();
		            	DVCSRequest reqOut = DvcsHelper.createVpkc(inputData, new Long(nonce.incrementAndGet()));
		            	MainPage.this.info("VPKC request is created");
						dvcsHandler.handle(reqOut);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        	
	        });
            
            // Add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // Set maximum size to 100K for demo purposes
            setMaxSize(Bytes.kilobytes(100));
        }
        
        protected byte[] getFileData()
        {
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null)
            {
                for (FileUpload upload : uploads)
                {
                    try
                    {
                        MainPage.this.info("Processed file: " + upload.getClientFileName());
                        return upload.getBytes();
                    }
                    catch (Exception e)
                    {
                        throw new IllegalStateException("Unable to write file", e);
                    }
                }
            }
            MainPage.this.info("No data to process");
            return null;
        } 

        private Folder getUploadFolder()
        {
            return ((WicketApplication)Application.get()).getUploadFolder();
        }        
    }

	
	@Override
	protected void onInitialize() {		
		add(new Label("message", "jNotary web-client"));
		
		IDvcsHandler dvcsHandler = null;		
		try {
			dvcsHandler = lookupRemote();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
		add(uploadFeedback);
		
		final FileUploadForm simpleUploadForm = new FileUploadForm("callForm");
		add(simpleUploadForm);
	        
		super.onInitialize();
	}

	public MainPage() {
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "css/style.css")));
		
	}
    
	private static IDvcsHandler lookupRemote() throws NamingException {
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
        final String appName = "";
        final String moduleName = "dvcs-srv";
        final String distinctName = "";
        final String beanName = DvcsHandler.class.getSimpleName();
        // the remote view fully qualified class name
        final String viewClassName = IDvcsHandler.class.getName();
        // let's do the lookup
        return (IDvcsHandler) context.lookup("ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName);
    }
}
