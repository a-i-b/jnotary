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
package org.jnotary.service.util;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.jnotary.crypto.CRLStorage;
import org.jnotary.crypto.FileStorage;
import org.jnotary.crypto.TrustedStore;
import org.jnotary.crypto.UserKeyStore;
import org.jnotary.service.data.ICRLManager;
import org.jnotary.service.data.IDvcsConfigurationManager;
import org.jnotary.service.model.CrlDistributionPoint;
import org.jnotary.service.model.Configuration;

import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Startup
@Singleton
public class GlobalResources implements IGlobalResources {
	
	@Inject
	private Logger log;

	@Inject
	private IDvcsConfigurationManager configurationManager;

	@Inject
	private ICRLManager crlManager;
	
	private UserKeyStore myKeyStore;
	private TrustedStore trustedRootStore;
	private TrustedStore trustedUserStore;
	private CRLStorage crlStorage;
	
	private AtomicInteger serialNumber = new AtomicInteger(0); 
	private AtomicLong nonce = new AtomicLong(0);

	private ServiceConfig serviceConfig = new ServiceConfig();	
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	public Integer getSerialNumber() {
		return serialNumber.incrementAndGet();
	}

	public Long getNonce() {
		return nonce.incrementAndGet();
	}

	public CRLStorage getCrlStorage() {
		return crlStorage;
	}

	public TrustedStore getTrustedUserStore() {
		return trustedUserStore;
	}

	public TrustedStore getTrustedRootStore() {
		return trustedRootStore;
	}

	public UserKeyStore getMyKeyStore() {
		return myKeyStore;
	}


	@PostConstruct
	public void initialize() {
		try { 
	        initUserKeyStorage();	        
	        initTrustedRoots();
	        initCrlStorage();
//	        initTrustedUsers();		
	        initServiceProperties();
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}

	@Override
	public void initCrlStorage() throws Exception {
		crlStorage = new CRLStorage();		
		List<CrlDistributionPoint> list = crlManager.read();
		for(CrlDistributionPoint crl: list) {
			crlStorage.addCRLSource(crl.getIssuerHash(), crl.getCrlUrl());
		}
	}

	@Override
	public void initServiceProperties() throws Exception {
		Configuration configuration = configurationManager.read();
		if(configuration == null) {
	        log.info("Service is not configured");
			return;
		}

		serviceConfig.load(configuration);
	}

	@Override
	public void initTrustedUsers() throws Exception {
		
		Properties myKeyProperties = new Properties();		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = classLoader.getResource("trustedUsers.properties");
		myKeyProperties.load(url.openStream());
		
		trustedUserStore = new TrustedStore(
				new FileStorage(
						myKeyProperties.getProperty("store-path"),
						myKeyProperties.getProperty("store-type"), 
						myKeyProperties.getProperty("store-password")));
		log.info("Loaded trusted users store. Path = " + myKeyProperties.getProperty("store-path"));
		log.info("List of trusted users:");
		for(X509Certificate cert: trustedUserStore.getCertificates()) {
			log.info(cert.getSubjectDN().getName());
		}
	}
		
	@Override
	public void initTrustedRoots() throws Exception {
		Configuration configuration = configurationManager.read();
		if(configuration == null) {
	        log.info("Service is not configured");
			return;
		}

		trustedRootStore = new TrustedStore(
				new FileStorage(
						configuration.getTrustedRootStorePath(),
						configuration.getTrustedRootStoreType(),
						configuration.getTrustedRootStorePassword()));
        log.info("Loaded trusted roots store. Path = " + configuration.getTrustedRootStorePath());
        log.info("List of trusted roots:");
        for(X509Certificate cert: trustedRootStore.getCertificates()) {
        	log.info(cert.getSubjectDN().getName());
        }
	}

	@Override
	public void initUserKeyStorage() throws Exception  {
		Configuration configuration = configurationManager.read();
        log.info("Configuration is loaded");
		if(configuration == null) {
	        log.info("Service is not configured");
			return;
		}

		try {
		myKeyStore = new UserKeyStore(
				new FileStorage(
						configuration.getKeyStorePath(),
						configuration.getKeyStoreType(),
						configuration.getKeyStorePassword()),
				configuration.getKeyAlias(),
				configuration.getKeyAliasPassword());
		} catch(Exception e) {
	        log.info("Error loading user key store: " + e.getLocalizedMessage());
	        throw e;
		}
        log.info("Loaded private key store. Path = " + configuration.getKeyStorePath());
        log.info("Working certificate's DN = " + getMyKeyStore().getUserCertificate().getSubjectDN());		
	}
	
	@PreDestroy
	public void terminate() {
		
	}
	
}
