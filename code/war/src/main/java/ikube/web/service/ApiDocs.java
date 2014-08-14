package ikube.web.service;

import com.google.common.collect.Lists;
import ikube.IConstants;
import ikube.model.Api;
import ikube.model.ApiMethod;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static ikube.toolkit.ObjectToolkit.populateFields;

/**
 * This rest web service exposes the web services that are annotated with the {@link ikube.web.service.Api}
 * annotation. This annotation contains the types of parameters for the web service, the type of operation, GET for example,
 * and what type of data it produces, including an example of that object/data.
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
@ikube.web.service.Api(description = "This resource is the rest service that provides the description of the other rest resources")
public class ApiDocs extends Resource {

    public static final String API = "/api";
    public static final String APIS = "/apis";

    /**
     * This method returns all the api objects that have been scanned, as a collection. The collection, as
     * a Json representation, can then be displayed on a web page as living documentation of the exposed apis
     * of the system.
     *
     * @return the Json representation of the collection of {@link ikube.web.service.Api}s describing the rest services
     */
    @GET
    @Path(APIS)
    @ikube.web.service.Api(description = "This method will return all the apis in the system as a collection, Jsonified")
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

    /**
     * Similar to the above, but returning only one api, based on the name in the parameter list.
     *
     * @param apiName the name of the api to get
     * @return the Json representation of the {@link ikube.web.service.Api} describing the rest service
     */
    @GET
    @ikube.web.service.Api(description = "This method will return one api in the system, Jsonified, specified by the parameter name")
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
        ikube.web.service.Api apiAnnotation = resource.getAnnotation(ikube.web.service.Api.class);

        Path pathAnnotation = resource.getAnnotation(Path.class);
        Consumes consumesAnnotation = resource.getAnnotation(Consumes.class);
        Produces producesAnnotation = resource.getAnnotation(Produces.class);

        String basePath = pathAnnotation != null ? pathAnnotation.value() : "";
        String consumesType = consumesAnnotation != null ? Arrays.deepToString(consumesAnnotation.value()) : "";
        String producesType = producesAnnotation != null ? Arrays.deepToString(producesAnnotation.value()) : "";

        Api api = new Api();
        api.setApi(resource.getName());
        api.setDescription(apiAnnotation.description());
        // Only public methods
        Method[] methods = resource.getDeclaredMethods();
        for (final Method method : methods) {
            if (!method.isAnnotationPresent(ikube.web.service.Api.class)) {
                continue;
            }
            ApiMethod apiMethod = new ApiMethod();
            ikube.web.service.Api apiMethodAnnotation = method.getAnnotation(ikube.web.service.Api.class);
            // Set the path for this method
            setMethodPath(apiMethod, method, basePath, apiMethodAnnotation);
            // The type of the method, Get, Post, etc.
            setMethodType(apiMethod, method, apiMethodAnnotation);

            // The description must be declared in the annotation of course
            apiMethod.setDescription(apiMethodAnnotation.description());
            // Set what the methods consume as types and produce as types
            setConsumesAndProducesTypes(apiMethod, method, consumesType, producesType);
            try {
                setConsumes(apiMethod, method, apiMethodAnnotation);
                setProduces(apiMethod, method, apiMethodAnnotation);
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            api.getApiMethods().add(apiMethod);
        }
        return api;
    }

    private void setMethodPath(final ApiMethod apiMethod, final Method method, final String basePath, final ikube.web.service.Api apiMethodAnnotation) {
        // If the uri annotation is set then this overrides the construction of the uri
        if (StringUtils.isNotEmpty(apiMethodAnnotation.uri())) {
            apiMethod.setUri(apiMethodAnnotation.uri());
        } else {
            if (!method.isAnnotationPresent(Path.class)) {
                apiMethod.setUri(basePath);
            } else {
                apiMethod.setUri(basePath + method.getAnnotation(Path.class).value());
            }
        }
    }

    private void setMethodType(final ApiMethod apiMethod, final Method method, final ikube.web.service.Api apiMethodAnnotation) {
        if (StringUtils.isNotEmpty(apiMethodAnnotation.type())) {
            apiMethod.setMethod(apiMethodAnnotation.type());
        } else {
            Annotation[] methodAnnotations = method.getDeclaredAnnotations();
            if (methodAnnotations != null) {
                for (final Annotation methodAnnotation : methodAnnotations) {
                    if (methodAnnotation.getClass().getPackage().getName().contains(GET.class.getPackage().getName())) {
                        apiMethod.setMethod(methodAnnotation.toString());
                    }
                }
            }
        }
    }

    private void setConsumesAndProducesTypes(final ApiMethod apiMethod, final Method method, final String consumesType, final String producesType) {
        // And now for the consumes and produces
        if (!method.isAnnotationPresent(Consumes.class)) {
            apiMethod.setConsumesType(consumesType);
        } else {
            apiMethod.setConsumesType(Arrays.deepToString(method.getAnnotation(Consumes.class).value()));
        }
        if (!method.isAnnotationPresent(Produces.class)) {
            apiMethod.setProducesType(producesType);
        } else {
            apiMethod.setProducesType(Arrays.deepToString(method.getAnnotation(Produces.class).value()));
        }
    }

    private void setConsumes(final ApiMethod apiMethod, final Method method, final ikube.web.service.Api apiMethodAnnotation)
            throws IllegalAccessException, InstantiationException {
        if (apiMethodAnnotation.consumes() != null) {
            if (apiMethodAnnotation.consumes().isArray() ||
                    !Modifier.isPublic(apiMethodAnnotation.consumes().getModifiers()) ||
                    Void.class.isAssignableFrom(apiMethodAnnotation.consumes())) {
                apiMethod.setConsumes(apiMethodAnnotation.consumes().toString());
            } else {
                apiMethod.setConsumes(populateFields(apiMethodAnnotation.consumes().newInstance(), true, 10));
            }
        } else {
            // Here we build the consumes and produces from the parameters and return value
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                parameters[i] = populateFields(parameterType.newInstance(), true, 10);
            }
            apiMethod.setConsumes(parameters);
        }
    }

    private void setProduces(final ApiMethod apiMethod, final Method method, final ikube.web.service.Api apiMethodAnnotation)
            throws IllegalAccessException, InstantiationException {
        if (apiMethodAnnotation.produces() != null) {
            if (apiMethodAnnotation.produces().isArray() ||
                    !Modifier.isPublic(apiMethodAnnotation.produces().getModifiers())
                    || Void.class.isAssignableFrom(apiMethodAnnotation.consumes())) {
                apiMethod.setProduces(apiMethodAnnotation.produces().toString());
            } else {
                apiMethod.setProduces(populateFields(apiMethodAnnotation.produces().newInstance(), true, 10));
            }
        } else {
            // Here we build the response somehow, it looks like the only way is to specify it in the annotation
            // if the response is not a type but a {@link Response} object, with no typing etc.
            Class<?> returnType = method.getReturnType();
            if (!Void.class.isAssignableFrom(returnType)) {
                apiMethod.setProduces(populateFields(returnType.newInstance(), true, 10));
            }
        }
    }

}