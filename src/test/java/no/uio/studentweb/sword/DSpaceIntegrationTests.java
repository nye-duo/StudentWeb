package no.uio.studentweb.sword;

import no.uio.duo.bagit.BagIt;
import org.junit.Test;
import org.swordapp.client.AuthCredentials;
import org.swordapp.client.DepositReceipt;
import org.swordapp.client.SWORDCollection;
import org.swordapp.client.SwordResponse;

import java.io.File;
import java.util.Date;
import java.util.List;

public class DSpaceIntegrationTests
{
    @Test
    public void create()
            throws Exception
    {
        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();
    }

    @Test
    public void update()
            throws Exception
    {
        // first we have to do a create just as in the previous test
        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
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

        SwordResponse response = depositor.update(location, new AuthCredentials("test", "test"), update);
        System.out.println(response.getLocation());

        out.delete();
    }

    @Test
    public void delete()
            throws Exception
    {
        // first create an item to be deleted

        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now delete it

        SwordResponse response = depositor.delete(receipt.getLocation(), new AuthCredentials("test", "test"));
    }

    @Test
    public void setGradeWithPermanentEmbargo()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade

        SwordResponse response = depositor.setGradeWithPermanentEmbargo(receipt.getLocation(), new AuthCredentials("test", "test"), "pass");
    }

    @Test
    public void setGrade()
            throws Exception
    {
        // first create an item to add the grade to

        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setGrade(receipt.getLocation(), new AuthCredentials("test", "test"), "pass", embargo, "a while");
    }

    @Test
    public void setEmbargo()
            throws Exception
    {
        // first create an item forfeit the appeal for

        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setEmbargo(receipt.getLocation(),  new AuthCredentials("test", "test"), embargo, "embargo");
    }

    @Test
    public void forfeitGradeAppeal()
            throws Exception
    {
        // first create an item forfeit the appeal for

        EndpointDiscovery ed = new EndpointDiscovery("http://localhost:8080/swordv2/servicedocument", null, null, null, new AuthCredentials("test", "test"));
        List<SWORDCollection> cols = ed.getEndpoints();
        SWORDCollection col = cols.get(0);

        String fileBase = "/home/richard/Code/External/BagItLibrary/src/test/resources/testbags/testfiles/";

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
        DepositReceipt receipt = depositor.create(col.getHref().toString(), new AuthCredentials("test", "test"), bi);
        System.out.println(receipt.getLocation());

        out.delete();

        // now set the grade with a near embargo
        Date embargo = new Date((new Date()).getTime() + 1000000000L);
        SwordResponse response = depositor.setGrade(receipt.getLocation(), new AuthCredentials("test", "test"), "fail", embargo, "a while");

        // now forfeit the appeal process

        // which means what, exactly ...
    }
}
