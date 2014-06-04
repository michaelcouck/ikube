package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableSvn;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
class SvnResourceProvider implements IResourceProvider<SVNDirEntry> {

	private static final int SLEEP_TIME = 1000;
	private static final int MAX_RESOURCES = 1000;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Stack<SVNDirEntry> svnDirEntries;

	SvnResourceProvider(final IndexableSvn indexableSvn) throws Exception {
		svnDirEntries = new Stack<>();
		final SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(indexableSvn.getUrl()));
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(indexableSvn.getUsername(), indexableSvn.getPassword());
		repository.setAuthenticationManager(authManager);
		ThreadUtilities.submit(indexableSvn.getName(), new Runnable() {
			public void run() {
				try {
					walkRepository(repository, indexableSvn.getFilePath());
				} finally {
					ThreadUtilities.destroy(indexableSvn.getName());
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized SVNDirEntry getResource() {
		synchronized (svnDirEntries) {
			if (svnDirEntries.isEmpty()) {
				return null;
			}
			return svnDirEntries.pop();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(final List<SVNDirEntry> resources) {
		synchronized (svnDirEntries) {
			svnDirEntries.addAll(resources);
		}
	}

	private void walkRepository(final SVNRepository repository, final String filePath) {
		SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
		Collection entries;
		try {
			entries = repository.getDir(filePath, -1, fileProperties, new ArrayList());
		} catch (final SVNException e) {
			logger.error("Exception accessing svn resource : " + filePath, e);
			return;
		}
		for (final Object entry : entries) {
			SVNDirEntry dirEntry = (SVNDirEntry) entry;
			String relativePath = dirEntry.getRelativePath();
			String childFilePath = filePath + "/" + relativePath;
			SVNNodeKind svnNodeKind = dirEntry.getKind();
			if (SVNNodeKind.FILE.equals(svnNodeKind)) {
				while (svnDirEntries.size() > MAX_RESOURCES) {
					ThreadUtilities.sleep(SLEEP_TIME);
				}
				svnDirEntries.add(dirEntry);
			}
			walkRepository(repository, childFilePath);
		}
	}
}