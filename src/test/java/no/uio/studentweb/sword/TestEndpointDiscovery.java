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

import org.junit.*;
import org.swordapp.client.AuthCredentials;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Test suite to unit test as much of the EndpointDiscovery object as possible.
 *
 * Note that this omits testing of the getEndpoint() method itself, as that connects to
 * a sword server.  See SSSSemiUnits for partial unit/integration tests for this method
 */
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

	/**
	 * Test the constructors of the EndpointDiscovery object, to ensure that all
	 * member variables are being set appropriately
	 */
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

	/**
	 * Test the service document url generation method for a simple url
	 *
	 * @throws Exception
	 */
	@Test
	public void testUrlGeneration()
			throws Exception
	{
		// set the access to this method for the duration of the test (it is private otherwise)
		Method method = EndpointDiscovery.class.getDeclaredMethod("generateServiceDocumentUrl", new Class[0]);
		method.setAccessible(true);

		// construct an endpoint which only has enough info to test the service document url generation
		EndpointDiscovery ed = new EndpointDiscovery(null, this.instituteServiceDocumentTemplate, null, this.templateUrlSourceData, null);
		String newUrl = (String) method.invoke(ed, new Class[0]);

		// expect the generated url to be the instituteServiceDocument
		assertEquals(this.instituteServiceDocument, newUrl);

		// now try a more difficult one (contains two replaces)
		EndpointDiscovery ed1 = new EndpointDiscovery(null, this.difficultTemplate, null, this.templateUrlSourceData, null);
		newUrl = (String) method.invoke(ed1, new Class[0]);

		// expect the generated url to be what we said it would be
		assertEquals(this.difficultTemplateResolved, newUrl);
	}

	/**
	 * Test the service documenturl generation method with a variety of broken templates,
	 * and expect it to throw appropriate exceptions
	 *
	 * @throws Exception
	 */
	@Test
	public void testBreakUrlGeneration()
			throws Exception
	{
		// set the access to this method for the duration of the test (it is private otherwise)
		Method method = EndpointDiscovery.class.getDeclaredMethod("generateServiceDocumentUrl", new Class[0]);
		method.setAccessible(true);

		// test to see if an incorrectly constructed item generates configuration exceptions
		// there is no configuration of the object, so any attempt to access member variables
		// is expected to throw a DiscoveryConfigurationException
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
		// there is a TemplateUrlSourceData implementation, but no service document template
		// to work on, so we expect to see a DiscoveryConfigurationException
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
		// since braces are not allowed in the URL, an unbalanced set of braces should
		// result in a TemplateUrlPropertyException
		EndpointDiscovery ed2 = new EndpointDiscovery(null, this.brokenTemplate, null, this.templateUrlSourceData, null);
		raised = false;
		try
		{
			method.invoke(ed2, new Class[0]);
		}
		catch (InvocationTargetException e)
		{
			assertTrue(e.getTargetException() instanceof TemplateUrlPropertyException);
			raised = true;
		}
		assertTrue(raised);

		// test to see that a url without any escapes is unaffected
		EndpointDiscovery ed3 = new EndpointDiscovery(null, this.instituteServiceDocument, null, this.templateUrlSourceData, null);
		String newUrl = (String) method.invoke(ed3, new Class[0]);
		assertEquals(this.instituteServiceDocument, newUrl);

		// check that we are dealing properly with empty braces {}
		// braces are not allowed in the final url, so they will be stripped, but since there is no key
		// in there, they will not be replaced by anything
		EndpointDiscovery ed4 = new EndpointDiscovery(null, this.nullTemplate, null, this.templateUrlSourceData, null);
		newUrl = (String) method.invoke(ed4, new Class[0]);
		assertEquals(this.nullTemplateResolved, newUrl);

		// check that unacceptable keys are dealt with
		// the mock object will throw an error for any key not "unit-code" or "other", so we
		// expect to see a TemplateUrlPropertyException
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
