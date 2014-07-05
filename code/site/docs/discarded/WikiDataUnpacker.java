package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This class will unpack the 7z files from Wiki and repack the data into Bzip2 files. Then unpack the Bzip2 files onto the disks.
 *
 * @author Michael Couck
 * @version 01.00
 * @since at least 14-04-2012
 */
public class WikiDataUnpacker {

    static final Logger LOGGER;

    static {
        // Init the logging config
        Logging.configure();
        LOGGER = Logger.getLogger(WikiDataUnpacker.class);
    }

    static final String WRITE = "write";
    static final String UNPACK = "unpack";
    static final String UNPACK_SINGLES = "unpack-singles";
    static final String REPACK = "repack";

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }
        ThreadUtilities.initialize();
        try {
            switch (args[0]) {
                case WRITE:
                    read7ZandWriteBzip2(args[1]);
                    break;
                case UNPACK:
                    readBz2AndUnpackFiles(args[1]);
                    break;
                case REPACK:
                    readBz2AndWriteBzip2(args[1]);
                    break;
                case UNPACK_SINGLES:
                    String[] outputDisksPaths = new String[args.length - 2];
                    System.arraycopy(args, 2, outputDisksPaths, 0, outputDisksPaths.length);
                    LOGGER.info("Input file : " + args[1] + ", disks : " + Arrays.deepToString(outputDisksPaths));
                    read7ZAndUnpackFiles(args[1], outputDisksPaths);
                    break;
                default:
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            printUsage();
            e.printStackTrace();
        }
    }

    /**
     * This method will read the 7z history of the wiki, unpack the compressed file, break it up into segments of one giga-byte then write the file to a
     * compressed bzip2 file.
     *
     * @param directoryPath the path to the input files, i.e. the 7z files to convert
     */
    @SuppressWarnings("unchecked")
    protected static void read7ZandWriteBzip2(final String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        List<File> files = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), "7z");
        List<Future<Object>> futures = new ArrayList<>();
        for (final File file : files) {
            WikiDataUnpacker7ZWorker dataUnpacker7ZWorker = new WikiDataUnpacker7ZWorker(file, 1000);
            Future<Object> future = (Future<Object>) ThreadUtilities.submit(null, dataUnpacker7ZWorker);
            futures.add(future);
            if (futures.size() >= 4) {
                ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
                futures.clear();
            }
        }
        ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
    }

    /**
     * This method will read the bzip2 files one by one and unpack them onto the external disks.
     *
     * @param directoryPath the path to the files to unpack
     */
    @SuppressWarnings("unchecked")
    protected static void readBz2AndUnpackFiles(final String directoryPath) throws Exception {
        File directory = new File(directoryPath);
        List<Future<Object>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            WikiDataUnpackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerWorker(directory);
            Future<Object> future = (Future<Object>) ThreadUtilities.submit(null, wikiDataUnpackerWorker);
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
    }

    /**
     * This method will read all the Bzip2 files in the specified directory and write them to smaller one gig files that can be read easier over the network.
     *
     * @param directoryPath the path to the directory where the compressed files are
     */
    @SuppressWarnings("unchecked")
    protected static void readBz2AndWriteBzip2(final String directoryPath) {
        File directory = new File(directoryPath);
        List<File> bZip2Files = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), "bz2");
        List<Future<Object>> futures = new ArrayList<>();
        for (final File bZip2File : bZip2Files) {
            WikiDataUnpackerRepackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerRepackerWorker(bZip2File);
            Future<Object> future = (Future<Object>) ThreadUtilities.submit(null, wikiDataUnpackerWorker);
            futures.add(future);
            if (futures.size() >= 4) {
                ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
                futures.clear();
            }
        }
        ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
    }

    /**
     * This method will start a single thread, that will read the input file(in 7Zip format), decompress it and write it to the disks specified, distributing
     * the files randomly over the disk in multiple folders containing several thousand files in eash.
     *
     * @param inputFilePath    the input file
     * @param outputDisksPaths the disks to write the data to
     * @throws Exception
     */
    @SuppressWarnings({"MismatchedReadAndWriteOfArray", "unchecked"})
    protected static void read7ZAndUnpackFiles(final String inputFilePath, final String... outputDisksPaths) throws Exception {
        File inputFile = FileUtilities.getFile(inputFilePath, Boolean.FALSE);
        File[] outputDisks = new File[outputDisksPaths.length];
        int index = 0;
        for (final String outputDiskPath : outputDisksPaths) {
            outputDisks[index++] = FileUtilities.getFile(outputDiskPath, Boolean.TRUE);
        }
        Future<Object> future = (Future<Object>) ThreadUtilities.submit(null, new WikiDataUnpacker7ZFileWorker(inputFile));
        List<Future<Object>> futures = new ArrayList<>(Arrays.asList(future));
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
    }

    private static void printUsage() {
        System.out.println("Usage   : [" + WRITE + " | " + UNPACK + " | " + REPACK + "] & [directory] & [disk patterns, like 'xfs-' for example]");
        System.out.println("Example to write bz2 files : java -jar ikube.jar write /media/nas/xfs/wiki-history-languages");
        System.out.println("Example to unpack html files from bz2 : java -jar ikube.jar unpack /media/nas/xfs/wiki-history-languages");
        System.out.println("Example to repack bz2 files : java -jar ikube.jar repack /media/nas/xfs/wiki-history-languages");
    }

}
