/*
	Copyright (c) 2012, University of Oslo

	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:
		* Redistributions of source code must retain the above copyright
		  notice, this list of conditions and the following disclaimer.
		* Redistributions in binary form must reproduce the above copyright
		  notice, this list of conditions and the following disclaimer in the
		  documentation and/or other materials provided with the distribution.
		* Neither the name of the University of Oslo nor the
		  names of its contributors may be used to endorse or promote products
		  derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF OSLO BE LIABLE FOR ANY
	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.uio.studentweb.sword;

import org.swordapp.client.AuthCredentials;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.SWORDWorkspace;
import org.swordapp.client.ServiceDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for carrying out EndpointDiscovery operations
 *
 * The class should be constructed with all the relevant known information (see Constructor
 * documentation), and then getEndpoints() can be called.
 *
 * This class uses the SWORDClient
 */
public class EndpointDiscovery
{
	/** The service document associated with the institute */
	private String instituteServiceDocument = null;

	/** The service document template associated with the institute */
	private String instituteServiceDocumentTemplate = null;

	/** The deposit url for the study program */
	private String studyProgramDeposit = null;

	/** An instance of a class which implements the TemplateUrlSourceData interface */
	private TemplateUrlSourceData templateUrlSourceData = null;

	/** The SWORD Client's AuthCredentials object */
	private AuthCredentials swordAuth = null;

	/**
	 * Construct a blank EndpointDiscovery object.  This will be of no use
	 * directly, so use the getters and setters to populate as you require
	 */
	public EndpointDiscovery() { }

	/**
	 * Construct an EndpointDiscovery with all of its available parameters.  Nulls may be passed
	 * in, or use the blank constructor and getters and setters if you require a sub-set of the
	 * total allowed argument set
	 *
	 * @param instituteServiceDocument	The service document associated with the institute
	 * @param instituteServiceDocumentTemplate	The service document template associated with the institute
	 * @param studyProgramDeposit	The deposit url for the study program
	 * @param templateUrlSourceData		An instance of a class which implements the TemplateUrlSourceData interface
	 * @param swordAuth		The SWORD Client's AuthCredentials object
	 */
	public EndpointDiscovery(String instituteServiceDocument, String instituteServiceDocumentTemplate,
							 String studyProgramDeposit, TemplateUrlSourceData templateUrlSourceData,
							 AuthCredentials swordAuth)
	{
		this.instituteServiceDocument = instituteServiceDocument;
		this.instituteServiceDocumentTemplate = instituteServiceDocumentTemplate;
		this.studyProgramDeposit = studyProgramDeposit;
		this.templateUrlSourceData = templateUrlSourceData;
		this.swordAuth = swordAuth;
	}

	public String getInstituteServiceDocument()
	{
		return instituteServiceDocument;
	}

	public void setInstituteServiceDocument(String instituteServiceDocument)
	{
		this.instituteServiceDocument = instituteServiceDocument;
	}

	public String getInstituteServiceDocumentTemplate()
	{
		return instituteServiceDocumentTemplate;
	}

	public void setInstituteServiceDocumentTemplate(String instituteServiceDocumentTemplate)
	{
		this.instituteServiceDocumentTemplate = instituteServiceDocumentTemplate;
	}

	public String getStudyProgramDeposit()
	{
		return studyProgramDeposit;
	}

	public void setStudyProgramDeposit(String studyProgramDeposit)
	{
		this.studyProgramDeposit = studyProgramDeposit;
	}

	public TemplateUrlSourceData getTemplateUrlSourceData()
	{
		return templateUrlSourceData;
	}

	public void setTemplateUrlSourceData(TemplateUrlSourceData templateUrlSourceData)
	{
		this.templateUrlSourceData = templateUrlSourceData;
	}

	public AuthCredentials getSwordAuth()
	{
		return swordAuth;
	}

	public void setSwordAuth(AuthCredentials swordAuth)
	{
		this.swordAuth = swordAuth;
	}

	/**
	 * Get a list of SWORDCollection objects representing the available endpoints for the
	 * configuration of the current EndpointDiscovery object.
	 *
	 * This will return one of two sets of things:
	 *
	 * 1/	A list with only one SWORDCollection object, which represents the studyProgramDeposit
	 * 		url member variable.  This happens if the studyProgramDeposit member variable is
	 * 		successfully validated against the retrieved service document.
	 *
	 * 2/	A list with an arbitrary number (zero or more) SWORDCollection objects which represent
	 * 		the available collections for deposit.  This happens if the studyProgramDeposit member
	 * 		variable is either null or unsuccessful in validating against the retrieved service document
	 *
	 * FIXME: this currently only deals with flat service documents - hierarchical service documents are
	 * 		not supported
	 *
	 * @return	A list of zero or more SWORDCollection objects
	 * @throws NoServiceException	if the service document url cannot be obtained, or the service document itself cannot be resolved
	 * @throws DiscoveryConfigurationException		if the member variables of the object are insufficient to carry out the endpoint discovery
	 * @throws SWORDClientException		re-throws exceptions from the SWORDClient
	 * @throws TemplateUrlPropertyException		if the instituteServiceDocumentTemplate contains fields which the source data cannot interpret
	 */
	public List<SWORDCollection> getEndpoints()
			throws NoServiceException, DiscoveryConfigurationException, SWORDClientException, TemplateUrlPropertyException
	{
		// if both instiututeServiceDocument and instituteServiceDocumentTemplate are null, we can't
		// obtain a deposit endpoint
		if (this.instituteServiceDocument == null && this.instituteServiceDocumentTemplate == null)
		{
			throw new NoServiceException("There is no Service Document or Service Document template");
		}

		// next, we have to generate the service document url if it is not already done
		// we will use this to either validate the studyProgramDeposit url (if there
		// is one) or generate the list of available collections
		if (this.instituteServiceDocument == null)
		{
			this.instituteServiceDocument = this.generateServiceDocumentUrl();
		}

		// go through the service document, and make two lists: one of all collections,
		// and the other containing zero or one elements which represent the collection
		// identified by the studyProgramDeposit
		List<SWORDCollection> allCollections = new ArrayList<SWORDCollection>();
		List<SWORDCollection> onlyOne = new ArrayList<SWORDCollection>();
		try
		{
			SWORDClient client = new SWORDClient();
			ServiceDocument sd = client.getServiceDocument(this.instituteServiceDocument, this.swordAuth);

			if (sd == null)
			{
				throw new NoServiceException("The sword server did not respond with a service document");
			}

			for (SWORDWorkspace workspace : sd.getWorkspaces())
			{
				// store all the collections in one big array
				allCollections.addAll(workspace.getCollections());
				for (SWORDCollection collection : workspace.getCollections())
				{
					if (this.studyProgramDeposit != null && collection.getHref().toString().equals(this.studyProgramDeposit))
					{
						// we have successfully validated, so we can return the onlyOne list
						onlyOne.add(collection);
						return onlyOne;
					}
				}
			}
		}
		catch (ProtocolViolationException e)
		{
			throw new SWORDClientException(e);
		}

		// if we get to here studyProgramDeposit was either null or wasn't a valid url
		// in either case, just give back the list of collections

		return allCollections;
	}

	/**
	 * Generate the service document url from the instituteServiceDocumentTemplate member variable
	 *
	 * This takes URLs which have replaceable elements wrapped by { and }, such as
	 * "http://some.url.com/{unit-code}", and looks up the replaceable elements in the
	 * TemplateUrlSourceData implementation held as a member variable.  All such templated
	 * fields are interpreted and the URL returned.
	 *
	 * @return	the interpreted/replaced url
	 * @throws DiscoveryConfigurationException	if the member variables of the object are insufficient to carry out the endpoint discovery
	 * @throws TemplateUrlPropertyException	if the instituteServiceDocumentTemplate contains fields which the source data cannot interpret
	 */
	private String generateServiceDocumentUrl()
			throws DiscoveryConfigurationException, TemplateUrlPropertyException
	{
		if (this.templateUrlSourceData == null)
		{
			throw new DiscoveryConfigurationException("Attempting to generate service document from template, but no TemplateUrlSourceData supplied");
		}

		if (this.instituteServiceDocumentTemplate == null)
		{
			throw new DiscoveryConfigurationException("Attempting to generate service document from template, but no template supplied");
		}

		// do template interpretation ...
		String t = this.instituteServiceDocumentTemplate; // for convenience
		while (true)
		{
			int a = t.indexOf("{");
			if (a == -1) { break; }
			int b = t.indexOf("}", a + 1);
			if (b == -1)
			{
				throw new TemplateUrlPropertyException("Opening { detected, but no equivalent closing brace");
			}
			String key = t.substring(a + 1, b);
			String substitute = "";
			if (!"".equals(key))
			{
				substitute = this.templateUrlSourceData.getTemplateProperty(key);
			}
			t = t.substring(0, a) + substitute + t.substring(b + 1);
		}

		return t;
	}
}
