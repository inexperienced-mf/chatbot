package chatbot;

import org.junit.Assert;
import org.junit.Test;
import java.lang.String;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotTests {
    private String uId_1 = "1";

    @Test
    public void testCreate(){
        new Bot("1", new ArrayList<>());
    }

    @Test
    public void testNotStartedIgnores() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>());
        for (String content : Arrays.asList("", "/help")) {
            Message response = bot_1.respondTo(new Message(uId_1, content, MessageType.UserMessage));
            Assert.assertEquals(MessageType.NotStartedMessage, response.messageType);
        }
    }

    @Test
    public void testStarts() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "/start", MessageType.UserMessage));
        Assert.assertEquals(MessageType.GreetMessage, response.messageType);
    }

    private Bot createStartedBot(String uId, List<Question> questions) {
        Bot bot = new Bot(uId, questions);
        bot.respondTo(new Message(uId, "/start", MessageType.UserMessage));
        return bot;
    }

    @Test
    public void testHelps() {
        Bot bot_1 = createStartedBot(uId_1, new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.UserMessage));
        Assert.assertEquals(MessageType.HelpMessage, response.messageType);
    }

    @Test
    public void testStartGame() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList(""));
        Message response = bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Assert.assertEquals(MessageType.QuestionMessage, response.messageType);
    }

    @Test
    public void testWrongAnswer() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList("A"));
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot_1.respondTo(new Message(uId_1, "B", MessageType.UserMessage));
        Assert.assertEquals(MessageType.WrongAnswerMessage, response.messageType);
    }

    private List<Question> getOneQuestionList(String rightAnswer) {
        return Arrays.asList(new Question("", rightAnswer));
    }

    @Test
    public void testHelpsDuringGame() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList(""));
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.UserMessage));
        Assert.assertEquals(MessageType.HelpMessage, response.messageType);
    }

    @Test
    public void testNotUnderstanding() {
        Bot bot_1 = createStartedBot(uId_1, new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "lalala", MessageType.UserMessage));
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

    @Test
    public void testYieldsAnswerOptions() {
        String[] answerOptions = new String[] {"A", "B", "C"};
        Bot bot = createStartedBot(uId_1, Arrays.asList(
                new Question("", answerOptions, 2)
        ));
        Message question = bot.respondTo(new Message(uId_1, "/play", MessageType.UserMessage));
        Assert.assertTrue(question.hasResponseOptions());
        Assert.assertArrayEquals(answerOptions, question.responseOptions.toArray());
    }
}