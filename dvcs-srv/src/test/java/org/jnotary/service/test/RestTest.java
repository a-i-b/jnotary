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
package org.jnotary.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jnotary.crypto.FileStorage;
import org.jnotary.crypto.Hasher;
import org.jnotary.crypto.Signer;
import org.jnotary.crypto.UserKeyStore;
import org.jnotary.crypto.Verifier;
import org.jnotary.crypto.Verifier.VerifyResult;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.ServiceType;
import org.jnotary.dvcs.util.DVCSException;
import org.jnotary.dvcs.util.DvcsHelper;
import org.jnotary.service.data.IDvcsConfigurationManager;
import org.jnotary.service.dvcs.DvcsResponseHelper;
import org.jnotary.service.model.Configuration;
import org.jnotary.service.rest.DvcsRequestHandler;
import org.jnotary.service.util.CryptoService;
import org.jnotary.service.util.GlobalResources;
import org.jnotary.service.util.IGlobalResources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RestTest {
	
	@Inject
	IDvcsConfigurationManager configurationManager;
	@Inject
	IGlobalResources globalResources;
	
	private final String url = "http://localhost:8080/test-srv/";

	String sampleData = "1234567890abcdef";
	SecureRandom random = new SecureRandom();
	
	private static File tmpP12 = new File(System.getProperty("java.io.tmpdir"), "Notary1.p12");
	private static File tmpJKS = new File(System.getProperty("java.io.tmpdir"), "trustedroots.jks");
	private static File clientP12 = new File(System.getProperty("java.io.tmpdir"), "Client1.p12");
	private static File clientCer = new File(System.getProperty("java.io.tmpdir"), "Client1.cer");
	private static File testP7S = new File(System.getProperty("java.io.tmpdir"), "test.p7s");
    
	@SuppressWarnings("static-access")
	public static void initExternalFiles() throws IOException {
		URL keyStorePath = RestTest.class.getClassLoader().getSystemResource("Notary1.p12");
        System.out.println("Copied " + keyStorePath);
        FileUtils.copyURLToFile(keyStorePath, tmpP12);
        
        URL clientStorePath = RestTest.class.getClassLoader().getSystemResource("Client1.p12");
        System.out.println("Copied " + clientStorePath);
        FileUtils.copyURLToFile(clientStorePath, clientP12);

        URL clientCertPath = RestTest.class.getClassLoader().getSystemResource("Client1.cer");
        System.out.println("Copied " + clientCertPath);
        FileUtils.copyURLToFile(clientCertPath, clientCer);

        URL signedFile = RestTest.class.getClassLoader().getSystemResource("test.p7s");
        System.out.println("Copied " + signedFile);
        FileUtils.copyURLToFile(signedFile, testP7S);
        
        URL trustedRootStorePath = RestTest.class.getClassLoader().getSystemResource("trustedroots.jks");
        System.out.println("Copied " + trustedRootStorePath);
        FileUtils.copyURLToFile(trustedRootStorePath, tmpJKS);
	}

    @Deployment
    public static WebArchive createDeployment() throws IOException {
    	initExternalFiles();    	
    	
    	return ShrinkWrap.create(WebArchive.class, "test-srv.war")

        		.addClass(org.jnotary.service.data.DvcsConfigurationManager.class)
        		.addClass(GlobalResources.class)
                .addPackage(DvcsRequestHandler.class.getPackage())
                .addPackage(DVCSException.class.getPackage())
                .addPackage(CryptoService.class.getPackage())
                .addPackage(DvcsResponseHelper.class.getPackage())
                .addPackage(Verifier.class.getPackage())
                .addPackage(DVCSRequest.class.getPackage())
                .addPackage(IDvcsConfigurationManager.class.getPackage())
                .addPackage(Configuration.class.getPackage())

                .addPackage(FileUtils.class.getPackage())
                .addPackages(true, "org.apache.commons.io")
                .addPackages(true, "org.codehaus.plexus.util")
                .addPackages(true, "org.bouncycastle")
                .addClass(org.bouncycastle.asn1.ASN1Integer.class)

                .addAsResource("trustedUsers.properties")

                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-ds.xml", "test-ds.xml");
    }
 
    @Before
    public  void setUp() throws Exception {
//        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

    }
    
    public void configureService() throws Exception {
        String password = "12345678"; 

        Configuration configuration = new Configuration();
		configuration.setKeyAlias("Notary1");
		configuration.setKeyAliasPassword(password);
		configuration.setKeyStorePassword(password);
		configuration.setKeyStorePath(tmpP12.getAbsolutePath());
		configuration.setTrustedRootStorePassword(password);
		configuration.setTrustedRootStorePath(tmpJKS.getAbsolutePath());
		configuration.setVerifyCRL(false);
		if(configurationManager == null)
			System.out.println("configurationManager == null");
		configurationManager.store(configuration);
		globalResources.initUserKeyStorage();
		globalResources.initServiceProperties();
		globalResources.initTrustedRoots();
	}
 
	private DVCSResponse removeSignature(byte[] signedData) throws Exception {
		Verifier verifier = new Verifier();		
		VerifyResult result = verifier.verifySignature(signedData, null);
		
		return DVCSResponse.getInstance(result.getContent());		
	}
    
	@Test
    public void testDvcsCallWithWrongData() throws Exception {
        configureService();
        
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(url + "rest/dvcs");
        target.request().accept(MediaType.APPLICATION_OCTET_STREAM);
    	Response response;
		try {
			response = target.request().post(Entity.entity(sampleData, MediaType.APPLICATION_OCTET_STREAM));
			byte[] value = response.readEntity(byte[].class);
            response.close();  // You should close connections!			
            Assert.assertEquals(200, response.getStatus());
			
    		DVCSResponse respIn = removeSignature(value);
    		assertEquals(PKIStatus.REJECTION, respIn.getDvErrorNote().getTransactionStatus().getStatus().intValue());			
    		System.out.printf("Status string: %s\n", respIn.getDvErrorNote().getTransactionStatus().getStatusString().getStringAt(0));
    		assertEquals(PKIFailureInfo.badMessageCheck, respIn.getDvErrorNote().getTransactionStatus().getFailInfo().intValue());			

		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testCpdCall() throws Exception {    	 
        configureService();
        
    	DVCSRequest reqOut = DvcsHelper.createCpd(sampleData.getBytes(), random.nextLong());
		
		byte[] requestData = sign(reqOut);
		
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(url + "rest/dvcs");
        target.request().accept(MediaType.APPLICATION_OCTET_STREAM);
    	Response response;
		try {
			response = target.request().post(Entity.entity(requestData, MediaType.APPLICATION_OCTET_STREAM));			
			byte[] value = response.readEntity(byte[].class);
            response.close();  // You should close connections!			
			Assert.assertEquals(200, response.getStatus());
			
    		DVCSResponse respIn = removeSignature(value);
    		assertNotNull(respIn.getDvCertInfo());
    		
    		assertEquals(ServiceType.CPD, respIn.getDvCertInfo().getRequestInformation().getService());
    		assertEquals(reqOut.getRequestInformation().getNonce(), respIn.getDvCertInfo().getRequestInformation().getNonce());
    		assertEquals(0, respIn.getDvCertInfo().getDvStatus().getStatus().intValue());
    				
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testCcpdCall() throws Exception {
        configureService();

        byte[] digestData = Hasher.makeHash(NISTObjectIdentifiers.id_sha256, sampleData.getBytes());
		DigestInfo messageImprint = new DigestInfo(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), digestData);
		DVCSRequest reqOut = DvcsHelper.createCcpd(messageImprint, random.nextLong());
		
		byte[] requestData = sign(reqOut);
		
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(url + "rest/dvcs");
        target.request().accept(MediaType.APPLICATION_OCTET_STREAM);
    	Response response;
		try {
			response = target.request().post(Entity.entity(requestData, MediaType.APPLICATION_OCTET_STREAM));			
			byte[] value = response.readEntity(byte[].class);
            response.close();  // You should close connections!			
			Assert.assertEquals(200, response.getStatus());
			
    		DVCSResponse respIn = removeSignature(value);
    		assertNotNull(respIn.getDvCertInfo());
    		
    		assertEquals(ServiceType.CCPD, respIn.getDvCertInfo().getRequestInformation().getService());
    		assertEquals(reqOut.getRequestInformation().getNonce(), respIn.getDvCertInfo().getRequestInformation().getNonce());
    		assertEquals(0, respIn.getDvCertInfo().getDvStatus().getStatus().intValue());
    		assertTrue("Digest is wrong", Arrays.equals(respIn.getDvCertInfo().getMessageImprint().getDigest(), Hasher.makeHash(NISTObjectIdentifiers.id_sha256, sampleData.getBytes())));
    				
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
   
    @Test
    public void testVsdCall() throws Exception {
        configureService();

        byte[] testData = loadFile(testP7S.getAbsolutePath());
		DVCSRequest reqOut = DvcsHelper.createVsd(testData, random.nextLong());
		
		byte[] requestData = sign(reqOut);
		
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(url + "rest/dvcs");
        target.request().accept(MediaType.APPLICATION_OCTET_STREAM);
    	Response response;
		try {
			response = target.request().post(Entity.entity(requestData, MediaType.APPLICATION_OCTET_STREAM));			
			byte[] value = response.readEntity(byte[].class);
            response.close();  // You should close connections!			
			Assert.assertEquals(200, response.getStatus());
			
    		DVCSResponse respIn = removeSignature(value);
    		assertNotNull(respIn.getDvCertInfo());
    		
    		assertEquals(ServiceType.VSD, respIn.getDvCertInfo().getRequestInformation().getService());
    		assertEquals(reqOut.getRequestInformation().getNonce(), respIn.getDvCertInfo().getRequestInformation().getNonce());
    		assertEquals(2, respIn.getDvCertInfo().getDvStatus().getStatus().intValue());
    				
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testVpkcCall() throws Exception {
        configureService();
    	byte[] testData = loadFile(clientCer.getAbsolutePath());
		DVCSRequest reqOut = DvcsHelper.createVpkc(testData, random.nextLong());
		
		byte[] requestData = sign(reqOut);
		
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target(url + "rest/dvcs");
        target.request().accept(MediaType.APPLICATION_OCTET_STREAM);
    	Response response;
		try {
			response = target.request().post(Entity.entity(requestData, MediaType.APPLICATION_OCTET_STREAM));			
			byte[] value = response.readEntity(byte[].class);
            response.close();  // You should close connections!
			Assert.assertEquals(200, response.getStatus());
			
    		DVCSResponse respIn = removeSignature(value);
    		assertNotNull(respIn.getDvCertInfo());
    		
    		assertEquals(ServiceType.VPKC, respIn.getDvCertInfo().getRequestInformation().getService());
    		assertEquals(reqOut.getRequestInformation().getNonce(), respIn.getDvCertInfo().getRequestInformation().getNonce());
    		assertEquals(2, respIn.getDvCertInfo().getDvStatus().getStatus().intValue());
    				
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	private byte[] sign(DVCSRequest request) throws Exception {
		return sign(request, true);
	}
	private byte[] sign(DVCSRequest request, boolean addCert) throws Exception {
		Signer signer = new Signer("SHA1withRSA");
		
		UserKeyStore userKeyStorage = new UserKeyStore(
				new FileStorage(clientP12.getAbsolutePath(), "PKCS12","12345678"),
				"Client1", "12345678");

		Signer.Parameters signerParameters = signer.getDefaultParameters();
		signerParameters.setAddSignerSertificate(addCert);
		signerParameters.setDetached(false);
		return signer.sign(userKeyStorage, request.getEncoded(), signerParameters);
	}
    private static byte[] loadFile(String path) throws Exception {
		InputStream stream = null;
		try {
			stream = new FileInputStream(new File(path));
			return IOUtils.toByteArray(stream);
		} catch (Exception e) {
			throw new Exception("Can't read file: " + e.getLocalizedMessage());
		} finally {
			if(stream != null)
				stream.close();
		}
    }    
}
