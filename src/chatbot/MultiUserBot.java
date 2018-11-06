package chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiUserBot {
	private Map<String, Bot> bots;
	private List<Question> questions;
	private List<AppealRequest> requests;
	private String adminPassword;

	public MultiUserBot(ArrayList<Question> questions, String adminPassword) {
		this.adminPassword = adminPassword;
		bots = new HashMap<>();
		this.questions = questions;
		this.requests = new ArrayList<>();
	}

	Message respondTo(Message m) {
		if (!bots.containsKey(m.userId))
			bots.put(m.userId, new Bot(m.userId, questions, (ArrayList<AppealRequest>) requests, adminPassword));
		Bot handler = bots.get(m.userId);
		return handler.respondTo(m);
	}
}