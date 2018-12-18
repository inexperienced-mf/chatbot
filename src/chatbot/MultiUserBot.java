package chatbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.synchronizedList;

public class MultiUserBot {
	private final Map<String, Bot> bots;
	private List<Question> questions;
	private final List<AppealRequest> requests;
	private String adminPassword;

	public MultiUserBot(ArrayList<Question> questions, String adminPassword) {
		this.adminPassword = adminPassword;
		bots = new ConcurrentHashMap<>();
		this.questions = synchronizedList(questions);
		this.requests = synchronizedList(new ArrayList<>());
	}

	Message respondTo(Message m) {
		Bot handler = bots.computeIfAbsent(m.userId, (userId) -> new Bot(userId, questions, requests, adminPassword) );
		return handler.respondTo(m);
	}
}