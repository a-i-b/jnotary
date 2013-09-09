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
package org.jnotary.service.data;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jnotary.service.model.Configuration;

@Stateless
@LocalBean
public class DvcsConfigurationManager implements IDvcsConfigurationManager {
	
	@Inject
	private Logger log;
	
	@PersistenceContext
    private EntityManager em;
    
    @Override
	public Configuration read() throws Exception {
		try {
			Query query = em.createQuery("SELECT e FROM "+Configuration.class.getName()+" e");
			List<Configuration> list = query.getResultList();
			if(list != null && !list.isEmpty()) {
				return list.get(0);
			}
			return null; 		
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
    }
    
	@Override
	public Configuration store(Configuration configuration) throws Exception {
		try {
			if (configuration.getId() == null){
				em.persist(configuration);
			} else {
				em.merge(configuration);
			}
			return configuration;			
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
	}
}
