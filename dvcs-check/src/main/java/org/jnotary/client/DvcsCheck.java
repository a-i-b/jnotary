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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.cms.SignerInformation;
import org.jnotary.crypto.FileStorage;
import org.jnotary.crypto.Hasher;
import org.jnotary.crypto.TrustedStore;
import org.jnotary.crypto.Verifier;
import org.jnotary.crypto.Verifier.VerifyResult;
import org.jnotary.crypto.util.CryptoHelper;
import org.jnotary.crypto.util.ClientCryptoConfig;
import org.jnotary.dvcs.DVCSResponse;


public class DvcsCheck {
	private static ClientCryptoConfig config = null;
	
    public static void main(String[] args) throws IOException {
    	
    	Options options = new Options();
    	options.addOption(createOption("k", "key-store-file", "path to key store description file", true, true));    	
    	
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        String[] files = null;
        String configPath = null;
        try {
            line = parser.parse( options, args );
            configPath = line.getOptionValue("k");

            files = line.getArgs();
            if(files == null || files.length != 2)
            	throw new Exception("File name parametres are not present");
        }
        catch( Exception e ) {
            System.err.println( "Bad parameters. Reason: " + e.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "DvcsCheck options source-file dvcs-response-file", options);            
            return;
        }

        config = new ClientCryptoConfig();
        try {
			config.load(configPath);
	        byte[] srcData = loadFile(line.getArgs()[0]);			
			byte[] dvcsData = loadFile(line.getArgs()[1]);
			
			VerifyResult dvcsReqVerifyResult = verifySignature(dvcsData);
			verifyCerificates(dvcsReqVerifyResult);
			System.out.println("DVCS file signature  and certificate are successfully verified");			

			DVCSResponse response = DVCSResponse.getInstance(CryptoHelper.removeSignature(dvcsData));			
			byte[] digestData = Hasher.makeHash(config.getHashAlgorithm().getAlgorithm(), srcData);
			verifyAndDump(digestData, response);			
		} catch (Exception e) {
            System.err.println(e.getMessage());
		}
    }
	
    private static void verifyCerificates(VerifyResult dvcsReqVerifyResult) throws Exception {
		TrustedStore trustedRoots = new TrustedStore(
				new FileStorage(
						config.getTrustedStorePath(),
						"JKS",
						config.getTrustedStorePassword()));

    	Verifier verifier = new Verifier();		
		for(SignerInformation signerInfo: dvcsReqVerifyResult.getSigners()) {
			X509Certificate cert = dvcsReqVerifyResult.getSignerCertificate(signerInfo.getSID());
			verifier.verifyCertificate(trustedRoots, cert);
		}

	}

	private static VerifyResult verifySignature(byte[] dvcsData) throws Exception {
    	Verifier verifier = new Verifier();		
		return verifier.verifySignature(dvcsData, null);
	}

    @SuppressWarnings("static-access")	
	private static Option createOption(String shortOptionName, String optionName, String description, boolean hasValue, boolean isMandatory )
	{
		OptionBuilder opt = OptionBuilder.withLongOpt(optionName)
			.withArgName(shortOptionName)
			.withDescription(description);
		if( hasValue ) 
			opt = opt.hasArg();
		if( isMandatory ) 
			opt = opt.isRequired();
		return opt.create(shortOptionName);
	}    
    
	private static void verifyAndDump(byte[] hash, DVCSResponse response) throws Exception {
		if(Arrays.equals(hash, response.getDvCertInfo().getMessageImprint().getDigest())) {
			System.out.println("Message imprint is successfully verified");			
		} else {
			System.out.println("Message imprint verification is FAILED");
			
			System.out.println("Message imprint of source file:");			
			HexDump.dump(hash, 0, System.out, 0);
			System.out.println("\nMessage imprint from dvcs-response file:");			
			HexDump.dump(response.getDvCertInfo().getMessageImprint().getDigest(), 0, System.out, 0);
		}
		
		System.out.println("DVCS-response information");
		if(response.getDvCertInfo() != null) {
			System.out.println("Service type:" + response.getDvCertInfo().getRequestInformation().getService());
			System.out.println("Nonce: " + response.getDvCertInfo().getRequestInformation().getNonce().getPositiveValue().toString(16));
			System.out.println("Response time: " + response.getDvCertInfo().getResponseTime().getGenTime().getTimeString());
		}
		
		PKIStatusInfo statusInfo = null;
		if(response.getDvErrorNote() != null)
			statusInfo = response.getDvErrorNote().getTransactionStatus();
		else if(response.getDvCertInfo() != null && response.getDvCertInfo().getDvStatus() != null)
			statusInfo = response.getDvCertInfo().getDvStatus();
		if(statusInfo == null)
			throw new Exception("Status info is not present");
		
		java.lang.StringBuilder sb = new StringBuilder("PKIStatus: ");
		sb.append(statusInfo.getStatus());		
		if(statusInfo.getStatusString() != null) {
			sb.append("; FreeText: ");
			sb.append(statusInfo.getStatusString().getStringAt(0).getString());
		}					
		if(statusInfo.getFailInfo() != null) {
			sb.append("; PKIFailerInfo: ");
			sb.append(statusInfo.getFailInfo().intValue());
		}
		System.out.println(sb.toString());
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
