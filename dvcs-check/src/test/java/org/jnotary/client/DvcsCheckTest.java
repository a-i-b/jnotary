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
package org.jnotary.client;

import org.jnotary.client.DvcsCheck;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class DvcsCheckTest {
	private String configFile;
	private String srcFile;
	private String dvcsFile;
	
	@Before
	public void before() {
		String keyPath = getClass().getClassLoader().getSystemResource("Client1.p12").getPath();
		keyPath = keyPath.replace("/Client1.p12", "");
		System.setProperty("user.dir", keyPath);
		configFile = getClass().getClassLoader().getSystemResource("myKey.properties").getPath();
		srcFile = getClass().getClassLoader().getSystemResource("txtfile.in").getPath();
		dvcsFile = getClass().getClassLoader().getSystemResource("cpd-file.out").getPath();
	}
//	@Ignore
	@Test
	public void checkCpd() throws Exception {

		String[] args = {
				"-k", configFile,
				srcFile, 
				dvcsFile};
		DvcsCheck.main(args);
	}
}
