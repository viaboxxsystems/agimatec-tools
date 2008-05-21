package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;
import com.agimatec.tools.nls.output.MBJSONPersistencer;
import com.agimatec.tools.nls.output.MBXMLPersistencer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 * MessageBundleTask Tester.
 *
 * @author ${USER}
 * @since <pre>06/14/2007</pre>
 * @version 1.0
 */
public class MessageBundleTaskTest extends TestCase {
    public MessageBundleTaskTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSaveExampleXML() throws Exception
    {
        MBBundles bundles = new MBBundles();
        MBBundle bundle = new MBBundle();
        bundle.setBaseName("Example");
        bundle.setInterfaceName("com.agimatec.I_Example");
        MBEntry entry = new MBEntry();
        entry.setKey("key1");
        MBText text = new MBText();
        text.setLocale("de");
        text.setValue("Beispieltext mit ÄÖÜ");
        entry.getTexts().add(text);
        text = new MBText();
        text.setLocale("en");
        text.setValue("An example");
        entry.getTexts().add(text);
        bundle.getEntries().add(entry);

        entry = new MBEntry();
        entry.setKey("key2");
        text = new MBText();
        text.setLocale("de");
        text.setValue("anderer Wert");
        entry.getTexts().add(text);
        bundle.getEntries().add(entry);

        bundles.getBundles().add(bundle);

        MBXMLPersistencer p = new MBXMLPersistencer();
        p.save(bundles, new File("target/example.xml"));

        MBJSONPersistencer p2 = new MBJSONPersistencer(true);
        p2.save(bundles, new File("target/example-json.js"));
    }

    public void testGenerate() throws Exception {


        MessageBundleTask task = new MessageBundleTask();
        task.setProject(new Project());
        task.setBundles( "example/example.xml");
        task.setOverwrite(true);
        task.setDeleteOldFiles(false);
        task.setPropertyPath("target/out-properties");
        task.setJsonPath("target/out-json");
        task.setSourcePath("target/out-src");
        task.setWriteProperties("true");
        task.setWriteJson("true");
        task.setWriteInterface("true");
        task.execute();
    }

    public static Test suite() {
        return new TestSuite(MessageBundleTaskTest.class);
    }
}
