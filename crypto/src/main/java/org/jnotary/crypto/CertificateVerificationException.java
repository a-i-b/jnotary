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
package org.jnotary.crypto;

public class CertificateVerificationException extends Exception {
	private static final long serialVersionUID = 1L;

	public CertificateVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

	public CertificateVerificationException(String message) {
        super(message);
    }
}
