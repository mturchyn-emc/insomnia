import mturchyn.blackwater.integration.Constants;
import mturchyn.blackwater.integration.IntegrationTestSuite;
import mturchyn.blackwater.integration.NoteDao;
import mturchyn.blackwater.integration.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

public class BlackWaterIT {

    public static final int SEC_TO_SLEEP = 8;
    private static Tomcat tomcat;
    private static ConfigurableApplicationContext context;
    private static NoteDao noteDao;

    @BeforeClass
    public static void setUp() throws Exception {
        tomcat = new Tomcat(Constants.TOMCAT_HOME);
        tomcat.stop();

        File warFile = new File(Constants.BLACKWATER_WAR_FILE_PATH);
        tomcat.putWarFile(warFile);
        tomcat.start();
        Thread.sleep(1000 * SEC_TO_SLEEP); // Wait for it.

        context = new ClassPathXmlApplicationContext("integration-context.xml");
        noteDao = (NoteDao) context.getBean("noteDao");
    }

    @Test
    public void testLightLoad() throws Exception {
        IntegrationTestSuite lightLoad = new IntegrationTestSuite(noteDao, 50, 20_000, 10);

        lightLoad.resetIndex();
        lightLoad.putNotes();
        lightLoad.readNotes();
        lightLoad.close();
    }

    @Test
    public void testHeavyLoad() throws Exception {
        IntegrationTestSuite heavyLoad = new IntegrationTestSuite(noteDao, 50, 200_000, 100);

        heavyLoad.resetIndex();
        heavyLoad.putNotes();
        heavyLoad.readNotes();
        heavyLoad.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        tomcat.stop();
        context.close();
        tomcat.removeBlackwaterJarFiles();
    }
}
