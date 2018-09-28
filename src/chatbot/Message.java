package chatbot;

import java.util.Date;

public class Message {
	final String userId;
	final long time;
	final String content;
	private static Date timer = new Date();

    public Message(String id, String newContent) {
    	userId = id;
    	this.time = timer.getTime();
    	content = newContent;
  }
}
