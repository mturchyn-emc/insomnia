package mturchyn.blackwater.integration;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class Tomcat {

    private final String tomcatHome;

    public Tomcat(String tomcatHome) {
        this.tomcatHome = tomcatHome;
    }

    public void putWarFile(File warFile) throws IOException{
        removeBlackwaterJarFiles();
        File dest = new File(tomcatHome + "webapps\\blackwater.war");
        FileUtils.copyFile(warFile, dest);
    }

    public void stop() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(tomcatHome + "bin\\shutdown.bat");
        Map<String,String> environment = processBuilder.environment();
        environment.put("JAVA_HOME", "c:\\Program Files\\Java\\jdk1.8.0_20\\");
        processBuilder.directory(new File(tomcatHome));
        Process process = processBuilder.start();
    }

    public void start() throws IOException{
        ProcessBuilder processBuilder = new ProcessBuilder(tomcatHome + "bin\\startup.bat");
        Map<String,String> environment = processBuilder.environment();
        environment.put("JAVA_HOME", "c:\\Program Files\\Java\\jdk1.8.0_20\\");
        environment.put("blackwater.home", "c:\\Users\\Maksim\\monica\\");
        processBuilder.directory(new File(tomcatHome));
        Process process = processBuilder.start();


    }

    public void removeBlackwaterJarFiles() throws IOException {
        File webappDir = new File(tomcatHome + "webapps");
        File[] blackwaterFiles = webappDir.listFiles((File pathname) -> {
            return pathname.getName().contains("blackwater");
        });
        for (File file : blackwaterFiles) {
            delete(file);
        }
    }

    void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new RuntimeException("Failed to delete file: " + f);
    }
}
