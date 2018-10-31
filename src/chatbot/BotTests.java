package chatbot;

import org.junit.Assert;
import org.junit.Test;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotTests {
    private String uId_1 = "1";

    @Test
    public void testCreate(){
        new Bot("1", new ArrayList<>(), new ArrayList<>());
    }

    @Test
    public void testNotStartedIgnores() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>(), new ArrayList<>());
        for (String content : Arrays.asList("", "/help")) {
            Message response = bot_1.respondTo(new Message(uId_1, content, MessageType.User));
            Assert.assertEquals(MessageType.NotStarted, response.messageType);
        }
    }

    @Test
    public void testStarts() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>(), new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "/start", MessageType.User));
        Assert.assertEquals(MessageType.Greet, response.messageType);
    }

    private Bot createStartedBot(String uId, List<Question> questions, List<AppealRequest> requests ) {
        Bot bot = new Bot(uId, questions, requests);
        bot.respondTo(new Message(uId, "/start", MessageType.User));
        return bot;
    }

    @Test
    public void testHelps() {
        Bot bot_1 = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.User));
        Assert.assertEquals(MessageType.Help, response.messageType);
    }

    @Test
    public void testStartGame() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList(""), new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Assert.assertEquals(MessageType.Question, response.messageType);
    }

    @Test
    public void testWrongAnswer() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList("A"), new ArrayList<>());
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot_1.respondTo(new Message(uId_1, "B", MessageType.User));
        Assert.assertEquals(MessageType.WrongAnswer, response.messageType);
    }

    private List<Question> getOneQuestionList(String rightAnswer) {
        return Arrays.asList(new Question("", rightAnswer));
    }

    @Test
    public void testHelpsDuringGame() {
        Bot bot_1 = createStartedBot(uId_1, getOneQuestionList(""), new ArrayList<>());
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.User));
        Assert.assertEquals(MessageType.Help, response.messageType);
    }

    @Test
    public void testNotUnderstanding() {
        Bot bot_1 = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        Message response = bot_1.respondTo(new Message(uId_1, "lalala", MessageType.User));
        Assert.assertEquals(MessageType.IncorrectCommand, response.messageType);
    }

    @Test
    public void testShowsResults() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("?", "2"));
        Bot bot = new Bot(uId_1, questions, new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/start", MessageType.User));
        bot.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "2", MessageType.User));
        Assert.assertEquals(MessageType.Results, response.messageType);
    }

    @Test
    public void testAcceptsRightAnswer() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("1?", "2"));
        questions.add(new Question("2?", "2"));
        Bot bot = new Bot(uId_1, questions, new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/start", MessageType.User));
        bot.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "2", MessageType.User));
        Assert.assertEquals(MessageType.Question, response.messageType);
    }

    @Test
    public void testYieldsAnswerOptions() {
        String[] answerOptions = new String[] {"A", "B", "C"};
        Bot bot = createStartedBot(uId_1, Arrays.asList(
                new Question("", answerOptions, 2)
        ), new ArrayList<>());
        Message question = bot.respondTo(new Message(uId_1, "/play", MessageType.User));
        Assert.assertTrue(question.hasResponseOptions());
        Assert.assertArrayEquals(answerOptions, question.responseOptions.toArray());
    }

    @Test
    public void testAsksForPassword() {
        Bot bot = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        Message response = bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Assert.assertEquals(MessageType.PasswordRequest, response.messageType);
    }

    @Test
    public void testGivesAdminAccessStarted() {
        Bot bot = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Assert.assertEquals(MessageType.AdminModeEnabled, response.messageType);
    }

    @Test
    public void testNotGivesAdminAccess() {
        Bot bot = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "chebupely", MessageType.User));
        Assert.assertEquals(MessageType.AdminModeNotEnabled, response.messageType);
    }

    @Test
    public void testExitsFromAdminMode() {
        Bot bot = createStartedBot(uId_1, new ArrayList<>(), new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/user", MessageType.User));
        Assert.assertEquals(MessageType.AdminModeDisabled, response.messageType);
    }

    private Bot createWaitingAnswerBot(String uId, List<AppealRequest> requests) {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("1+1=?", "2"));
        Bot bot = createStartedBot(uId, questions, requests);
        bot.respondTo(new Message(uId, "/play", MessageType.User));
        return bot;
    }

    @Test
    public void testGivesAdminAccessPlayed() {
        Bot bot = createWaitingAnswerBot(uId_1, new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Assert.assertEquals(MessageType.AdminModeEnabled, response.messageType);
    }

    @Test
    public void testGivesUserAccessPlayed() {
        Bot bot = createWaitingAnswerBot(uId_1, new ArrayList<>());
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.AppealContentNeeded, response.messageType);
    }

    @Test
    public void testCreatesAppealRequest() {
        Bot bot = createWaitingAnswerBot(uId_1, new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "", MessageType.User));
        Assert.assertEquals(MessageType.AppealRequestAdded, response.messageType);
    }

    @Test
    public void testWorksWhenZeroAppealRequests() {
        Bot bot = createWaitingAnswerBot(uId_1, new ArrayList<>());
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.NoAppealRequestLeft, response.messageType);
    }

    @Test
    public void testWorksWhenNotZeroAppealRequests() {
        List<AppealRequest> requests = new ArrayList<>();
        requests.add(new AppealRequest(uId_1, new Question("1+1=?", "2"), ""));
        Bot bot = createWaitingAnswerBot(uId_1, requests);
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.AppealDecisionRequest, response.messageType);
    }

    @Test
    public void testAnswersOnAppeal() {
        List<AppealRequest> requests = new ArrayList<>();
        requests.add(new AppealRequest(uId_1, new Question("1+1=?", "2"), ""));
        Bot bot = createWaitingAnswerBot(uId_1, requests);
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "", MessageType.User));
        Assert.assertEquals(MessageType.AppealAnswer, response.messageType);
    }
}