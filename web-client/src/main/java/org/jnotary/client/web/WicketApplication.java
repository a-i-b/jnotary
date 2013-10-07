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
package org.jnotary.client.web;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.Folder;

public class WicketApplication extends WebApplication {
	private Folder uploadFolder = null;
	
    public WicketApplication() {
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }

    public Folder getUploadFolder()
    {
        return uploadFolder;
    }
    
	@Override
	public void init()
	{
		super.init();
        // Get BeanManager.
        BeanManager bm;
        try {
            bm = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to obtain CDI BeanManager", e);
        }
        // Configure CDI, disabling Conversations as we aren't using them
        new CdiConfiguration(bm).configure(this);
        
        uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
        // Ensure folder exists
        uploadFolder.mkdirs();
        
        getApplicationSettings().setUploadProgressUpdatesEnabled(true);
        
     }
}
