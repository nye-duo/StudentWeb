package no.uio.studentweb.sword;

import no.uio.duo.bagit.BagIt;
import no.uio.duo.bagit.Metadata;
import org.apache.abdera.model.Element;
import org.junit.Test;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.Content;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.ServerResource;
import org.swordapp.client.Statement;
import org.swordapp.client.SwordResponse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DSpaceIntegrationTests
{
    // private String serviceDoc = "http://localhost:8080/swordv2/servicedocument";
    // private String serviceDoc = "https://duo-ds-utv01.uio.no/dspace/swordv2/servicedocument";
    private String serviceDoc = "https://duo-utv.uio.no/swordv2/servicedocument";

    // private AuthCredentials simpleAuth = new AuthCredentials("test", "test");
    private AuthCredentials simpleAuth = new AuthCredentials("richard", "dspace");

    // private String bagitCode = "/home/richard/Code/External/BagItLibrary";
    private String bagitCode = "/Users/richard/Code/External/BagItLibrary";

    // private String depositZip = "/home/richard/Dropbox/Documents/DUO/deposit/deposit.zip";
    private String depositZip = "/Users/richard/Dropbox/Documents/DUO/deposit/deposit.zip";


    @Test
    public void scratch()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        File bagfile = new File(depositZip);

        BagIt bi = new BagIt(bagfile);

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());
    }

    @Test
    public void createBag()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        System.out.print(out.getAbsolutePath());
    }

    @Test
    public void create()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), "application/pdf", 1);
        bi.addFinalFile(new File(secondFinal), "application/pdf" , 2);
        bi.addFinalFile(new File(thirdFinal), "application/pdf", 3);

        bi.addSupportingFile(new File(firstOSecondary), "application/vnd.oasis.opendocument.text", 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), "application/vnd.oasis.opendocument.text", 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), "application/vnd.oasis.opendocument.text", 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), "application/vnd.oasis.opendocument.text", 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), "application/vnd.oasis.opendocument.text", 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), "application/vnd.oasis.opendocument.text", 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();
    }

    @Test
    public void update()
            throws Exception
    {
        // first we have to do a create just as in the previous test
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();


        // now we can go on and do an update

        String location = receipt.getLocation();

        // create a bag, where we've messed around with the contents
        BagIt update = new BagIt(out);

        // reverse the order of the appendices
        // add an additional file
        update.addFinalFile(new File(firstFinal), 1);
        update.addFinalFile(new File(secondFinal), 3);
        update.addFinalFile(new File(thirdFinal), 2);
        update.addFinalFile(new File(thirdCSecondary), 4);

        // switch first open for first closed
        // reverse the order
        update.addSupportingFile(new File(firstCSecondary), 3, "open");
        update.addSupportingFile(new File(secondOSecondary), 2, "open");
        update.addSupportingFile(new File(thirdOSecondary), 1, "open");

        // switch first closed for first open
        // reverse the first two
        // omit the last one
        update.addSupportingFile(new File(firstOSecondary), 2, "closed");
        update.addSupportingFile(new File(secondCSecondary), 1, "closed");

        update.addMetadataFile(new File(metadata));
        update.addLicenceFile(new File(licence));

        update.writeToFile();

        SwordResponse response = depositor.update(location, this.simpleAuth, update);
        System.out.println(response.getLocation());

        out.delete();
    }

    @Test
    public void delete()
            throws Exception
    {
        // first create an item to be deleted

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now delete it

        SwordResponse response = depositor.delete(receipt.getLocation(), this.simpleAuth);
    }

    @Test
    public void setGradeWithPermanentEmbargo()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade

        SwordResponse response = depositor.setGradeWithPermanentEmbargo(receipt.getLocation(), this.simpleAuth, "pass");
    }

    @Test
    public void setGradeWithoutEmbargo()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade, but no embargo

        SwordResponse response = depositor.setGrade(receipt.getLocation(), this.simpleAuth, "pass", null, null);
    }

    @Test
    public void setGradeWithEmbargo()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setGrade(receipt.getLocation(), this.simpleAuth, "A", embargo, "a while");

        // now let's check that we can retrieve the grade
        RepositoryItem ri = new RepositoryItem(receipt.getLocation(), this.simpleAuth);

        DepositReceipt nr = ri.getEntryDocument();
        List<Element> extensions = nr.getEntry().getExtensions();

        Metadata md = ri.getMetadata();

        // check the grade
        List<String> grades = md.getField(Metadata.GRADE);
        assert grades.size() == 1;
        assert grades.get(0).equals("A");

        // check the sent date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sentDate = sdf.format(embargo);
        List<String> embargoDate = md.getField(Metadata.EMBARGO_END_DATE);
        assert embargoDate.size() == 1;
        assert embargoDate.get(0).equals(sentDate);

        // check the embargo terms
        List<String> terms = md.getField(Metadata.EMBARGO_TYPE);
        assert terms.size() == 1;
        assert terms.get(0).equals("a while");

        /*
        List<ServerResource> files = ri.getFiles();
        for (ServerResource sr : files)
        {
            Content content = ri.getFile(sr.getUri().toString());
        }
        */
    }

    @Test
    public void setEmbargo()
            throws Exception
    {
        // first create an item forfeit the appeal for

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setEmbargo(receipt.getLocation(),  this.simpleAuth, embargo, "embargo");
    }

    @Test
    public void forfeitGradeAppeal()
            throws Exception
    {
        // first create an item forfeit the appeal for

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setGrade(receipt.getLocation(), this.simpleAuth, "fail", embargo, "a while");

        // now forfeit the appeal process

        // which means what, exactly ...
    }


    @Test
    public void getMetadata()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        // String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        Metadata metadata = new Metadata();

        metadata.addField(Metadata.NAME, "Thor Heyerdahl");
        metadata.addField(Metadata.FAMILY_NAME, "Heyerdahl");
        metadata.addField(Metadata.GIVEN_NAME, "Thor");
        metadata.addField(Metadata.STUDENT_NUMBER, "123456789");
        metadata.addField(Metadata.UID, "theyerdahl");
        metadata.addField(Metadata.FOEDSELSNUMMER, "987654321");
        metadata.addField(Metadata.POSTAL_ADDRESS, "Colla Micheri, Italy");
        metadata.addField(Metadata.EMAIL, "t.heyerdahl@kontiki.com");
        metadata.addField(Metadata.TELEPHONE_NUMBER, "0047 123456");
        metadata.addSubject("AST3220", "Kosmologi I");
        metadata.addField(Metadata.UNITCODE, "123");
        metadata.addField(Metadata.UNIT_NAME, "Arkeologi, konservering og historie");
        metadata.addField(Metadata.TITLE, "101 days around some of the world");
        metadata.addField(Metadata.TITLE, "101 days in the Pacific", "nob");
        metadata.addField(Metadata.LANGUAGE, "nob");
        metadata.addField(Metadata.ABSTRACT, "Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsaflte ved navn\n" +
                "        Kon-Tiki. (with special characters: 有期 and guest starring å and ø)", "nob");
        metadata.addField(Metadata.ABSTRACT, "In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                "        they called the Kon-Tiki.");
        metadata.addField(Metadata.TYPE, "Master's thesis");
        metadata.addField(Metadata.EMBARGO_TYPE, "5 years");
        metadata.addField(Metadata.EMBARGO_END_DATE, "01-01-2015");
        metadata.addField(Metadata.GRADE, "pass");


        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadata(metadata);
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now let's go on and test the RepositoryItem features

        RepositoryItem item = new RepositoryItem(receipt.getLocation(), this.simpleAuth);
        DepositReceipt entry = item.getEntryDocument();
        Metadata md = item.getMetadata();

        System.out.println(md.toXML());
    }

    @Test
    public void getFiles()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        // String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        Metadata metadata = new Metadata();

        metadata.addField(Metadata.NAME, "Thor Heyerdahl");
        metadata.addField(Metadata.FAMILY_NAME, "Heyerdahl");
        metadata.addField(Metadata.GIVEN_NAME, "Thor");
        metadata.addField(Metadata.STUDENT_NUMBER, "123456789");
        metadata.addField(Metadata.UID, "theyerdahl");
        metadata.addField(Metadata.FOEDSELSNUMMER, "987654321");
        metadata.addField(Metadata.POSTAL_ADDRESS, "Colla Micheri, Italy");
        metadata.addField(Metadata.EMAIL, "t.heyerdahl@kontiki.com");
        metadata.addField(Metadata.TELEPHONE_NUMBER, "0047 123456");
        metadata.addSubject("AST3220", "Kosmologi I");
        metadata.addField(Metadata.UNITCODE, "123");
        metadata.addField(Metadata.UNIT_NAME, "Arkeologi, konservering og historie");
        metadata.addField(Metadata.TITLE, "101 days around some of the world");
        metadata.addField(Metadata.TITLE, "101 days in the Pacific", "nob");
        metadata.addField(Metadata.LANGUAGE, "nob");
        metadata.addField(Metadata.ABSTRACT, "Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn\n" +
                "        Kon-Tiki.", "nob");
        metadata.addField(Metadata.ABSTRACT, "In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                "        they called the Kon-Tiki.");
        metadata.addField(Metadata.TYPE, "Master's thesis");
        metadata.addField(Metadata.EMBARGO_TYPE, "5 years");
        metadata.addField(Metadata.EMBARGO_END_DATE, "01-01-2015");
        metadata.addField(Metadata.GRADE, "pass");


        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadata(metadata);
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now let's go on and test the RepositoryItem features

        RepositoryItem item = new RepositoryItem(receipt.getLocation(), this.simpleAuth);
        Statement statement = item.getStatement();
        List<ServerResource> files = item.getFiles();

        for (ServerResource file : files)
        {
            System.out.println(file.getUri());
        }
    }

    @Test
    public void getFile()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        // String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        Metadata metadata = new Metadata();

        metadata.addField(Metadata.NAME, "Thor Heyerdahl");
        metadata.addField(Metadata.FAMILY_NAME, "Heyerdahl");
        metadata.addField(Metadata.GIVEN_NAME, "Thor");
        metadata.addField(Metadata.STUDENT_NUMBER, "123456789");
        metadata.addField(Metadata.UID, "theyerdahl");
        metadata.addField(Metadata.FOEDSELSNUMMER, "987654321");
        metadata.addField(Metadata.POSTAL_ADDRESS, "Colla Micheri, Italy");
        metadata.addField(Metadata.EMAIL, "t.heyerdahl@kontiki.com");
        metadata.addField(Metadata.TELEPHONE_NUMBER, "0047 123456");
        metadata.addSubject("AST3220", "Kosmologi I");
        metadata.addField(Metadata.UNITCODE, "123");
        metadata.addField(Metadata.UNIT_NAME, "Arkeologi, konservering og historie");
        metadata.addField(Metadata.TITLE, "101 days around some of the world");
        metadata.addField(Metadata.TITLE, "101 days in the Pacific", "nob");
        metadata.addField(Metadata.LANGUAGE, "nob");
        metadata.addField(Metadata.ABSTRACT, "Thor Heyerdahl og fem andre dro fra Peru til Raroia i en selvkonstruert balsafl�te ved navn\n" +
                "        Kon-Tiki.", "nob");
        metadata.addField(Metadata.ABSTRACT, "In the Kon-Tiki Expedition, Heyerdahl and five fellow adventurers went to Peru, where\n" +
                "        they constructed a pae-pae raft from balsa wood and other native materials, a raft that\n" +
                "        they called the Kon-Tiki.");
        metadata.addField(Metadata.TYPE, "Master's thesis");
        metadata.addField(Metadata.EMBARGO_TYPE, "5 years");
        metadata.addField(Metadata.EMBARGO_END_DATE, "01-01-2015");
        metadata.addField(Metadata.GRADE, "pass");


        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadata(metadata);
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now let's go on and test the RepositoryItem features

        RepositoryItem item = new RepositoryItem(receipt.getLocation(), this.simpleAuth);
        List<ServerResource> files = item.getFiles();

        for (ServerResource file : files)
        {
            System.out.println(file.getUri());
            Content content = item.getFile(file.getUri().toString());
            assert content.getInputStream() != null;
        }
    }

    @Test
    public void setGradeAgain()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery(this.serviceDoc, null, null, null, this.simpleAuth);
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = bagitCode + "/src/test/resources/testbags/testfiles/";

        String firstFinal = fileBase + "MainArticle.pdf";
        String secondFinal = fileBase + "AppendixA.pdf";
        String thirdFinal = fileBase + "AppendixB.pdf";
        String firstOSecondary = fileBase + "MainArticle.odt";
        String secondOSecondary = fileBase + "AppendixA.odt";
        String thirdOSecondary = fileBase + "AppendixB.odt";
        String firstCSecondary = fileBase + "UserData1.odt";
        String secondCSecondary = fileBase + "UserData2.odt";
        String thirdCSecondary = fileBase + "UserData3.odt";
        String metadata = fileBase + "metadata.xml";
        String licence = fileBase + "licence.txt";

        File out = new File(System.getProperty("user.dir") + File.separator + "deposit.zip");

        BagIt bi = new BagIt(out);

        bi.addFinalFile(new File(firstFinal), 1);
        bi.addFinalFile(new File(secondFinal), 2);
        bi.addFinalFile(new File(thirdFinal), 3);

        bi.addSupportingFile(new File(firstOSecondary), 1, "open");
        bi.addSupportingFile(new File(secondOSecondary), 2, "open");
        bi.addSupportingFile(new File(thirdOSecondary), 3, "open");

        bi.addSupportingFile(new File(firstCSecondary), 1, "closed");
        bi.addSupportingFile(new File(secondCSecondary), 2, "closed");
        bi.addSupportingFile(new File(thirdCSecondary), 3, "closed");

        bi.addMetadataFile(new File(metadata));
        bi.addLicenceFile(new File(licence));

        bi.writeToFile();

        Depositor depositor = new Depositor();
        DepositReceipt receipt = depositor.create(col.getHref().toString(), this.simpleAuth, bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setGrade(receipt.getLocation(), this.simpleAuth, "pass", embargo, "a while");

        // now we try setting the grade again (which ought to work)
        embargo = new Date((new Date()).getTime() + 1000000000L);
        response = depositor.setGrade(receipt.getLocation(), this.simpleAuth, "fail", embargo, "a bit");
    }
}
