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

import no.uio.duo.bagit.Metadata;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import org.apache.abdera.model.Element;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDError;
import org.swordapp.client.ServerResource;
import org.swordapp.client.Statement;
import org.swordapp.client.StatementParseException;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class RepositoryItem
{
    private SWORDClient client = new SWORDClient();

    private String editUri = null;
    private AuthCredentials auth = null;
    private DepositReceipt receipt = null;
    private Statement statement = null;

    public RepositoryItem(String editUri, AuthCredentials auth)
    {
        this.editUri = editUri;
        this.auth = auth;
    }

    public DepositReceipt getEntryDocument()
            throws SWORDClientException, SWORDError
    {
        try
        {
            if (this.receipt == null)
            {
                DepositReceipt receipt = this.client.getDepositReceipt(this.editUri, this.auth);
                if (receipt.getStatusCode() == 200)
                {
                    this.receipt = receipt;
                }
                else
                {
                    throw new SWORDError(receipt.getStatusCode(), receipt.getEntry().toString());
                }
            }
            return this.receipt;
        }
        catch (ProtocolViolationException e)
        {
            throw new SWORDClientException(e);
        }
    }

    public Statement getStatement()
            throws SWORDClientException, SWORDError, StatementParseException
    {
        try
        {
            if (this.statement == null)
            {
                DepositReceipt receipt = this.getEntryDocument();
                Statement statement = this.client.getStatement(receipt, "application/rdf+xml", this.auth);
                this.statement = statement;
            }
            return this.statement;
        }
        catch (ProtocolViolationException e)
        {
            throw new SWORDClientException(e);
        }
    }

    public List<ServerResource> getFiles()
            throws SWORDClientException, SWORDError, StatementParseException
    {
        Statement statement = this.getStatement();
        List<ServerResource> resources = statement.getParts();
        return resources;
    }

    public Metadata getMetadata()
            throws SWORDClientException, SWORDError
    {
        try
        {
            DepositReceipt receipt = this.getEntryDocument();
            Element metadataElement = receipt.getEntry().getExtension(new QName(Constants.FS_NS, Constants.FS_METADATA));

            // this is unpleasant but also the simplest code way of doing this that I can see
            StringWriter writer = new StringWriter();
            metadataElement.writeTo(writer);

            // bunch of back-flips to make this actually work ...
            Builder parser = new Builder();
            Document doc = parser.build(new ByteArrayInputStream(writer.toString().getBytes()));
            nu.xom.Element root = doc.getRootElement();
            nu.xom.Element clone = (nu.xom.Element) root.copy();
            Metadata metadata = new Metadata(clone);

            return metadata;
        }
        catch (IOException e)
        {
            throw new SWORDClientException(e);
        }
        catch (ParsingException e)
        {
            throw new SWORDClientException(e);
        }
    }
}
