package ikube.index.spatial;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 06.03.2011
 * @version 01.00
 */
public interface ISpatialEnrichmentInterceptor {

	Object enrich(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
