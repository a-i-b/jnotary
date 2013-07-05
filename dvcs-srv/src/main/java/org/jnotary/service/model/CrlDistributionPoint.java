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
import java.lang.Integer;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: CRL
 *
 */
@Entity
@Table
public class CrlDistributionPoint implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
	private Long id;
	
	private Integer issuerHash;
	private String	issuerDescription;
	private String	crlUrl;
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getIssuerHash() {
		return issuerHash;
	}

	public void setIssuerHash(Integer issuerHash) {
		this.issuerHash = issuerHash;
	}

	public String getCrlUrl() {
		return crlUrl;
	}

	public void setCrlUrl(String crlUrl) {
		this.crlUrl = crlUrl;
	}

	public String getIssuerDescription() {
		return issuerDescription;
	}

	public void setIssuerDescription(String issuerDescription) {
		this.issuerDescription = issuerDescription;
	}
   
}
