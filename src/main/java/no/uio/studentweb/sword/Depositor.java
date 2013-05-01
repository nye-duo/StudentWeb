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
import org.swordapp.client.Deposit;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.EntryPart;
import org.swordapp.client.ProtocolViolationException;
import org.swordapp.client.SWORDClient;
import org.swordapp.client.SWORDClientException;
import org.swordapp.client.SWORDError;
import org.swordapp.client.SwordResponse;

import no.uio.duo.bagit.BagIt;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to handle the deposit operations required between StudentWeb and the
 * repository using SWORDv2.  This is effectively a thin wrapper around the
 * SWORDv2 client library
 */
public class Depositor
{
    /**
     * Create a new item in the repository with the supplied BagIt object
     *
     * @param colUri    The SWORDv2 collection URI to deposit to
     * @param auth      The SWORDv2 authentication credentials
     * @param bagIt     The BagIt object to deposit
     * @return  A sword deposit receipt
     * @throws SWORDClientException
     * @throws SWORDError
     * @throws IOException
     */
	public DepositReceipt create(String colUri, AuthCredentials auth, BagIt bagIt)
			throws SWORDClientException, SWORDError, IOException
	{
		SWORDClient client = new SWORDClient();

		Deposit deposit = new Deposit();
		deposit.setInProgress(true);

		deposit.setFile(bagIt.getFile());
		deposit.setFilename(bagIt.getName());
		deposit.setMd5(bagIt.getMD5());
		deposit.setMimeType(bagIt.getMimetype());
		deposit.setPackaging(bagIt.getPackaging());

		try
		{
			DepositReceipt receipt = client.deposit(colUri, deposit, auth);
			return receipt;
		}
		catch (ProtocolViolationException e)
		{
			throw new SWORDClientException(e);
		}
	}

    /**
     * Update an item which has previously been deposited in the repository
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @param bagIt The BagIt object to deposit
     * @return  A basit SwordResponse object
     * @throws SWORDClientException
     * @throws SWORDError
     * @throws IOException
     */
	public SwordResponse update(String editUri, AuthCredentials auth, BagIt bagIt)
			throws SWORDClientException, SWORDError, IOException
	{
		try
		{
			SWORDClient client = new SWORDClient();
			DepositReceipt receipt = client.getDepositReceipt(editUri, auth);

			Deposit deposit = new Deposit();
			deposit.setMetadataRelevant(true);

			deposit.setFile(bagIt.getFile());
			deposit.setFilename(bagIt.getName());
			deposit.setMd5(bagIt.getMD5());
			deposit.setMimeType(bagIt.getMimetype());
			deposit.setPackaging(bagIt.getPackaging());
			
			SwordResponse resp = client.replaceMedia(receipt, deposit, auth);
			return resp;
		}
		catch (ProtocolViolationException e)
		{
			throw new SWORDClientException(e);
		}
	}

    /**
     * Delete an existing item from the repository
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @return  A basic SwordResponse object
     * @throws SWORDClientException
     * @throws SWORDError
     */
	public SwordResponse delete(String editUri, AuthCredentials auth)
			throws SWORDClientException, SWORDError
	{
		try
		{
			SWORDClient client = new SWORDClient();
			SwordResponse resp = client.delete(editUri, auth);
			return resp;
		}
		catch (ProtocolViolationException e)
		{
			throw new SWORDClientException(e);
		}
	}

    /**
     * Set an item's grade and permanently embargo it.  This is equivalent to calling {@link #setGrade} with a
     * an embargo of {@link Constants.EMBARGO_PERMANENT}
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth      The SWORDv2 authentication credentials
     * @param grade     The grade to set, should be one of {@link Constants.PASS} or {@link Constants.FAIL}
     * @return  A DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
    public SwordResponse setGradeWithPermanentEmbargo(String editUri, AuthCredentials auth, String grade)
            throws SWORDClientException, SWORDError
    {
        // give us a date a couple of hundred years in the future
        Date embargoUntil = this.getFarFuture();
        return this.setGrade(editUri, auth, grade, embargoUntil, Constants.EMBARGO_PERMANENT);
    }

    /**
     * Set an item's grade and embargo period
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @param grade The grade to set, should be one of {@link Constants.PASS} or {@link Constants.FAIL}
     * @param embargoUntil  The Date to embargo until
     * @param embargoType   The Embargo type, should be one of the embargo types in {@link Constants}
     * @return  A DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
	public SwordResponse setGrade(String editUri, AuthCredentials auth, String grade, Date embargoUntil, String embargoType)
			throws SWORDClientException, SWORDError
	{
		try
		{
			SWORDClient client = new SWORDClient();

			EntryPart ep = new EntryPart();
			ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_GRADE), grade);

            this.addEmbargoDetails(ep, embargoUntil, embargoType);

			Deposit deposit = new Deposit();
			deposit.setEntryPart(ep);
			deposit.setInProgress(false);

			DepositReceipt receipt = client.addToContainer(editUri, deposit, auth);
			return receipt;
		}
		catch (ProtocolViolationException e)
		{
			throw new SWORDClientException(e);
		}
	}

    /**
     * Forfeit the grade appeal for the student.  This is equivalent to calling {@link #setGrade} with the same
     * arguments, and exists purely for syntactic sugar
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @param grade The grade to set, should be one of {@link Constants.PASS} or {@link Constants.FAIL}
     * @param embargoUntil  The Date to embargo until
     * @param embargoType   The Embargo type, should be one of the embargo types in {@link Constants}
     * @return  return DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
    public SwordResponse forfeitGradeAppeal(String editUri, AuthCredentials auth, String grade, Date embargoUntil, String embargoType)
            throws SWORDClientException, SWORDError
    {
        return this.setGrade(editUri, auth, grade, embargoUntil, embargoType);
    }

    /**
     * Forfeit the grade appeal for the student.  This is equivalent to calling {@link #setEmbargo} with the same
     * arguments, and exists purely for syntactic sugar
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @param embargoUntil  The Date to embargo until
     * @param embargoType   The Embargo type, should be one of the embargo types in {@link Constants}
     * @return  return DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
	public SwordResponse forfeitGradeAppeal(String editUri, AuthCredentials auth, Date embargoUntil, String embargoType)
			throws SWORDClientException, SWORDError
	{
		return this.setEmbargo(editUri, auth, embargoUntil, embargoType);
	}

    /**
     * Forfeit the grade appeal for the student.  This is equivalent to calling {@link #setGradeWithPermanentEmbargo} with the same
     * arguments, and exists purely for syntactic sugar
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @param grade The grade to set, should be one of {@link Constants.PASS} or {@link Constants.FAIL}
     * @return  return DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
    public SwordResponse forfeitGradeAppealWithPermanentEmbargo(String editUri, AuthCredentials auth, String grade)
            throws SWORDClientException, SWORDError
    {
        return this.setGradeWithPermanentEmbargo(editUri, auth, grade);
    }

    /**
     * Forfeit the grade appeal for the student.  This is equivalent to calling {@link #setEmbargo} with these same
     * arguments and {@link Constants.EMBARGO_PERMANENT}, and thus exists purely for syntactic sugar
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     * @return return DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
    public SwordResponse forfeitGradeAppealWithPermanentEmbargo(String editUri, AuthCredentials auth)
            throws SWORDClientException, SWORDError
    {
        // give us a date a couple of hundred years in the future
        Date embargoUntil = this.getFarFuture();
        return this.setEmbargo(editUri, auth, embargoUntil, Constants.EMBARGO_PERMANENT);
    }

    /**
     * Set an embargo on the item
     *
     * @param editUri   The SWORDv2 Edit-URI of the item, as obtained from the DepositReceipt after the original deposit
     * @param auth  The SWORDv2 authentication credentials
     *  @param embargoUntil  The Date to embargo until
     * @param embargoType   The Embargo type, should be one of the embargo types in {@link Constants}
     * @return  return DepositReceipt object
     * @throws SWORDClientException
     * @throws SWORDError
     */
    public SwordResponse setEmbargo(String editUri, AuthCredentials auth, Date embargoUntil, String embargoType)
    			throws SWORDClientException, SWORDError
    {
        try
        {
            SWORDClient client = new SWORDClient();
            EntryPart ep = new EntryPart();
            Deposit deposit = new Deposit();

            this.addEmbargoDetails(ep, embargoUntil, embargoType);

            deposit.setEntryPart(ep);
            deposit.setInProgress(false);

            DepositReceipt receipt = client.addToContainer(editUri, deposit, auth);
            return receipt;
        }
        catch (ProtocolViolationException e)
        {
            throw new SWORDClientException(e);
        }
    }

    private void addEmbargoDetails(EntryPart ep, Date embargoUntil, String embargoType)
    {
        if (embargoUntil != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String eed = sdf.format(embargoUntil);
            ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_EMBARGO_END_DATE), eed);
        }

        if (embargoType != null)
        {
            ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_EMBARGO_TYPE), embargoType);
        }
    }

    private Date getFarFuture()
    {
        Date future = new Date((new Date()).getTime() + 9000000000000L);
        return future;
    }
}
