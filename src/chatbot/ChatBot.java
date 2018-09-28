package chatbot;

import java.util.Scanner;

public class ChatBot {

	public static void main(String[] args) {
		MultiUserBot multiUserBot = new MultiUserBot();
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String check;
			if(scanner.hasNext()) {
				check = scanner.nextLine();
				String[] lines = check.split(":");
				if (lines.length < 2)
					System.out.printf("||DUDE AUTHORIZE|| \n" );
				else {
					Message response = multiUserBot.respondTo(new Message(lines[0], lines[1]));
					System.out.printf("Bot: %s \n", response.content );
				}
			}
		}
	}
}
