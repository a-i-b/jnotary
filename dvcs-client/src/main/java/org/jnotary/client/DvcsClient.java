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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.jnotary.crypto.Hasher;
import org.jnotary.crypto.util.CryptoHelper;
import org.jnotary.crypto.util.ClientCryptoConfig;
import org.jnotary.dvcs.DVCSRequest;
import org.jnotary.dvcs.DVCSResponse;
import org.jnotary.dvcs.util.DvcsHelper;


public class DvcsClient {
	private static SecureRandom random = new SecureRandom();
	
    public static void main(String[] args) throws IOException {
    	
    	Options options = new Options();
    	options.addOption(createOption("k", "key-store-file", "path to key store description file", true, true));    	
    	options.addOption(createOption("u", "url", "dvcs service URL", true, true));    	
    	options.addOption(createOption("t", "service-type", "service type: cpd | ccpd | vpkc | vsd", true, true));    	
    	options.addOption(createOption("n", "nonce", "dvcs nonce (optional, long)", true, false));    	
    	
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        String[] files = null;
        String configPath = null;
        String dvcsUrl = null;
        String serviceType = null;
        Long nonce = null;
        try {
            line = parser.parse( options, args );
            configPath = line.getOptionValue("k");
            dvcsUrl = line.getOptionValue("u");
            serviceType = line.getOptionValue("t");
            nonce = null;
            if(line.hasOption("n")) {
            	try {
            	nonce = Long.parseLong(line.getOptionValue("n"));
            	} catch(Exception e) {
                    System.err.println( "Bad parameters. Reason: " + e.getMessage() );
                    return;
            	}
            }
            else
    			nonce = random.nextLong();

            files = line.getArgs();
            if(files == null || files.length != 2)
            	throw new Exception("File name parametres are not present");
        }
        catch( Exception e ) {
            System.err.println( "Bad parameters. Reason: " + e.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "DvcsClient options input-file output-file", options);            
            return;
        }

        DVCSRequest reqOut = null;
        ClientCryptoConfig config = new ClientCryptoConfig();
        try {
			config.load(configPath);
	        byte[] inputData = loadFile(line.getArgs()[0]);

			if(serviceType.equals("cpd"))
				reqOut = DvcsHelper.createCpd(inputData, nonce);
			
			if(serviceType.equals("vsd"))
				reqOut = DvcsHelper.createVsd(inputData, nonce);
			
			if(serviceType.equals("vpkc"))
				reqOut = DvcsHelper.createVpkc(inputData, nonce);
			
			if(serviceType.equals("ccpd")) {
				byte[] digestData = Hasher.makeHash(config.getHashAlgorithm().getAlgorithm(), inputData);
				DigestInfo messageImprint = new DigestInfo(config.getHashAlgorithm(), digestData);
				reqOut = DvcsHelper.createCcpd(messageImprint, nonce);
			}
			
			byte[] requestData = CryptoHelper.sign(reqOut.getEncoded(), config);
			byte[] responseData = RestHelper.call(dvcsUrl, requestData);
			DVCSResponse response = DVCSResponse.getInstance(CryptoHelper.removeSignature(responseData));
			
			dump(response);
			saveFile(line.getArgs()[1], responseData);
			
		} catch (Exception e) {
            System.err.println(e.getMessage());
		}
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
    
	private static void dump(DVCSResponse response) throws Exception {
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

    private static void saveFile(String path, byte[] data) throws Exception {
    	FileOutputStream output = null;
		try {
			output = new FileOutputStream(new File(path));
			IOUtils.write(data, output);
		} catch (Exception e) {
			throw new Exception("Can't save file: " + e.getLocalizedMessage());
		} finally {
			if(output != null)
				output.close();
		}
    }
}
