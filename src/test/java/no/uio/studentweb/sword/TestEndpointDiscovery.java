package no.uio.studentweb.sword;

import org.junit.*;
import org.swordapp.client.AuthCredentials;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class TestEndpointDiscovery
{
	private String instituteServiceDocument = null;
	private String instituteServiceDocumentTemplate = null;
	private String studyProgramDeposit = null;
	private TemplateUrlSourceData templateUrlSourceData = null;
	private AuthCredentials swordAuth = null;

	private String brokenTemplate = null;
	private String nullTemplate = null;
	private String nullTemplateResolved = null;
	private String difficultTemplate = null;
	private String difficultTemplateResolved = null;
	private String unacceptableKey = null;

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

		this.brokenTemplate = "http://localhost:8080/{whatever";
		this.nullTemplate = "http://localhost:8080/{}";
		this.nullTemplateResolved = "http://localhost:8080/";
		this.difficultTemplate = "http://localhost:8080/{unit-code}/{other}";
		this.difficultTemplateResolved = "http://localhost:8080/sd-uri/other";
		this.unacceptableKey = "http://localhost:8080/{something}";
	}

	@Test
    public void discoveryInit()
    {
        EndpointDiscovery ed = new EndpointDiscovery();

		assertNull(ed.getInstituteServiceDocument());
		assertNull(ed.getInstituteServiceDocumentTemplate());
		assertNull(ed.getStudyProgramDeposit());
		assertNull(ed.getSwordAuth());
		assertNull(ed.getTemplateUrlSourceData());

		EndpointDiscovery ed1 = new EndpointDiscovery(
			this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
			this.studyProgramDeposit, this.templateUrlSourceData,
			this.swordAuth
		);

		assertEquals(this.instituteServiceDocument, ed1.getInstituteServiceDocument());
		assertEquals(this.instituteServiceDocumentTemplate, ed1.getInstituteServiceDocumentTemplate());
		assertEquals(this.studyProgramDeposit, ed1.getStudyProgramDeposit());
		assertEquals(this.templateUrlSourceData, ed1.getTemplateUrlSourceData());
		assertEquals(this.swordAuth, ed1.getSwordAuth());
    }

	@Test
	public void testUrlGeneration()
			throws Exception
	{
		Method method = EndpointDiscovery.class.getDeclaredMethod("generateServiceDocumentUrl", new Class[0]);
		method.setAccessible(true);

		// construct an endpoint which only has enough info to test the service document url generation
		EndpointDiscovery ed = new EndpointDiscovery(null, this.instituteServiceDocumentTemplate, null, this.templateUrlSourceData, null);
		String newUrl = (String) method.invoke(ed, new Class[0]);

		assertEquals(this.instituteServiceDocument, newUrl);

		// now try a more difficult one
		EndpointDiscovery ed1 = new EndpointDiscovery(null, this.difficultTemplate, null, this.templateUrlSourceData, null);
		newUrl = (String) method.invoke(ed1, new Class[0]);

		assertEquals(this.difficultTemplateResolved, newUrl);
	}

	@Test
	public void testBreakUrlGeneration()
			throws Exception
	{
		Method method = EndpointDiscovery.class.getDeclaredMethod("generateServiceDocumentUrl", new Class[0]);
		method.setAccessible(true);

		// test to see if an incorrectly constructed item generates configuration exceptions
		EndpointDiscovery ed0 = new EndpointDiscovery();
		boolean raised = false;
		try
		{
			method.invoke(ed0, new Class[0]);
		}
		catch (InvocationTargetException e)
		{
			assertTrue(e.getTargetException() instanceof DiscoveryConfigurationException);
			raised = true;
		}
		assertTrue(raised);

		// test to see if an incorrectly constructed item generates configuration exceptions
		EndpointDiscovery ed1 = new EndpointDiscovery(null, null, null, this.templateUrlSourceData, null);
		raised = false;
		try
		{
			method.invoke(ed1, new Class[0]);
		}
		catch (InvocationTargetException e)
		{
			assertTrue(e.getTargetException() instanceof DiscoveryConfigurationException);
			raised = true;
		}
		assertTrue(raised);

		// test to see if a broken url template is not affected by the process
		EndpointDiscovery ed2 = new EndpointDiscovery(null, this.brokenTemplate, null, this.templateUrlSourceData, null);
		String newUrl = (String) method.invoke(ed2, new Class[0]);

		assertEquals(this.brokenTemplate, newUrl);

		// test to see that a url without any escapes is unaffected
		EndpointDiscovery ed3 = new EndpointDiscovery(null, this.instituteServiceDocument, null, this.templateUrlSourceData, null);
		newUrl = (String) method.invoke(ed3, new Class[0]);

		assertEquals(this.instituteServiceDocument, newUrl);

		// check that we are dealing properly with empty braces {}
		EndpointDiscovery ed4 = new EndpointDiscovery(null, this.nullTemplate, null, this.templateUrlSourceData, null);
		newUrl = (String) method.invoke(ed4, new Class[0]);

		assertEquals(this.nullTemplateResolved, newUrl);

		// check that unacceptable keys are dealt with
		EndpointDiscovery ed5 = new EndpointDiscovery(null, this.unacceptableKey, null, this.templateUrlSourceData, null);
		raised = false;
		try
		{
			method.invoke(ed5, new Class[0]);
		}
		catch (InvocationTargetException e)
		{
			assertTrue(e.getTargetException() instanceof TemplateUrlPropertyException);
			raised = true;
		}
		assertTrue(raised);
	}
}
