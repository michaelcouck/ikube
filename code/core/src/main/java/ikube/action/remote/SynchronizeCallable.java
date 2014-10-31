package ikube.action.remote;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * This class will read from a file, probably on a remote server, and return a byte array
 * of the chunk of file, starting at an offset in the file and going the specified length. In
 * the event there is no more data to read from the file, i.e. the offset is past the end
 * of the file then the return byte array will have a zero length.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30-03-2014
 */
public class SynchronizeCallable implements Callable<byte[]>, Serializable {

	/**
	 * The absolute path to the file on the remote server
	 */
	private String indexFile;
	/**
	 * The offset to start reading from in the file stream
	 */
	private long offset;
	/**
	 * The length of data to read from the target file
	 */
	private long length;

	public SynchronizeCallable(final String indexFile, final long offset, final long length) {
		this.indexFile = indexFile;
		this.offset = offset;
		this.length = length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] call() throws Exception {
		byte[] bytes = new byte[0];
		RandomAccessFile randomAccessFile = null;
		try {
			File indexFile = new File(this.indexFile);
			randomAccessFile = new RandomAccessFile(indexFile, "rw");
			if (randomAccessFile.length() > offset) {
				bytes = new byte[(int) length];
				randomAccessFile.seek(offset);
				@SuppressWarnings("UnusedDeclaration")
				long pointer = randomAccessFile.getFilePointer();
				long read = randomAccessFile.read(bytes);
				// System.out.println("Offset : " + offset + ", " + pointer + ", " + read);
				if (read < length) {
					byte[] holder = new byte[(int) read];
					System.arraycopy(bytes, 0, holder, 0, holder.length);
					bytes = holder;
				}
			}
		} finally {
			IOUtils.closeQuietly(randomAccessFile);
		}
		return bytes;
	}

	/**
	 * This method can be used to compress the data over the wire.
	 *
	 * @param chunk the chunk of data to compress
	 * @return the compressed binary array of data
	 */
	@SuppressWarnings("UnusedDeclaration")
	private byte[] compress(final byte[] chunk) {
		LZ4Factory factory = LZ4Factory.fastestInstance();
		long decompressedLength = length;
		LZ4Compressor compressor = factory.fastCompressor();
		long maxCompressedLength = compressor.maxCompressedLength((int) decompressedLength);
		byte[] compressed = new byte[(int) maxCompressedLength];
		long compressedLength = compressor.compress(chunk, 0, (int) decompressedLength, compressed, 0, (int) maxCompressedLength);
		return compressed;
	}

}
