/**
 * Copyright (c)2017 Enterprise Architecture Solutions Ltd.
 *
 * 23.01.2017	JWC	1st coding.
 * 21.07.2017	JWC Re-worked the URL for the new domain approach
 *
 * @file 
 * Implements a redirect to the SAML login component of the Essential Intelligence Platform.
 * 
 * Computes the correct URL to which the user should be returned after authenticating via SAML.
 * 
 * @returns {Boolean}
 */
function samlLogin()
{
	var url= window.location.protocol + "//" + window.location.host + "/app/samlLogin?appRedirectUrl=" + window.location.href;
	window.open(url, "_self");
	event.preventDefault();
	return false;
}					
