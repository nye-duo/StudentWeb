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

public class Depositor
{
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

	public SwordResponse setGrade(String editUri, AuthCredentials auth, String grade)
			throws SWORDClientException, SWORDError
	{
		try
		{
			SWORDClient client = new SWORDClient();

			EntryPart ep = new EntryPart();
			ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_GRADE), grade);

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

	public SwordResponse forfeitGradeAppeal(String editUri, AuthCredentials auth, Date embargoUntil, String embargoType)
			throws SWORDClientException, SWORDError
	{
		try
		{
			SWORDClient client = new SWORDClient();
			EntryPart ep = new EntryPart();
			Deposit deposit = new Deposit();

			if (embargoUntil != null)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				String eed = sdf.format(embargoUntil);
				ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_EMBARGO_END_DATE), eed);
			}

			if (embargoType != null)
			{
				ep.addSimpleExtension(new QName(Constants.FS_NS, Constants.FS_EMBARGO_TYPE), embargoType);
			}

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
}
