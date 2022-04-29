/**
 * Copyright (c)2015-2016 Enterprise Architecture Solutions Ltd.
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
 * 
 */
package com.enterprise_architecture.essential.importutility.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Class to manage the calls to #getRealPath() method to handle incorrect calls.
 * 
 * @author Jonathan Carter
 *
 */
public class EssentialServletContext implements ServletContext 
{
	/**
	 * The actual servlet context that is being intercepted
	 */
	protected ServletContext itsServletContext = null;
	
	/**
	 * The 'correct' root of the relative path
	 */
	protected final String PATH_ROOT = "/";
	
	/**
	 * Default constructor.
	 * @param theContext the servlet context that is to be intercepted
	 */
	public EssentialServletContext(ServletContext theContext) 
	{
		itsServletContext = theContext;
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Object getAttribute(String arg0) 
	{
		return itsServletContext.getAttribute(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Enumeration<String> getAttributeNames() 
	{		
		return itsServletContext.getAttributeNames();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public ServletContext getContext(String arg0) 
	{	
		return itsServletContext.getContext(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public String getInitParameter(String arg0) 
	{
		return itsServletContext.getInitParameter(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Enumeration<String> getInitParameterNames() 
	{
		return itsServletContext.getInitParameterNames();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public int getMajorVersion() 
	{
		return itsServletContext.getMajorVersion();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public String getMimeType(String arg0) 
	{
		return itsServletContext.getMimeType(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public int getMinorVersion() 
	{
		return itsServletContext.getMinorVersion();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) 
	{
		return itsServletContext.getNamedDispatcher(arg0);
	}

	/**
	 * Intercept call to ensure that the relative path supplied is correctly formatted.
	 * An empty or null string will be replaced by '/'. Any relative path supplied that
	 * does not start with '/' has the '/' added to the start of the path.
	 * @param theRelativePath the relative path to the element for which the full path (real path) is
	 * being requested. 
	 */
	@Override
	public String getRealPath(String theRelativePath) 
	{
		String aRelPath = PATH_ROOT;
		
		// Defensive programming.
		if(theRelativePath != null && theRelativePath.length() > 0)
		{
			// Check that theRelativePath starts with PATH_ROOT
			if(!theRelativePath.startsWith(PATH_ROOT))
			{
				aRelPath = PATH_ROOT + theRelativePath;
			}
			else
			{
				aRelPath = theRelativePath;
			}			
		}
		
		// TRACE
		//System.out.println("Doing intercepted getRealPath() for: " + aRelPath);
		
		// Return the correct value from the servlet context
		return itsServletContext.getRealPath(aRelPath);
		
	}
	
	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) 
	{
		return itsServletContext.getRequestDispatcher(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public URL getResource(String arg0) throws MalformedURLException 
	{
		return itsServletContext.getResource(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public InputStream getResourceAsStream(String arg0) 
	{
		return itsServletContext.getResourceAsStream(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Set<String> getResourcePaths(String arg0) 
	{
		return itsServletContext.getResourcePaths(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public String getServerInfo() 
	{
		return itsServletContext.getServerInfo();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Servlet getServlet(String arg0) throws ServletException 
	{
		return itsServletContext.getServlet(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public String getServletContextName() 
	{
		return itsServletContext.getServletContextName();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Enumeration getServletNames() 
	{
		return itsServletContext.getServletNames();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public Enumeration getServlets() 
	{
		return itsServletContext.getServlets();
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public void log(String arg0) 
	{
		itsServletContext.log(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public void log(Exception arg0, String arg1) 
	{
		itsServletContext.log(arg0, arg1);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public void log(String arg0, Throwable arg1) 
	{
		itsServletContext.log(arg0, arg1);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public void removeAttribute(String arg0) 
	{
		itsServletContext.removeAttribute(arg0);
	}

	/**
	 * Pass call through to the contained servlet context 
	 */
	@Override
	public void setAttribute(String arg0, Object arg1) 
	{
		itsServletContext.setAttribute(arg0, arg1);
	}
	
	@Override
	public Dynamic addFilter(String theFilterName, String theClassName) 
	{
		return itsServletContext.addFilter(theFilterName, theClassName);
	}

	@Override
	public Dynamic addFilter(String theFilterName, Filter theFilter) 
	{		
		return itsServletContext.addFilter(theFilterName, theFilter);
	}

	@Override
	public Dynamic addFilter(String theFilterName, Class<? extends Filter> theFilterClass) 
	{		
		return itsServletContext.addFilter(theFilterName, theFilterClass);
	}

	@Override
	public void addListener(Class<? extends EventListener> theListenerClass) 
	{
		itsServletContext.addListener(theListenerClass);
		
	}

	@Override
	public void addListener(String theClassName) 
	{
		itsServletContext.addListener(theClassName);
		
	}

	@Override
	public <T extends EventListener> void addListener(T arg0) 
	{
		itsServletContext.addListener(arg0);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String theServletName,
																String theClassName) 
	{
		return itsServletContext.addServlet(theServletName, theClassName);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String theServletName,
																Servlet theServlet) 
	{
		return itsServletContext.addServlet(theServletName, theServlet);
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String theServletName,
																Class<? extends Servlet> theServletClass) 
	{
		return itsServletContext.addServlet(theServletName, theServletClass);
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> theClass) throws ServletException 
	{		
		return itsServletContext.createFilter(theClass);
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> theClass) throws ServletException 
	{
		return itsServletContext.createListener(theClass);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> theClass) throws ServletException 
	{	
		return itsServletContext.createServlet(theClass);
	}

	@Override
	public void declareRoles(String... theRoleNames) 
	{
		itsServletContext.declareRoles(theRoleNames);
		
	}

	@Override
	public ClassLoader getClassLoader() 
	{	
		return itsServletContext.getClassLoader();
	}

	@Override
	public String getContextPath() 
	{
		return itsServletContext.getContextPath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() 
	{
		return itsServletContext.getDefaultSessionTrackingModes();
	}

	@Override
	public int getEffectiveMajorVersion() 
	{
		return itsServletContext.getEffectiveMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() 
	{
		return itsServletContext.getEffectiveMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() 
	{
		return itsServletContext.getEffectiveSessionTrackingModes();
	}

	@Override
	public FilterRegistration getFilterRegistration(String theFilterName) 
	{
		return itsServletContext.getFilterRegistration(theFilterName);
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() 
	{
		return itsServletContext.getFilterRegistrations();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() 
	{
		return itsServletContext.getJspConfigDescriptor();
	}

	@Override
	public ServletRegistration getServletRegistration(String theServletName)
	{
		return itsServletContext.getServletRegistration(theServletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() 
	{
		return itsServletContext.getServletRegistrations();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() 
	{
		return itsServletContext.getSessionCookieConfig();
	}

	@Override
	public boolean setInitParameter(String theName, String theValue) 
	{
		return itsServletContext.setInitParameter(theName, theValue);
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> theSessionTrackingModes)
				throws IllegalStateException, IllegalArgumentException 
	{
		itsServletContext.setSessionTrackingModes(theSessionTrackingModes);
	}

	/**
	 * This is an override for Tomcat 8.x target platforms
	 * Removed the @override so that it compiles with Tomcat 7.x, too
	 * @return
	 */
	public String getVirtualServerName() {
		// TODO Auto-generated method stub
		return "Essential Import Utility";
	}
	
	/*@Override
	public String getVirtualServerName()
	{
		return "TEST";
	}*/

}
