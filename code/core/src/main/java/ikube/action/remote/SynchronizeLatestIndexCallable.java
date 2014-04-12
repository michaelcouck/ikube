package ikube.action.remote;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This callable will get all the files in the latest index directory on the target server.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-04-2014
 */
public class SynchronizeLatestIndexCallable implements Callable<String[]>, Serializable {

	private IndexContext indexContext;

	public SynchronizeLatestIndexCallable(final IndexContext indexContext) {
		this.indexContext = indexContext;
	}

	@Override
	public String[] call() throws Exception {
		final List<String> filePaths = new ArrayList<>();
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		System.out.println("Latest index directory : " + latestIndexDirectory);
		if (latestIndexDirectory != null && latestIndexDirectory.exists()) {
			Files.walkFileTree(latestIndexDirectory.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) {
					File file = path.toFile();
					if (file.isFile()) {
						filePaths.add(file.getAbsolutePath());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			System.out.println("Index directory not initialized : " + indexDirectoryPath);
		}
		return filePaths.toArray(new String[filePaths.size()]);
	}

}
