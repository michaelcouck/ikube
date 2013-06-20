package ikube.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;

/**
 * @author Michael Couck
 * @since 20.06.13
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableTweets extends Indexable<IndexableTweets> {

	@Column
	@NotNull
	@Attribute(field = false, description = "This is the consumer key for the OAuth security")
	private String consumerKey;
	@Column
	@NotNull
	@Attribute(field = false, description = "This is the consumer secret key for the OAuth security")
	private String consumerSecret;
	@Column
	@NotNull
	@Attribute(field = false, description = "This is the token for the OAuth security")
	private String token;
	@Column
	@NotNull
	@Attribute(field = false, description = "This is the token secret for the OAuth security")
	private String tokenSecret;

	@Column
	@Attribute(field = false, description = "This is the text field name of the tweet in the Lucene index")
	private String textField;
	@Column
	@Attribute(field = false, description = "This is the date created field name of the tweet in the Lucene index")
	private String createdAtField;
	@Column
	@Attribute(field = false, description = "This is the user namd field name of the tweet in the Lucene index")
	private String fromUserField;
	@Column
	@Attribute(field = false, description = "This is the location/address field name of the tweet in the Lucene index")
	private String locationField;

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getTextField() {
		return textField;
	}

	public void setTextField(String textField) {
		this.textField = textField;
	}

	public String getCreatedAtField() {
		return createdAtField;
	}

	public void setCreatedAtField(String createdAtField) {
		this.createdAtField = createdAtField;
	}

	public String getFromUserField() {
		return fromUserField;
	}

	public void setFromUserField(String fromUserField) {
		this.fromUserField = fromUserField;
	}

	public String getLocationField() {
		return locationField;
	}

	public void setLocationField(String locationField) {
		this.locationField = locationField;
	}

}
