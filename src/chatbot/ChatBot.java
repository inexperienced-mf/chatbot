package chatbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

public class ChatBot {
    static {
        ApiContextInitializer.init();
    }

	public static void main(String[] args) {
		QuestionsLoader questionsLoader = new QuestionsLoader();
		ArrayList<Question> questions = questionsLoader.loadQuestions();
		TelegramBinder tgBot = new TelegramBinder(questions);
        TelegramBotsApi botApi = new TelegramBotsApi();
        try {
            botApi.registerBot(tgBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
	}
}
