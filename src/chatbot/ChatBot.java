package chatbot;

import java.util.ArrayList;
import java.util.Scanner;

public class ChatBot {

	public static void main(String[] args) {
		QuestionsLoader questionsLoader = new QuestionsLoader();
		ArrayList<Question> questions = questionsLoader.loadQuestions();
		MultiUserBot multiUserBot = new MultiUserBot(questions);

		Scanner scanner = new Scanner(System.in);
		while (true) {
			String check;
			if(scanner.hasNext()) {
				check = scanner.nextLine();
				String[] lines = check.split(":");
				if (lines.length < 2)
					System.out.printf("||DUDE AUTHORIZE|| \n" );
				else {
					Message response = multiUserBot.respondTo(new Message(lines[0], lines[1], MessageType.UserMessage));
					System.out.printf("Bot: %s \n", response.content );
				}
			}
		}
	}
}
