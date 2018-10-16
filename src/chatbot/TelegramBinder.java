package chatbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

public class TelegramBinder extends TelegramLongPollingBot {

    private MultiUserBot multiUserBot;

    TelegramBinder(ArrayList<Question> questions) {
        super();
        multiUserBot = new MultiUserBot(questions);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message m = update.getMessage();
        String text = m.hasText() ? m.getText() : "";
        chatbot.Message abstractMsg = new chatbot.Message(
                String.valueOf(m.getChatId()),
                text,
                MessageType.UserMessage
                );
        chatbot.Message reply = multiUserBot.respondTo(abstractMsg);
        SendMessage sendMsg = new SendMessage();
        sendMsg.setChatId(m.getChatId());
        sendMsg.setText(reply.content);
        try {
            execute(sendMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public String getBotToken() {
        return null;
    }
}
