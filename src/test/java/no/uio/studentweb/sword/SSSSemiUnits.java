package no.uio.studentweb.sword;

import org.junit.*;
import static org.junit.Assert.*;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDCollection;

import java.util.List;

// This class assumes certain things about the SSS which makes them not real unit tests
public class SSSSemiUnits
{
	private String instituteServiceDocument = null;
	private String instituteServiceDocumentTemplate = null;
	private String studyProgramDeposit = null;
	private TemplateUrlSourceData templateUrlSourceData = null;
	private AuthCredentials swordAuth = null;

	@Before
	public void setUp()
			throws Exception
	{
		// FIXME: should read this all from some test config, or try to auto-locate
		// resources
		this.instituteServiceDocument = "http://localhost:8080/sd-uri";
		this.instituteServiceDocumentTemplate = "http://localhost:8080/{unit-code}";
		this.studyProgramDeposit= "http://localhost:8080/col-uri/ded3c328-d8f0-4307-8b86-086cf46a953c";
		this.templateUrlSourceData = new MockTemplateUrlSourceData();
		this.swordAuth = new AuthCredentials("sword", "sword");
	}

	@Test
	public void testEndpointWithValidCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(1, cols.size());

		SWORDCollection col = cols.get(0);
		assertEquals(this.studyProgramDeposit, col.getHref().toString());
	}

	@Test
	public void testEndpointWithoutValidCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				"http://whatever/", this.templateUrlSourceData, this.swordAuth
		);

		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(10, cols.size());
	}

	@Test
	public void testEndpointWithoutCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				null, this.templateUrlSourceData, this.swordAuth
		);

		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(10, cols.size());
	}

	@Test
	public void testEndpointWithoutService()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				null, null,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		boolean raised = false;
		try
		{
			List<SWORDCollection> cols = ed.getEndpoints();
		}
		catch (NoServiceException e)
		{
			raised = true;
		}
		assertTrue(raised);
	}

	@Test
	public void testServiceDocGenerate()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				null, this.instituteServiceDocumentTemplate,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		List<SWORDCollection> cols = ed.getEndpoints();

		assertEquals(this.instituteServiceDocument, ed.getInstituteServiceDocument());
	}

	@Test
	public void testSwordClientUnauthorised()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, null,
				this.studyProgramDeposit, this.templateUrlSourceData, null
		);

		boolean raised = false;
		try
		{
			List<SWORDCollection> cols = ed.getEndpoints();
		}
		catch (NoServiceException e)
		{
			raised = true;
		}
		assertTrue(raised);
	}
}
