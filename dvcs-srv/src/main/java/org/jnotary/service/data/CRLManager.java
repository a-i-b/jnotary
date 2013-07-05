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
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jnotary.service.model.CrlDistributionPoint;
import org.jnotary.service.model.Configuration;

@Stateless
@LocalBean
public class CRLManager implements ICRLManager {
	@Inject
	private Logger log;
	
	@Inject
    private EntityManager em;

	@Override
	public List<CrlDistributionPoint> read() throws Exception {
		try {
			Query query = em.createQuery("SELECT e FROM "+CrlDistributionPoint.class.getName()+" e");
			return query.getResultList();
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
	}
	
	@Override
	public CrlDistributionPoint store(CrlDistributionPoint crl) throws Exception {		
		if (crl == null)
			return null;
		
		try {
			if (crl.getId() == null) {
				em.persist(crl);
			} else {
				em.merge(crl);
			}
			return crl;
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
	}

	@Override
	public void remove(Long urlId) throws Exception {
		try {
			if (urlId != null) {
				CrlDistributionPoint obj = em.find(CrlDistributionPoint.class, urlId);
				em.remove(obj);
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw e;
		}
	}

	@Override
	public CrlDistributionPoint getById(Long crlUrlId) {
		// TODO Auto-generated method stub
		return em.find(CrlDistributionPoint.class, crlUrlId);
	}
	
}
