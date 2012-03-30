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

public class EndpointDiscovery
{
	private String instituteServiceDocument = null;
	private String instituteServiceDocumentTemplate = null;
	private String studyProgramDeposit = null;
	private TemplateUrlSourceData templateUrlSourceData = null;
	private AuthCredentials swordAuth = null;

	public EndpointDiscovery() { }

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
			if (b == -1) { break; }
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
