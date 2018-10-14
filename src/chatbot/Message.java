package chatbot;

import java.util.Date;

public class Message {
	final String userId;
	final long time;
	final String content;
	final MessageType messageType;
	private static Date timer = new Date();

    public Message(String id, String newContent, MessageType messageType) {
    	userId = id;
    	this.time = timer.getTime();
    	content = newContent;
    	this.messageType = messageType;
  }
}
