package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.concurrent.Callable;

import static ikube.action.SynchronizeCallable.*;

/**
 * TODO: This needs to be implemented completely.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30-03-2014
 */
public class SynchronizeCallable implements Callable<FileChunk>, Serializable {

    public static class FileChunk {
        // Name of the file to read/write
        public String name;
        // The length of the chunk to read from the file
        public int length;
        // The offert in the file to read from, or write to
        public int offset;
        // The chunk of the file at the specified position
        public byte[] chunk;
    }

    private FileChunk fileChunk;
    private IndexContext indexContext;

    public SynchronizeCallable(final FileChunk fileChunk, final IndexContext indexContext) {
        this.fileChunk = fileChunk;
        this.indexContext = indexContext;
    }

    @Override
    public FileChunk call() throws Exception {
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);

        File indexFile = new File(latestIndexDirectory, fileChunk.name);
        RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "rw");
        randomAccessFile.seek(fileChunk.offset);

        fileChunk.chunk = new byte[fileChunk.length];

        fileChunk.length = randomAccessFile.read(fileChunk.chunk, 0, fileChunk.length);

        LZ4Factory factory = LZ4Factory.fastestInstance();

        // byte[] data = "12345345234572".getBytes("UTF-8");
        // final int decompressedLength = data.length;
        final int decompressedLength = fileChunk.length;

        // compress data
        LZ4Compressor compressor = factory.fastCompressor();
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        // int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
        int compressedLength = compressor.compress(fileChunk.chunk, 0, decompressedLength, compressed, 0, maxCompressedLength);

        System.out.println("Compressed length : " + compressedLength + ", " + decompressedLength);

        return fileChunk;
    }

}
