package chatbot;

import java.util.Date;
import java.util.List;

public class Message {
	final String userId;
	final long time;
	final String content;
	final MessageType messageType;
	final List<String> responseOptions;
	private static Date timer = new Date();

    public Message(String userId, String content, MessageType messageType) {
    	this(userId, content, messageType, null);
    }

    public Message(String userId, String content, MessageType messageType, List<String> responseOptions) {
        this.userId = userId;
        this.time = timer.getTime();
        this.content = content;
        this.messageType = messageType;
        this.responseOptions = responseOptions;
    }

	boolean hasResponseOptions() {
    	return responseOptions != null;
	}

}
