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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;

public class MainPage extends WebPage {
		
	private class FileUploadForm extends Form<Void>
    {
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

            // Add one file input field
            add(fileUploadField = new FileUploadField("fileInput"));

            // Set maximum size to 100K for demo purposes
            setMaxSize(Bytes.kilobytes(100));
        }
        
        @Override
        protected void onSubmit()
        {
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null)
            {
                for (FileUpload upload : uploads)
                {
                    // Create a new file
                    File newFile = new File(getUploadFolder(), upload.getClientFileName());

                    try
                    {
                        // Save to new file
                        newFile.createNewFile();
                        upload.writeTo(newFile);

                        MainPage.this.info("saved file: " + upload.getClientFileName());
                    }
                    catch (Exception e)
                    {
                        throw new IllegalStateException("Unable to write file", e);
                    }
                }
            }
        } 

        private Folder getUploadFolder()
        {
            return ((WicketApplication)Application.get()).getUploadFolder();
        }        
    }

	
	@Override
	protected void onInitialize() {		
		add(new Label("message", "jNotary web-client"));
		
		final FeedbackPanel uploadFeedback = new FeedbackPanel("uploadFeedback");
		add(uploadFeedback);
		
		final FileUploadForm simpleUploadForm = new FileUploadForm("callForm");
		add(simpleUploadForm);
	        
		super.onInitialize();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(new CssResourceReference(MainPage.class, "css/style.css")));
		
	}
}
