package ikube.action.index.handler;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexableFileSystemHandler extends IndexableHandler<IndexableFileSystem> {

	class ResourceProvider implements IResourceProvider<File> {

		Stack<File> files = new Stack<File>();

		ResourceProvider(final IndexableFileSystem indexableFileSystem) throws IOException {
			Files.walkFileTree(new File(indexableFileSystem.getPath()).toPath(), new SimpleFileVisitor<Path>() {
				public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
					File file = path.toFile();
					logger.info("File : " + file + ", files : " + files.size());
					while (files.size() > IConstants.MILLION) {
						ThreadUtilities.sleep(1000);
					}
					files.push(file);
					return super.visitFile(path, attrs);
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

	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableFileSystem indexableFileSystem) throws Exception {
		final AtomicInteger threads = new AtomicInteger(indexableFileSystem.getThreads());
		ForkJoinPool forkJoinPool = new ForkJoinPool(threads.get());
		ResourceProvider fileResourceProvider = new ResourceProvider(indexableFileSystem);
		RecursiveAction recursiveAction = getRecursiveAction(indexContext, indexableFileSystem, fileResourceProvider);
		forkJoinPool.invoke(recursiveAction);
		return new ArrayList<Future<?>>(Arrays.asList(recursiveAction));
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}