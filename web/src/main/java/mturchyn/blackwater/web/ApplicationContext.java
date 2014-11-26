package mturchyn.blackwater.web;

import mturchyn.blackwater.core.config.Schema;
import mturchyn.blackwater.core.config.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationContext {

    private final static Logger LOG = LogManager.getLogger(ApplicationContext.class);

    private static ApplicationContext instance = null;

    private String homeDir;
    private Settings settings;
    private Schema schema;

    public static ApplicationContext init(String homeDir) {
        LOG.debug("Creating instance of application context. Home dir: {}", homeDir);
        instance = new ApplicationContext(homeDir);
        return instance;
    }

    public static ApplicationContext getInstance() {
        return instance;
    }

    private ApplicationContext(final String homeDir) {
        this.homeDir = normalizeFilePath(homeDir);
        initSettings();
        initSchema();
    }

    private void initSchema() {
        File schemaFile = new File(this.homeDir + "schema.xml");

        if (!schemaFile.exists()) {
            LOG.error("Cannot find file {}", schemaFile.getAbsolutePath());
            throw new RuntimeException(schemaFile.getAbsolutePath() + " doesn't exist");
        }

        if (LOG.isDebugEnabled()) {
            logFileContent(schemaFile);
        }
        try {
            JAXBContext context = JAXBContext.newInstance(Schema.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            schema = (Schema) unmarshaller.unmarshal(schemaFile);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot initialize schema.", e);
        }
    }

    private void initSettings() {
        File settingsFile = new File(this.homeDir + "settings.xml");
        if (!settingsFile.exists()) {
            LOG.error("Cannot find file {}", settingsFile.getAbsolutePath());
            throw new RuntimeException(settingsFile.getAbsolutePath() + " doesn't exist");
        }

        if (LOG.isDebugEnabled()) {
            logFileContent(settingsFile);
        }

        try {
            JAXBContext context = JAXBContext.newInstance(Settings.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            settings = (Settings) unmarshaller.unmarshal(settingsFile);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot initialize settings.", e);
        }
    }

    private void logFileContent(File textFile) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(textFile.getAbsolutePath()));
            String fileContentAsString = new String(fileContent, "UTF-8");
            LOG.debug(textFile.getName() + " file content:\n{}", fileContentAsString);
        } catch (IOException e) {
            LOG.error("Error while reading content of " + textFile.getAbsolutePath(), e);
        }
    }

    private String normalizeFilePath(String homeDir) {
        return homeDir.lastIndexOf("\\") == homeDir.length() - 1 ? homeDir : homeDir + "\\";
    }

    public Settings getSettings() {
        return settings;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getHomeDir() {
        return homeDir;
    }
}
