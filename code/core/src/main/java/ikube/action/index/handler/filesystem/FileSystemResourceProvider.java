package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileSystemResourceProvider implements IResourceProvider<File> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResourceProvider.class);

	private boolean finished = Boolean.FALSE;
	private Stack<File> files = new Stack<File>();

	FileSystemResourceProvider(final IndexableFileSystem indexableFileSystem) throws IOException {
		File startDirectory = new File(indexableFileSystem.getPath());
		Path startPath = startDirectory.toPath();
		class SimpleFileVisitorWalker extends SimpleFileVisitor<Path> {
			public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
				File file = path.toFile();
				if (file != null && file.exists()) {
					while (files.size() > IConstants.MILLION) {
						ThreadUtilities.sleep(1000);
					}
					files.push(file);
					return super.visitFile(path, attrs);
				}
				return FileVisitResult.SKIP_SUBTREE;
			}
		}
		Files.walkFileTree(startPath, new SimpleFileVisitorWalker());
		finished = Boolean.TRUE;
		LOGGER.info("Finished : " + finished);
	}

	public synchronized File getResource() {
		if (files.size() == 0) {
			if (!finished) {
				LOGGER.info("No more files, waiting for walker : ");
				ThreadUtilities.sleep(10000);
				if (files.size() > 0) {
					return files.pop();
				}
			}
			return null;
		}
		return files.pop();
	}

	@Override
	public void setResources(final List<File> resources) {
		this.files.addAll(resources);
	}

}
