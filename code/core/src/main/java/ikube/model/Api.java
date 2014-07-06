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

    private String api;
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Collection<ApiMethod> apiMethods = Lists.newArrayList();

    public String getApi() {
        return api;
    }

    public void setApi(final String api) {
        this.api = api;
    }

    public Collection<ApiMethod> getApiMethods() {
        return apiMethods;
    }

    public void setApiMethods(final Collection<ApiMethod> apiMethods) {
        this.apiMethods = apiMethods;
    }
}