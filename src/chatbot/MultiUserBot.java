package chatbot;

import java.util.HashMap;
import java.util.Map;

public class MultiUserBot {
	  private Map<String, Bot> bots;

	  public MultiUserBot() {
		  bots = new HashMap<String,Bot>();
	  }
	  
	  public Message respondTo(Message m) {
		  if (!bots.containsKey(m.userId))
			  bots.put(m.userId, new Bot(m.userId));
		  Bot handler = bots.get(m.userId);
	      return handler.respondTo(m);
	  }
	}