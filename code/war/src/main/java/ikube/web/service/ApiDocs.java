package ikube.web.service;

import com.google.common.collect.Lists;
import ikube.IConstants;
import ikube.model.Api;
import ikube.model.ApiMethod;
import org.reflections.Reflections;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static ikube.toolkit.ObjectToolkit.populateFields;

/**
 * TODO: Document me...
 *
 * @author Michael couck
 * @version 01.00
 * @since 11-07-2014
 */
@Component
@Path(ApiDocs.API)
@Scope(ApiDocs.REQUEST)
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class ApiDocs extends Resource {

    public static final String API = "/api";
    public static final String APIS = "/apis";

    @GET
    @Path(APIS)
    public Response apis() {
        Collection<Api> apis = Lists.newArrayList();
        String packageName = this.getClass().getPackage().getName();
        Set<Class<?>> resources = new Reflections(packageName).getTypesAnnotatedWith(ikube.web.service.Api.class);
        for (final Class<?> resource : resources) {
            Api api = getApi(resource);
            apis.add(api);
        }
        return buildJsonResponse(apis);
    }

    @GET
    public Response api(@QueryParam(value = IConstants.NAME) final String apiName) {
        try {
            Class<?> resource = Class.forName(apiName);
            Api api = getApi(resource);
            return buildJsonResponse(api);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Api getApi(final Class<?> resource) {
        Api api = new Api();
        api.setApi(resource.getName());
        Method[] methods = resource.getDeclaredMethods();
        for (final Method method : methods) {
            if (!method.isAnnotationPresent(ikube.web.service.Api.class)) {
                continue;
            }
            ApiMethod apiMethod = new ApiMethod();
            ikube.web.service.Api annotation = method.getAnnotation(ikube.web.service.Api.class);
            apiMethod.setType(annotation.type());
            apiMethod.setUri(annotation.uri());
            apiMethod.setDescription(annotation.description());
            try {
                if (annotation.consumes().isArray()) {
                    apiMethod.setConsumes(annotation.consumes().toString());
                } else {
                    apiMethod.setConsumes(populateFields(annotation.consumes().newInstance(), true, 10));
                }
                if (annotation.produces().isArray()) {
                    apiMethod.setProduces(annotation.produces().toString());
                } else {
                    apiMethod.setProduces(populateFields(annotation.produces().newInstance(), true, 10));
                }
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            api.getApiMethods().add(apiMethod);
        }
        return api;
    }

}