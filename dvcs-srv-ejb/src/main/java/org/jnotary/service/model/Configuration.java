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
package org.jnotary.service.model;

import java.io.Serializable;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Configuration
 *
 */
@Entity
@Table
public class Configuration implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Integer id;

	private Boolean verifyCRL = false;
	private Boolean addSertificateToSignature = true;
	private String hashAlgorithm = "SHA-256";
	private String signatureAlgorithm = "SHA1withRSA";
	private String keyStorePath = "";
	@Transient
	private String keyStoreType = "PKCS12";
	private String keyStorePassword = "";
	private String keyAlias = "";
	private String keyAliasPassword = "";
	private String trustedRootStorePath = "";
	private String trustedRootStorePassword = "";
	@Transient
	private String trustedRootStoreType = "JKS";
	private Boolean cpdAllowed = true;
	private Boolean ccpdAllowed = true;
	private Boolean vsdAllowed = true;
	private Boolean vpkcAllowed = true;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Boolean getVerifyCRL() {
		return verifyCRL;
	}
	public void setVerifyCRL(Boolean verifyCRL) {
		this.verifyCRL = verifyCRL;
	}
	public Boolean getAddSertificateToSignature() {
		return addSertificateToSignature;
	}
	public void setAddSertificateToSignature(Boolean addSertificateToSignature) {
		this.addSertificateToSignature = addSertificateToSignature;
	}
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}
	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}
	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}
	public String getKeyStorePath() {
		return keyStorePath;
	}
	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}
	public String getKeyStoreType() {
		return keyStoreType;
	}
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	public String getKeyStorePassword() {
		return keyStorePassword;
	}
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}
	public String getKeyAlias() {
		return keyAlias;
	}
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	public String getKeyAliasPassword() {
		return keyAliasPassword;
	}
	public void setKeyAliasPassword(String keyAliasPassword) {
		this.keyAliasPassword = keyAliasPassword;
	}
	public String getTrustedRootStorePath() {
		return trustedRootStorePath;
	}
	public void setTrustedRootStorePath(String trustedRootStorePath) {
		this.trustedRootStorePath = trustedRootStorePath;
	}
	public String getTrustedRootStorePassword() {
		return trustedRootStorePassword;
	}
	public void setTrustedRootStorePassword(String trustedRootStorePassword) {
		this.trustedRootStorePassword = trustedRootStorePassword;
	}
	public String getTrustedRootStoreType() {
		return trustedRootStoreType;
	}
	public void setTrustedRootStoreType(String trustedRootStoreType) {
		this.trustedRootStoreType = trustedRootStoreType;
	}
	public Boolean getCpdAllowed() {
		return cpdAllowed;
	}
	public void setCpdAllowed(Boolean cpdAllowed) {
		this.cpdAllowed = cpdAllowed;
	}
	public Boolean getCcpdAllowed() {
		return ccpdAllowed;
	}
	public void setCcpdAllowed(Boolean ccpdAllowed) {
		this.ccpdAllowed = ccpdAllowed;
	}
	public Boolean getVsdAllowed() {
		return vsdAllowed;
	}
	public void setVsdAllowed(Boolean vsdAllowed) {
		this.vsdAllowed = vsdAllowed;
	}
	public Boolean getVpkcAllowed() {
		return vpkcAllowed;
	}
	public void setVpkcAllowed(Boolean vpkcAllowed) {
		this.vpkcAllowed = vpkcAllowed;
	}

}
