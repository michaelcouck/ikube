package ikube.zip;

import de.schlichtherle.truezip.file.TFile;
import ikube.toolkit.FILE;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("WeakerAccess")
public class CompareEars {

    static String[] originalEarFiles = {
            "BTMUApp.ear",
            "BTMUInterfacingSimulatorEAR.ear",
            "BTMUProcess.ear",
            "BTMUTestApp.ear",
            "BTMUUnitTestEAR.ear",
            "JP1ClientEAR.ear",
            "LVPProcess.ear"};
    static String[] mavenizedEarFiles = {
            "BTMUApp-5.6.1.ear",
            "BTMUInterfacingSimulatorEAR-5.6.1.ear",
            "BTMUProcessEAR-5.6.1.ear",
            "BTMUTestApp-5.6.1.ear",
            "BTMUUnitTestEAR-5.6.1.ear",
            "JP1ClientEAR-5.6.1.ear",
            "LVPProcess-5.6.1.ear"
    };

    public static void main(final String[] args) throws IOException {
        int increment = 0;
        for (int i = increment; i < increment + 1; i++) {
            String sourceFile = originalEarFiles[i];
            String targetFile = mavenizedEarFiles[i];
            System.out.println(sourceFile + ":" + targetFile);
            TFile sourceZipFile = new TFile(FILE.findFileRecursively(new File("/home/laptop/Downloads"), sourceFile));
            TFile targetZipFile = new TFile(FILE.findFileRecursively(new File("/home/laptop/Workspace/btmu-upgrade/"), targetFile));

            sourceToTarget(sourceZipFile, targetZipFile);
            sourceToTarget(targetZipFile, sourceZipFile);
        }
    }

    private static void sourceToTarget(final TFile sourceZipFile, final TFile targetZipFile) {
        Set<String> sourceEntries = getZipEntries(sourceZipFile, new TreeSet<String>());
        Set<String> targetEntries = getZipEntries(targetZipFile, new TreeSet<String>());
        System.out.println("Source not in target : " + sourceZipFile.getName());
        Document document = DocumentFactory.getInstance().createDocument();
        Element root = document.addElement("root");
        System.out.println(document.asXML());
        for (final String sourceZipEntry : sourceEntries) {
            if (!targetEntries.contains(sourceZipEntry)) {
                Element dependency = root.addElement("dependency");
                Element groupId = dependency.addElement("groupId");
                groupId.setText("fill-in");
                Element artifactId = dependency.addElement("artifactId");
                Element version = dependency.addElement("version");
                version.setText("fill-in");

                artifactId.setText(sourceZipEntry);
                // System.out.println("        : " + sourceZipEntry);

                System.out.println(dependency.asXML());
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static Set<String> getZipEntries(final TFile parentTFile, final Set<String> zipEntries) {
        // Set<String> zipEntries = new TreeSet<String>();
        for (final TFile childTFile : parentTFile.listFiles()) {
            boolean isDirArcJarOrWar = childTFile.isDirectory() || childTFile.isArchive() ||
                    childTFile.getName().endsWith(".jar") || childTFile.getName().endsWith(".war");
            if (isDirArcJarOrWar) {
                if (childTFile.getName().endsWith(".jar") || childTFile.getName().endsWith(".war")) {
                    String fileName = StringUtils.substringBefore(childTFile.getName(), ".");
                    fileName = StringUtils.substringBefore(fileName, "-");
                    zipEntries.add(fileName);
                }
                if (childTFile.isDirectory()) {
                    getZipEntries(childTFile, zipEntries);
                }
            }
        }
        return zipEntries;
    }

}