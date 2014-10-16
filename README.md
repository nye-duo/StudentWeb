StudentWeb Sword Integration Tools
==================================

Introduction
------------

This library contans a variety of tools to aid the integration between StudentWeb
and DSpace (DUO) via SWORD.


Build/Install
-------------

###SWORDv2 Client

This library depends on the sword2-client 0.9 Java Library which is not currently available
in the Maven central repo.  It can be downloaded from:

	https://github.com/swordapp/JavaClient2.0

Once downloaded it should be installed into the local maven repository with

	mvn install

###BagItLibrary

This library depends on the 2.0 version of the BagItLibrary which implements the custom packaging and unpackaging
requirements for StudentWeb and Duo.  It can be downloaded from [https://github.com/nye-duo/BagItLibrary](https://github.com/nye-duo/BagItLibrary)

You should follow the Build/Install instructions provided in its README in order to satisfy its dependencies,
and then you can install it to the local maven repository with

	mvn install

###StudentWeb Integration Tools

Once these non-maven-central-repo dependencies have been installed, then it's possible to compile this library with:

    mvn clean package


Endpoint Discovery
------------------

###Introduction

The Endpoint Discovery features implement the business logic as laid out in the functional
overview: [https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit](https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit#heading=h.cmw4rs85d267)

It also contains features to carry out the following specific tasks:

1. Interpret url templates for service document urls

2. Make requests to a sword server to retrieve the service document, and validate the supplied
	deposit url against it.


###Url Templates

If the Service Document url is not available in full, it may instead be provided as a
URL template which encloses keywords to be replaced in curly braces, thus:

	{unit-code}

During discovery these urls will be parsed into the correct form for use in retrieving
the service document from the sword server.  For example:

	http://duo.oui.no/swordv2/service/{unit-code}

might become:

	http://duo.oui.no/swordv2/service/12345

The list of keywords which can be replaced is dependent on the implementation of the
TemplateUrlSourceData interface, which must be provided by the module user (i.e. you).


###Sword Collections

When a discovery operation is run, the module may respond with one of two things:

1.	A list with only one SWORDCollection object, which represents the supplied deposit
	url.  This happens if the deposit url is successfully validated against the
	retrieved service document.

2.	A list with an arbitrary number (zero or more) SWORDCollection objects which represent
	the available collections for deposit.  This happens if the supplied deposit url
	is either null or unsuccessful in validating against the retrieved service document

Either way, the caller will receive a List<SWORDCollection> object which can then be
used to select the deposit endpoint.  If the list is of size 1, then the deposit should
go ahead without bothering the user about selection, and if it is greater than size 1,
then the user should be offered the choice of deposit.

The list of collections returned will be obtained from the sword server using the
AuthCredentials object supplied to the EndpointDiscovery class, and so all returned
collections are safe for deposit by the authenticated user.

###Example Usage

	EndpointDiscovery ed = new EndpointDiscovery(
				"http://duo.uio.no/swordv2/service", // service document url
				"http://duo.uio.no/swordv2/service/{unit-code}",	// or service document template
				"http://duo.uio.no/swordv2/collection/1",	// desired deposit endpoint
				new SWTemplateUrlSourceData(), 		// implementation of the TemplateUrlSourceData interface
				new AuthCredentials("sword", "sword")	// sword server credentials
		);

	// retrieve the list of possible collections
	List<SWORDCollection> cols = ed.getEndpoints();

	if (cols.size() == 0)
	{
		// no deposit collections available - you probably
		// want to give the client an error
	}
	else if (cols.size() == 1)
	{
		// only one available collection, so go ahead and do the
		// deposit
	}
	else
	{
		// more than one collection available, so give the user
		// a selection
	}

###Known Limitations

SWORDv2 permits service documents to be nested.  The EndpointDiscovery library only looks
at the top level service documents, and does not attempt to validate supplied
deposit urls against sub-service documents.  Therefore, if possible configure your
sword server to give all the collections in one service document and do not
use nested service documents.


Deposit
-------

###Introduction

Deposit to DSpace is handled via the no.uio.studentweb.sword.Depositor class.  This has all the essential
methods required for StudentWeb to interact with the repository:

	create
	update
	delete
	setGrade
	setGradeWithPermanentEmbargo
	forfeitGradeAppeal
	forfeitGradeAppealWithPermanentEmbargo
	setEmbargo

These meet the deposit protocol operation requirements as detailed in the funtional overview: [https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit](https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit#heading=h.i3etn5tyajkz)


###create

This takes a BagIt object as constructed by the related [BagItLibrary](https://github.com/nye-duo/BagItLibrary) and sends it to the
specified deposit endpoint using the AuthCredentials provided.

	public DepositReceipt create(String colUri, AuthCredentials auth, BagIt bagIt)

It sets the parameters of the SWORDv2 deposit as:

	In-Progress: true
	Content-Disposition; Filename: {name of the BagIt file}
	Content-Type: application/zip
	Packaging: http://duo.uio.no/terms/package/FSBagIt

When the deposit has completed, it will return the DepositReceipt from the underlying sword2-client
library, or it will throw an appropriate exception

The result will be a new item in the DSpace WorkSpace with the metadata and files as specified
in the BagIt.


###update

This takes a BagIt object as constructed by the related [BagItLibrary](https://github.com/nye-duo/BagItLibrary) and uses it to replace
any existing content and metadata in the repository identified by the specified Edit-URI, using
the AuthCredentials provided

	public SwordResponse update(String editUri, AuthCredentials auth, BagIt bagIt)

It sets the parameters of the SWORDv2 deposit as:

	Metadata-Relevant: true
	Content-Disposition; Filename: {name of the BagIt file}
	Content-Type: application/zip
	Packaging: http://duo.uio.no/terms/package/FSBagIt

When the deposit has completed, it will return the SwordResponse object from the underlying sword2-client
library, or it will throw an appropriate exception

The result will be that all the content and metadata in the existing item in DSpace will have been replaced with
the new content and metadata.


###delete

This deletes the item from the repository at the specified Edit-URI with the AuthCredentials provided

	public SwordResponse delete(String editUri, AuthCredentials auth)

This issues an HTTP DELETE on the Edit-URI.

When the delete operation has completed, it will return the SwordResponse object from the underlying sword2-client
library, or it will throw an appropriate exception.

The result will be that the item will be completely removed from DSpace in its entirity.


###setGrade

This adds the supplied grade and the associated embargo details to the item in the repository identified
with the Edit-URI, using the AuthCredentials provided.

	public SwordResponse setGrade(String editUri, AuthCredentials auth, String grade, Date embargoUntil, String embargoType)

The grade is a free string, but should be provided using one of the constants:

	Constants.PASS
	Constants.FAIL

The embargoType is a free string, but should be provided using one of the constants:

	Constants.EMBARGO_PERMANENT
    Constants.EMBARGO_OPEN
    Constants.EMBARGO_3_YEARS
    Constants.EMBARGO_CLOSED

The SWORDv2 request that this creates includes an atom:entry document with the grade and embargo details
encoded as per the metadata schema documented in the functional overview: [https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit](https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit#heading=h.xgvss9entr7t)

The parameters of the request are:

	In-Progress: false

When the deposit has completed, it will return the DepositReceipt from the underlying sword2-client
library, or it will throw an appropriate exception

The result will be that the new metadata will have been added to the item in DSpace, and the item will have
been moved from the WorkSpace and into the review Workflow (if appropriate).  From this point on, no further updates to the
item's content will be accepted (the operations "update" and "delete" will fail from this point on); updates
to metadata will still be accepted ("setGrade", "setGradeWithPermanentEmbargo", "forfeitGradeAppeal", "setEmbargo"
will all continue to work).


###setGradeWithPermanentEmbargo

This adds the supplied grade to the item in the repository identified with the Edit-URI, using the AuthCredentials
provided.  It simultaneously permanently embargoes the item.

	public SwordResponse setGradeWithPermanentEmbargo(String editUri, AuthCredentials auth, String grade)

The grade is a free string, but should be provided using one of the constants:

	Constants.PASS
	Constants.FAIL

This behaves identically to "setGrade", and simply defaults the embargo to a time 9000000000000 milliseconds
in the future (a number of hundred years), and sets the embargo type to Constants.EMBARGO_PERMANENT.


###forfeitGradeAppeal

There are two method signatures for forfeiting the grade appeal; one is with a grade set, and the other is without.

	public SwordResponse forfeitGradeAppeal(String editUri, AuthCredentials auth, String grade, Date embargoUntil, String embargoType)
	public SwordResponse forfeitGradeAppeal(String editUri, AuthCredentials auth, Date embargoUntil, String embargoType)

This sets the embargo date of the item appropriately, and optionally sets a new grade for the item (as identified
by the Edit-URI, using the AuthCredentials provided).

If a grade is supplied, this operation is identical to "setGrade".

If no grade is supplied, this operation is identical to "setEmbargo".


###forfeitGradeAppealWithPermanentEmbargo

There are two method signatures for forfeiting the grade appeal with a permanent embargo period; one is with a grade set, and the other is without.

	public SwordResponse forfeitGradeAppealWithPermanentEmbargo(String editUri, AuthCredentials auth, String grade)
	public SwordResponse forfeitGradeAppealWithPermanentEmbargo(String editUri, AuthCredentials auth)

This sets the embargo date far in the future, and optionally setsa new grade for the item (as identified
by the Edit-URI, using the AuthCredentials provided).

If the grade is supplied, this operation is identical to "setGradeWithPermanentEmbargo".

If no grade is supplied, this operation is equivalent to "setEmbargo" with an embargo date far in the future and
and embargo type of Constants.EMBARGO_PERMANENT.


###setEmbargo

This sets the embargo metadata terms in DSpace to the supplied embargo date and embargo type on the item identified
by the Edit-URI using the AuthCredentials provided.

	public SwordResponse setEmbargo(String editUri, AuthCredentials auth, Date embargoUntil, String embargoType)

The embargoType is a free string, but should be provided using one of the constants:

	Constants.EMBARGO_PERMANENT
    Constants.EMBARGO_OPEN
    Constants.EMBARGO_3_YEARS
    Constants.EMBARGO_CLOSED

The SWORDv2 request that this creates includes an atom:entry document with the embargo details
encoded as per the metadata schema documented in the functional overview: [https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit](https://docs.google.com/a/cottagelabs.com/document/d/17Iiswcz_LkSMgdhEZIesV1BTrijRNsPWAyH29-L1rQg/edit#heading=h.xgvss9entr7t)

The parameters of the request are:

	In-Progress: false

When the deposit has completed, it will return the DepositReceipt from the underlying sword2-client
library, or it will throw an appropriate exception

The result will be that the new metadata will have been added to the item in DSpace, and the item will then
be embargoed by DSpace up until the date supplied as the end of the embargo.


Retrieval
---------

###Introduction

Retrieving items and information about items from DSpace is done via the no.uio.studentweb.sword.RepositoryItem class.

There are two kinds of information that we want to be able to retrieve from the repository:

1. The metadata associated with the item.  This is associated with the Entry Document or Deposit Receipt (in SWORDv2
	terms), where the metadata is embedded in an atom:entry XML document.

2. The list of files associated with the item.  This is associated with the Statement (in SWORDv2 terms), where the
	files associated with an item are listed as entries or aggregated resources of the item iteself.

These are serviced by methods on the RepositoryItem thus:

1. getEntryDocument gets the atom:entry XML document which represents the item in the repository.  Meanwhile,
	getMetadata will parse the metadata out of the atom:entry document into a [BagItLibrary](https://github.com/nye-duo/BagItLibrary) Metadata object such as
	is used when creating the initial deposit.

2. getStatement gets the sword2-client Statement object which represents the item in the repository.  Meanwhile
	getFiles will return a list of sword2-client ServerResource objects which represent the files in the repository
	item.

###RepositoryItem

To construct a new RepositoryItem object, provide it with the identifier for the item (Edit-URI) and the credentials
which should be used to access the item:

	public RepositoryItem(String editUri, AuthCredentials auth)


###Entry Document/Metadata

To retrieve the entry document, construct a repository item, and then simply request the Entry Document

	RepositoryItem item = new RepositoryItem(editUri, authCredentials);
	DepositReceipt entry = item.getEntryDocument();

The object returned is the DepositReceipt object from the underlying sword2-client library.  This gives you access
to all of the item's top-level SWORDv2 properties (such as its associated urls), and also gives you access to its
native Dublin Core metadata, and any embedded foreign markup.

The StudentWeb metadata is embedded within this Deposit Receipt in an element called fs:metadata, so the Deposit
Receipt has the structure:

	<atom:entry>
		<fs:metadata>
			<!-- original StudentWeb metadata -->
		<fs:metadata>
	</atom:entry>

The most convenient way to access this metadata is actually to request it directly from the RepositoryItem thus:

	RepositoryItem item = new RepositoryItem(editUri, authCredentials);
	Metadata metadata = item.getMetadata()

This provides you with a [BagItLibrary](https://github.com/nye-duo/BagItLibrary) Metadata object from which you can access the individual elements of the
metadata schema, such as:

	List<String> title = metadata.getField(Metadata.TITLE);

See the [BagItLibrary](https://github.com/nye-duo/BagItLibrary) documentation for more details.


###Statement/Files

To retrieve the statement, construct a repository item, and then simply request it:

	RepositoryItem item = new RepositoryItem(editUri, authCredentials);
	Statement statement = item.getStatement();

The object returned is the Statement from the underlying sword2-client library (and, in particular, the OREStatement
implementation of the Statement interface, although this should not be relevant).  This gives you access to all of
the item's files, which you can obtain with:

	List<ServerResource> files = statement.getParts();

There is a shortcut for this provided on the RepositoryItem, so that you can get the files in one operation:

	RepositoryItem item = new RepositoryItem(editUri, authCredentials);
	List<ServerResource> files = item.getFiles();

The ServerResource object can then be used to obtain the file's URI using ServerResource.getUri(), and other
associated information about the file which is relevant to SWORDv2, such as the date it was deposited.

