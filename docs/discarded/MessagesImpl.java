package org.krams.tutorial.client;

/**
 * Interface to represent the messages contained in resource bundle:
 * 	/home/mark/workspace_eclipse/spring-gwt-integration/src/main/resources/org/krams/tutorial/client/MessagesImpl.properties'.
 */
public interface MessagesImpl extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "Enter your name".
   * 
   * @return translated "Enter your name"
   */
  @DefaultMessage("Enter your name")
  @Key("nameField")
  String nameField();

  /**
   * Translated "Send".
   * 
   * @return translated "Send"
   */
  @DefaultMessage("Send")
  @Key("sendButton")
  String sendButton();
}
