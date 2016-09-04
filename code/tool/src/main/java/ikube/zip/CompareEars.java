package ikube.zip;

import de.schlichtherle.truezip.file.TFile;
import ikube.toolkit.FILE;
import org.apache.commons.lang.StringUtils;

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
            "BTMUProcess-5.6.1.ear",
            "BTMUTestApp-5.6.1.ear",
            "BTMUUnitTestEAR-5.6.1.ear",
            "JP1ClientEAR-5.6.1.ear",
            "LVPProcess-5.6.1.ear"
    };

    public static void main(final String[] args) throws IOException {
        int increment = 2;
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
        for (final String sourceZipEntry : sourceEntries) {
            if (!targetEntries.contains(sourceZipEntry)) {
                System.out.println("        : " + sourceZipEntry);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static Set<String> getZipEntries(final TFile parentTFile, final Set<String> zipEntries) {
        // Set<String> zipEntries = new TreeSet<String>();
        for (final TFile childTFile : parentTFile.listFiles()) {
            boolean isDirArcJarorWar = childTFile.isDirectory() || childTFile.isArchive() ||
                    childTFile.getName().endsWith(".jar") || childTFile.getName().endsWith(".war");
            if (isDirArcJarorWar) {
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