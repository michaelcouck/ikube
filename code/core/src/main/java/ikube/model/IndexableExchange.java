package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@Entity
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableExchange extends Indexable {

    @Column
    @NotNull
    @Pattern(regexp = "^(https?|http?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "The url must be valid")
    @Attribute(field = false, description = "This is the endpoint url for the web service")
    private String url;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the userid to login to the server")
    private String userid;

    @Column
    @Size(min = 1, max = 256)
    @Attribute(field = false, description = "This is the password to login to the server")
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
