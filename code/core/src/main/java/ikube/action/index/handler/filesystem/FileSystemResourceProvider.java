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

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Stack<File> files = new Stack<File>();

	FileSystemResourceProvider(final IndexableFileSystem indexableFileSystem) throws IOException {
		Files.walkFileTree(new File(indexableFileSystem.getPath()).toPath(), new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
				File file = path.toFile();
				if (file != null && file.exists() && file.canRead()) {
					logger.info("File : " + file + ", files : " + files.size());
					while (files.size() > IConstants.MILLION) {
						ThreadUtilities.sleep(1000);
					}
					files.push(file);
					return super.visitFile(path, attrs);
				}
				return FileVisitResult.SKIP_SUBTREE;
			}
		});
	}

	public File getResource() {
		if (files.size() == 0) {
			return null;
		}
		File file = files.pop();
		logger.info("Files left : " + files.size());
		return file;
	}

	@Override
	public void setResources(List<File> resources) {
	}

}
