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
import static org.junit.Assert.*;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.SWORDCollection;

import java.util.List;

// This class assumes certain things about the SSS which makes them not real unit tests

/**
 * This is more of an integration test, and relies on a configured Simple Sword Server (SSS)
 * instance in order to run.  It is a test suite for ensuring that the getEndpoint method
 * of the EndpointDiscovery object connects to a sword server via the SWORDClient and
 * appropriately interprets the results.
 */
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

	/**
	 * Test that getEndpoints() detects when a studyProgramDeposit url is a valid
	 * collection identifier on the server.  It returns exactly one result which
	 * is the collection represented by that identifier.
	 *
	 * @throws Exception
	 */
	@Test
	public void testEndpointWithValidCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		// we expect only one response
		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(1, cols.size());

		// we expect that response to have the same id as the studyProgramDeposit
		SWORDCollection col = cols.get(0);
		assertEquals(this.studyProgramDeposit, col.getHref().toString());
	}

	/**
	 * Test that getEndpoints() detects when a studyProgramDeposit url is
	 * NOT a valid collection identifier on the server.  It returns the
	 * full list of available collections in the sword server
	 *
	 * @throws Exception
	 */
	@Test
	public void testEndpointWithoutValidCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				"http://whatever/", this.templateUrlSourceData, this.swordAuth
		);

		// expect back the full list of sword collections
		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(10, cols.size());
	}

	/**
	 * Test that getEndpoints() detects when a studyProgramDeposit url is
	 * null.  It returns the
	 * full list of available collections in the sword server
	 *
	 * @throws Exception
	 */
	@Test
	public void testEndpointWithoutCollection()
			throws Exception
	{
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, this.instituteServiceDocumentTemplate,
				null, this.templateUrlSourceData, this.swordAuth
		);

		// expect back the full list of sword collections
		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(10, cols.size());
	}

	/**
	 * Test that getEndpoints() throws a NoServiceException when both the
	 * instituteServiceDocument or instituteServiceDocumentTemplate member variables
	 * are not set.
	 *
	 * @throws Exception
	 */
	@Test
	public void testEndpointWithoutService()
			throws Exception
	{
		// construct an object with no service document urls
		EndpointDiscovery ed = new EndpointDiscovery(
				null, null,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		// expect a NoServiceException as there are no service document urls to work from
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

	/**
	 * Test that getEndpoints() generates the correct service document url from
	 * the template when there is no instituteServiceDocument member variable
	 *
	 * @throws Exception
	 */
	@Test
	public void testServiceDocGenerate()
			throws Exception
	{
		// create an object with no service document, but with a template for one
		EndpointDiscovery ed = new EndpointDiscovery(
				null, this.instituteServiceDocumentTemplate,
				this.studyProgramDeposit, this.templateUrlSourceData, this.swordAuth
		);

		// expect the service document to be correctly generated
		List<SWORDCollection> cols = ed.getEndpoints();
		assertEquals(this.instituteServiceDocument, ed.getInstituteServiceDocument());
	}

	/**
	 * Test that getEndpoints() throws a NoServiceException when it cannot communicate
	 * effectively with the sword server (e.g. when the auth credentials are missing
	 * or incorrect)
	 *
	 * @throws Exception
	 */
	@Test
	public void testSwordClientUnauthorised()
			throws Exception
	{
		// create an object without auth credentials
		EndpointDiscovery ed = new EndpointDiscovery(
				this.instituteServiceDocument, null,
				this.studyProgramDeposit, this.templateUrlSourceData, null
		);

		// expect that a NoServiceException is raised as no service document is retrieved
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
