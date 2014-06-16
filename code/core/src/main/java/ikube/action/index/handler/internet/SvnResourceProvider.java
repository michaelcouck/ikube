package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableSvn;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
@SuppressWarnings("unchecked")
class SvnResourceProvider implements IResourceProvider<SVNDirEntry> {

    private static final int SLEEP_TIME = 1000;
    private static final int MAX_RESOURCES = 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pattern pattern;
    private final Stack<SVNDirEntry> svnDirEntries;

    SvnResourceProvider(final IndexableSvn indexableSvn) throws Exception {
        svnDirEntries = new Stack<>();
        if (StringUtils.isEmpty(indexableSvn.getExcludedPattern())) {
            pattern = null;
        } else {
            pattern = Pattern.compile(indexableSvn.getExcludedPattern());
        }
        SVNURL svnurl = SVNURL.parseURIEncoded(indexableSvn.getUrl());
        final SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        ISVNAuthenticationManager authManager = SVNWCUtil
                .createDefaultAuthenticationManager(indexableSvn.getUsername(), indexableSvn.getPassword());
        repository.setAuthenticationManager(authManager);
        indexableSvn.setRepository(repository);

        logger.info("Starting walk : ");
        walkRepository(repository, indexableSvn.getFilePath());
        logger.info("Walk finished normally : ");

        ThreadUtilities.submit(indexableSvn.getName(), new Runnable() {
            public void run() {
                while (!svnDirEntries.isEmpty()) {
                    ThreadUtilities.sleep(1000);
                }
                try {
                    repository.closeSession();
                } catch (final Exception e) {
                    logger.error(null, e);
                }
                indexableSvn.setRepository(null);
                logger.info("Terminating walk : ");
                ThreadUtilities.destroy(indexableSvn.getName());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SVNDirEntry getResource() {
        try {
            SVNDirEntry svnDirEntry = null;
            if (!svnDirEntries.isEmpty()) {
                svnDirEntry = svnDirEntries.pop();
            }
            logger.info("Popped : " + svnDirEntries.size() + ", " + svnDirEntry);
            return svnDirEntry;
        } finally {
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<SVNDirEntry> resources) {
        if (resources != null) {
            svnDirEntries.addAll(resources);
        }
    }

    protected void walkRepository(final SVNRepository repository, final String filePath) {
        SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
        Collection entries;
        try {
            logger.info("Getting entries for : " + filePath);
            entries = repository.getDir(filePath, -1, fileProperties, new ArrayList());
        } catch (final SVNException e) {
            logger.error("Exception accessing svn resource : " + filePath, e);
            return;
        }
        logger.info("Entries : " + entries.size());
        for (final Object entry : entries) {
            SVNDirEntry dirEntry = (SVNDirEntry) entry;
            String relativePath = dirEntry.getRelativePath();
            String childFilePath = filePath + "/" + relativePath;
            if (isExcluded(childFilePath, pattern)) {
                continue;
            }
            SVNNodeKind svnNodeKind = dirEntry.getKind();
            if (SVNNodeKind.FILE.equals(svnNodeKind)) {
                while (svnDirEntries.size() > MAX_RESOURCES) {
                    ThreadUtilities.sleep(SLEEP_TIME);
                }
                svnDirEntries.add(dirEntry);
            } else {
                logger.info("Recursing, walking directory : " + childFilePath);
                walkRepository(repository, childFilePath);
            }
        }
    }

    /**
     * This method checks if a string should be excluded according to a certain pattern.
     *
     * @param string  the string to check for inclusion in the processing
     * @param pattern the pattern that excludes explicitly the resource
     * @return whether this string is not excluded and can be processed
     */
    private boolean isExcluded(final String string, final Pattern pattern) {
        if (pattern == null) {
            return Boolean.FALSE;
        }
        if (StringUtils.isEmpty(string)) {
            return Boolean.TRUE;
        }
        boolean excluded = pattern.matcher(string).matches();
        if (excluded) {
            logger.info("Excluding : " + string);
        }
        return excluded;
    }
}