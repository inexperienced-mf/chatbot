package chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiUserBot {
	private Map<String, Bot> bots;
	private List<Question> questions;
	private List<AppealRequest> requests;

	  public MultiUserBot(ArrayList<Question> questions ) {
		  bots = new HashMap<>();
		  this.questions = questions;
		  this.requests = new ArrayList<>();
	  }
	  
	  public Message respondTo(Message m) {
		  if (!bots.containsKey(m.userId))
			  bots.put(m.userId, new Bot(m.userId, questions, requests));
		  Bot handler = bots.get(m.userId);
		  handler.updateRequest(requests);
		  Message response = handler.respondTo(m);
		  updateAppealRequests(handler);
	      return response;
	  }

	  private void updateAppealRequests(Bot bot) {
          requests.forEach(request -> {if (!bot.getRequests().contains(request)) requests.remove(request);});
      }
	}