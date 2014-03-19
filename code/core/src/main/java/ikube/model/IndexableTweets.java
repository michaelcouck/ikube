package ikube.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.06.13
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.JOINED)
public class IndexableTweets extends Indexable {

    @Transient
    private transient Coordinate coordinate;

    /**
     * Login details for the stream, mine obviously.
     */
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

    /**
     * Fields in the index.
     */
    @Column
    @Attribute(field = false, description = "This is the text field name of the tweet in the Lucene index")
    private String textField;
    @Column
    @Attribute(field = false, description = "This is the date created field name of the tweet in the Lucene index")
    private String createdAtField;
    @Column
    @Attribute(field = false, description = "This is the user name field name of the tweet in the Lucene index")
    private String fromUserField;
    @Column
    @Attribute(field = false, description = "This is the location/address field name of the tweet in the Lucene index when tweeted, could be different from the users actual location when registering")
    private String locationField;

    /**
     * Details of the user, try to place them on the map.
     */
    @Column
    @Attribute(field = false, description = "?")
    private String userLocationField;
    @Column
    @Attribute(field = false, description = "?")
    private String userNameField;
    @Column
    @Attribute(field = false, description = "?")
    private String userScreenNameField;
    @Column
    @Attribute(field = false, description = "?")
    private String userTimeZoneField;
    @Column
    @Attribute(field = false, description = "?")
    private String userUtcOffsetField;
    @Column
    @Attribute(field = false, description = "?")
    private String userLanguageField;

    @Column
    @Attribute(field = false, description = "?")
    private boolean persistTweets;

    @Column
    @Attribute(field = false, description = "The number of times to clone the tweets for volume simulation")
    private int clones = 0;

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

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

    public String getUserLocationField() {
        return userLocationField;
    }

    public void setUserLocationField(String userLocationField) {
        this.userLocationField = userLocationField;
    }

    public String getUserNameField() {
        return userNameField;
    }

    public void setUserNameField(String userNameField) {
        this.userNameField = userNameField;
    }

    public String getUserScreenNameField() {
        return userScreenNameField;
    }

    public void setUserScreenNameField(String userScreenNameField) {
        this.userScreenNameField = userScreenNameField;
    }

    public String getUserTimeZoneField() {
        return userTimeZoneField;
    }

    public void setUserTimeZoneField(String userTimeZoneField) {
        this.userTimeZoneField = userTimeZoneField;
    }

    public String getUserUtcOffsetField() {
        return userUtcOffsetField;
    }

    public void setUserUtcOffsetField(String userUtcOffsetField) {
        this.userUtcOffsetField = userUtcOffsetField;
    }

    public boolean isPersistTweets() {
        return persistTweets;
    }

    public void setPersistTweets(boolean persistTweets) {
        this.persistTweets = persistTweets;
    }

    public String getUserLanguageField() {
        return userLanguageField;
    }

    public void setUserLanguageField(String userLanguageField) {
        this.userLanguageField = userLanguageField;
    }

    public int getClones() {
        return clones;
    }

    public void setClones(int clones) {
        this.clones = clones;
    }

}
