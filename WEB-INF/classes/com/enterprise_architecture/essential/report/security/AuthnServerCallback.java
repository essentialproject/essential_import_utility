/**
 * Copyright (c)2015-2018 Enterprise Architecture Solutions ltd.
 * This file is part of Essential Architecture Manager, 
 * the Essential Architecture Meta Model and The Essential Project.
 *
 * Essential Architecture Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Essential Architecture Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Essential Architecture Manager.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 24.04.2018	DK	First coding
 * 23.08.2018	JWC Replaced HTTPServlet import as part of upgrade to ZK 8.5
 */
package com.enterprise_architecture.essential.report.security;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.zkoss.web.servlet.http.HttpServlet;

/**
 * Servlet implementation class AuthnServerCallback. Receives a redirect back from the AuthN Server
 * containing tokens for the authenticated user.
 */
public class AuthnServerCallback extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AuthnServerCallback() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			//we always redirect to home page on successful authN in Import Utility
			response.sendRedirect("home_eip.zul");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	
	

	
}
