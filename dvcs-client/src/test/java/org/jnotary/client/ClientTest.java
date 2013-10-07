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
package org.jnotary.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

public class ClientTest
{
	String configFile;
	String filein;
	
	@Before
    @SuppressWarnings("static-access")
	public void before() {
		String keyPath = getClass().getClassLoader().getSystemResource("Client1.p12").getPath();
		keyPath = keyPath.replace("/Client1.p12", "");
		System.setProperty("user.dir", keyPath);
		configFile = getClass().getClassLoader().getSystemResource("myKey.properties").getPath();
		filein = getClass().getClassLoader().getSystemResource("txtfile.in").getPath();
	}
//	@Ignore
	@Test
	public void cpd() throws Exception {

		String[] args = {
				"-k", configFile,
				"-u", "http://localhost:8080/dvcs-srv/rest/dvcs", 
				"-t", "cpd", 
				filein, 
				"/tmp/cpd-file.out"};
		DvcsClient.main(args);
	}

	@Ignore
	@Test
	public void ccpd() throws Exception {

		String[] args = {
				"-k", configFile,
				"-u", "http://localhost:8080/dvcs-srv/rest/dvcs", 
				"-t", "ccpd", 
				filein, 
				"/tmp/ccpd-file.out"};
		DvcsClient.main(args);
	}

	@Ignore
	@Test
    @SuppressWarnings("static-access")
	public void vsd() throws Exception {

		String p7sfile = getClass().getClassLoader().getSystemResource("test.p7s").getPath();		
		String[] args = {
				"-k", configFile,
				"-u", "http://localhost:8080/dvcs-srv/rest/dvcs", 
				"-t", "vsd", 
				p7sfile, 
				"/tmp/vsd-file.out"};
		DvcsClient.main(args);
	}

	@Ignore
	@Test
    @SuppressWarnings("static-access")
	public void vpkc() throws Exception {

		String vpkcfile = getClass().getClassLoader().getSystemResource("Client1.cer").getPath();		
		String[] args = {
				"-k", configFile,
				"-u", "http://localhost:8080/dvcs-srv/rest/dvcs", 
				"-t", "vpkc", 
				vpkcfile, 
				"/tmp/vpkc-file.out"};
		DvcsClient.main(args);
	}
	
	@Ignore
	@Test
    @SuppressWarnings("static-access")
	public void vpkcOfRevoked() throws Exception {

		String vpkcfile = getClass().getClassLoader().getSystemResource("Gesperrt.cer").getPath();		
		String[] args = {
				"-k", configFile,
				"-u", "http://localhost:8080/dvcs-srv/rest/dvcs", 
				"-t", "vpkc", 
				vpkcfile, 
				"/tmp/vpkc-file.out"};
		DvcsClient.main(args);
	}	
}
