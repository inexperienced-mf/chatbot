package chatbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class TelegramAdapter extends TelegramLongPollingBot {

    private MultiUserBot multiUserBot;
    private final String botToken;

    TelegramAdapter(ArrayList<Question> questions, String token, String adminPassword) {
        super();
        multiUserBot = new MultiUserBot(questions, adminPassword);
        botToken = token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message m = update.getMessage();
        String text = m.hasText() ? m.getText() : "";
        chatbot.Message abstractMsg = new chatbot.Message(
                String.valueOf(m.getChatId()),
                text,
                MessageType.User
        );
        chatbot.Message reply = multiUserBot.respondTo(abstractMsg);
        SendMessage sendMsg = new SendMessage();
        sendMsg.setChatId(m.getChatId());
        sendMsg.setText(reply.content);
        if (reply.hasResponseOptions()) {
            List<String> options = reply.responseOptions;
            ReplyKeyboardMarkup keyboardMarkup = getReplyKeyboardMarkup(options);
            sendMsg.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(sendMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getReplyKeyboardMarkup(List<String> options) {
        List<KeyboardRow> keys = new ArrayList<>();
        for (String option: options) {
            KeyboardRow key = new KeyboardRow();
            key.add(option);
            keys.add(key);
        }
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keys);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return "AwkwardQuizBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
