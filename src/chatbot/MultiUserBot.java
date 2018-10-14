package chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiUserBot {
	private Map<String, Bot> bots;
	private List<Question> questions;

	  public MultiUserBot(ArrayList<Question> questions ) {
		  bots = new HashMap<>();
		  this.questions = questions;
	  }
	  
	  public Message respondTo(Message m) {
		  if (!bots.containsKey(m.userId))
			  bots.put(m.userId, new Bot(m.userId, questions));
		  Bot handler = bots.get(m.userId);
	      return handler.respondTo(m);
	  }
	}