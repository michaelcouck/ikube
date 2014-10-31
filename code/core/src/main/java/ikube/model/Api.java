package ikube.model;

import com.google.common.collect.Lists;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Api extends Persistable {

    /**
     * The name of the resource, i.e. the actual class.
     */
    private String api;
    /**
     * The description of this api.
     */
    private String description;
    /**
     * The methods that are exposed as web service methods.
     */
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Collection<ApiMethod> apiMethods = Lists.newArrayList();

    public String getApi() {
        return api;
    }

    public void setApi(final String api) {
        this.api = api;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Collection<ApiMethod> getApiMethods() {
        return apiMethods;
    }

    public void setApiMethods(final Collection<ApiMethod> apiMethods) {
        this.apiMethods = apiMethods;
    }
}