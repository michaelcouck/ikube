/*
 * Copyright 2009 De Post, N.V. - La Poste, S.A. All rights reserved
 */
package ikube.toolkit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import java.security.Principal;

/**
 * A utility class that finds out what the name of the currently active user is.
 * 
 * @author (premerg)
 * @since 26-feb-09 11:21:16
 * 
 * @revision $Rev$
 * @lastChangedBy $Author$
 * @lastChangedDate $Date$
 */
public class UserNameResolver {

    /**
     * The key in the request local storage.
     */
    private static final String USER_NAME = "unr_name";

    /**
     * Our Log4J logger.
     */
    private static final Logger LOG = Logger.getLogger(UserNameResolver.class);

    /**
     * The user name used when none can be determined from the environment.
     */
    private static final String ANONYMOUS = "anonym";
    
    /**
     * Don't construct directly.
     */
    private UserNameResolver() {
    }

    /**
     * Returns the user name of the caller.
     * 
     * @return the user name of the caller
     */
    public static String getCallerUserName() {
        String caller = null; // RequestLocalStorage.get(USER_NAME);
        if (!StringUtils.isEmpty(caller)) {
            return caller;
        }
        Principal callerPrincipal = getCallerPrincipal();
        if (callerPrincipal != null) {
            setCallerUserName(callerPrincipal.getName());
            return callerPrincipal.getName();
        }
        return ANONYMOUS;
    }

    /**
     * Set the user name for a caller so that other callers in the same thread can retrieve it.
     * 
     * @param name The name of the original caller
     */
    public static void setCallerUserName(String name) {
        // RequestLocalStorage.put(USER_NAME, name);
    }

    /**
     * Fetches the caller principal.
     * 
     * @return the caller principal.
     */
    private static Principal getCallerPrincipal() {
        try {
            InitialContext ic = new InitialContext();
            SessionContext sc = (SessionContext) ic.lookup("java:comp/EJBContext");
            return sc.getCallerPrincipal();
        } catch (NoInitialContextException e) {
            LOG.debug("Running out-of-container", e);
        } catch (NamingException e) {
            LOG.warn("Unable to look up caller principal", e);
        } catch (IllegalStateException e) {
            LOG.warn("Unable to look up caller principal (no security context available)", e);
        }
        return null;            
    }
}
