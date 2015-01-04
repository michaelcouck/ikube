package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableSvn;
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
 * This provider will walk over an SVN repository, adding all the entries to a stack. This
 * collection can then be accessed by clients, taking resources off the top as needed until there are,
 * no more resources to take, each get resource pops the next file from the stack.
 * <p/>
 * The session is not closed to the SVN {@link org.tmatesoft.svn.core.io.SVNRepository} as clients
 * will still need to read the files, they are not completely downloaded in the provider, for obvious
 * reasons.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 04-06-2014
 */
@SuppressWarnings("unchecked")
class SvnResourceProvider implements IResourceProvider<SVNDirEntry> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The pattern to ignore when fetching resources, like .*(private)*. for example.
     */
    private final Pattern pattern;
    /**
     * The stack of {@link org.tmatesoft.svn.core.SVNDirEntry} that will be put on the stack while
     * walking the SVN directories. Note that these are just references to the files, they do not contain
     * the contents of the files. The contents still need to be read using the
     * {@link org.tmatesoft.svn.core.io.SVNRepository}.
     */
    private final Stack<SVNDirEntry> svnDirEntries;
    /**
     * Flag set to true when the job is terminated.
     */
    private boolean terminated;

    SvnResourceProvider(final IndexableSvn indexableSvn) throws Exception {
        svnDirEntries = new Stack<>();
        if (StringUtils.isEmpty(indexableSvn.getExcludedPattern())) {
            pattern = null;
        } else {
            pattern = Pattern.compile(indexableSvn.getExcludedPattern());
        }
        SVNURL svnurl = SVNURL.parseURIEncoded(indexableSvn.getUrl());
        final SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        if (StringUtils.isNotEmpty(indexableSvn.getUsername()) && StringUtils.isNotEmpty(indexableSvn.getPassword())) {
            ISVNAuthenticationManager authManager = SVNWCUtil
                    .createDefaultAuthenticationManager(indexableSvn.getUsername(), indexableSvn.getPassword());
            repository.setAuthenticationManager(authManager);
        }
        indexableSvn.setRepository(repository);

        logger.warn("Starting walk : ");
        walkRepository(repository, indexableSvn.getFilePath());
        logger.warn("Walk finished normally : ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SVNDirEntry getResource() {
        if (isTerminated()) {
            return null;
        }
        try {
            SVNDirEntry svnDirEntry = null;
            if (!svnDirEntries.isEmpty()) {
                svnDirEntry = svnDirEntries.pop();
                logger.warn("Popped : " + svnDirEntries.size() + ", " + svnDirEntry);
            }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
    }

    protected void walkRepository(final SVNRepository repository, final String filePath) {
        if (isTerminated()) {
            return;
        }
        SVNProperties fileProperties = SVNProperties.wrap(new HashMap<>());
        Collection entries;
        try {
            entries = repository.getDir(filePath, -1, fileProperties, new ArrayList());
            logger.warn("Files in directory : " + filePath + " : " + entries.size() + ", " + svnDirEntries.size());
        } catch (final SVNException e) {
            logger.error("Exception accessing svn resource : " + filePath, e);
            return;
        }
        for (final Object entry : entries) {
            SVNDirEntry dirEntry = (SVNDirEntry) entry;
            try {
                String relativePath = dirEntry.getRelativePath();
                String childFilePath = filePath + "/" + relativePath;
                if (!isExcluded(childFilePath, pattern)) {
                    SVNNodeKind svnNodeKind = dirEntry.getKind();
                    if (SVNNodeKind.FILE.equals(svnNodeKind)) {
                        svnDirEntries.add(dirEntry);
                    } else {
                        logger.info("Recursing, walking directory : " + childFilePath);
                        walkRepository(repository, childFilePath);
                    }
                }
            } catch (final Exception e) {
                logger.error("Exception walking the svn repository : " + dirEntry, e);
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
            logger.warn("Excluding : " + string);
        }
        return excluded;
    }
}