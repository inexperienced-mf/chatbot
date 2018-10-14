package chatbot;

import org.junit.Assert;
import org.junit.Test;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotTests {
    private String uId_1 = "1";
    private QuestionsLoader questionsLoader = new QuestionsLoader();
    private ArrayList<Question> questions = questionsLoader.loadQuestions();

    @Test
    public void testCreate(){
        new Bot("1", questions);
    }

    @Test
    public void testUnstartedIgnores() {
        Bot bot_1 = new Bot(uId_1, questions);
        for (String content : Arrays.asList("", "/help")) {
            Message response = bot_1.respondTo(new Message(uId_1, content, MessageType.UserMessage));
            Assert.assertEquals(MessageType.NotStartedMessage, response.messageType);
        }
    }

    @Test
    public void testStarts() {
        Bot bot_1 = new Bot(uId_1, questions);
        Message response = bot_1.respondTo(new Message(uId_1, "/start", MessageType.UserMessage));
        Assert.assertEquals(MessageType.GreetMessage, response.messageType);
    }

    private Bot createStartedBot(String uId) {
        Bot bot = new Bot(uId, questions);
        bot.respondTo(new Message(uId, "/start", MessageType.UserMessage));
        return bot;
    }

    @Test
    public void testHelps() {
        Bot bot_1 = createStartedBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.UserMessage));
        Assert.assertEquals(MessageType.HelpMessage, response.messageType);
    }

    @Test
    public void testWrongAnswer() {
        Bot bot_1 = createStartedBot(uId_1);
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot_1.respondTo(new Message(uId_1, "", MessageType.UserMessage));
        Assert.assertEquals(MessageType.WrongAnswerMessage, response.messageType);
    }

    @Test
    public void testStartGame() {
        Bot bot_1 = createStartedBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Assert.assertEquals(MessageType.QuestionMessage, response.messageType);
    }

    @Test
    public void testHelpsWhileGame() {
        Bot bot_1 = createStartedBot(uId_1);
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.UserMessage));
        Assert.assertEquals(MessageType.HelpMessage, response.messageType);
    }

    @Test
    public void testNotUnderstanding() {
        Bot bot_1 = createStartedBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "alala", MessageType.UserMessage));
        Assert.assertEquals(MessageType.IncorrectCommandMessage, response.messageType);
    }

    @Test
    public void testShowsResults() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("?", "2"));
        Bot bot = new Bot(uId_1, questions);
        bot.respondTo(new Message(uId_1, "/start", MessageType.UserMessage));
        bot.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot.respondTo(new Message(uId_1, "2", MessageType.UserMessage));
        Assert.assertEquals(MessageType.ResultsMessage, response.messageType);
    }

    @Test
    public void testAcceptsRightAnswer() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("1?", "2"));
        questions.add(new Question("2?", "2"));
        Bot bot = new Bot(uId_1, questions);
        bot.respondTo(new Message(uId_1, "/start", MessageType.UserMessage));
        bot.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot.respondTo(new Message(uId_1, "2", MessageType.UserMessage));
        Assert.assertEquals(MessageType.QuestionMessage, response.messageType);
    }
}