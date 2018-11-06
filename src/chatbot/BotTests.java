package chatbot;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotTests {
    private String uId_1 = "1";

    @Test
    public void testCreate(){
        new Bot("1", new ArrayList<>(), new ArrayList<>(), "");
    }

    @Test
    public void testNotStartedIgnores() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        for (String content : Arrays.asList("", "/help")) {
            Message response = bot_1.respondTo(new Message(uId_1, content, MessageType.User));
            Assert.assertEquals(MessageType.NotStarted, response.messageType);
        }
    }

    @Test
    public void testStarts() {
        Bot bot_1 = new Bot(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        Message response = bot_1.respondTo(new Message(uId_1, "/start", MessageType.User));
        Assert.assertEquals(MessageType.Greet, response.messageType);
    }

    private Bot createAndStart(String uId, List<Question> questions, List<AppealRequest> requests, String pwd) {
        Bot bot = new Bot(uId, questions, (ArrayList<AppealRequest>) requests, pwd);
        bot.respondTo(new Message(uId, "/start", MessageType.User));
        return bot;
    }

    @Test
    public void testHelps() {
        Bot bot_1 = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.User));
        Assert.assertEquals(MessageType.Help, response.messageType);
    }

    @Test
    public void testStartGame() {
        Bot bot_1 = createAndStart(uId_1, getOneQuestionList(""), new ArrayList<>(), "");
        Message response = bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Assert.assertEquals(MessageType.Question, response.messageType);
    }

    @Test
    public void testWrongAnswer() {
        Bot bot_1 = createAndStart(uId_1, getOneQuestionList("A"), new ArrayList<>(), "");
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot_1.respondTo(new Message(uId_1, "B", MessageType.User));
        Assert.assertEquals(MessageType.WrongAnswer, response.messageType);
    }

    private List<Question> getOneQuestionList(String rightAnswer) {
        return Arrays.asList(new Question("", rightAnswer));
    }

    @Test
    public void testHelpsDuringGame() {
        Bot bot_1 = createAndStart(uId_1, getOneQuestionList(""), new ArrayList<>(), "");
        bot_1.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot_1.respondTo(new Message(uId_1, "/help", MessageType.User));
        Assert.assertEquals(MessageType.Help, response.messageType);
    }

    @Test
    public void testNotUnderstanding() {
        Bot bot_1 = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        Message response = bot_1.respondTo(new Message(uId_1, "lalala", MessageType.User));
        Assert.assertEquals(MessageType.IncorrectCommand, response.messageType);
    }

    @Test
    public void testShowsResults() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("?", "2"));
        Bot bot = createAndStart(uId_1, questions, new ArrayList<>(), "");
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
        Bot bot = createAndStart(uId_1, questions, new ArrayList<>(), "");
        bot.respondTo(new Message(uId_1, "/play", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "2", MessageType.User));
        Assert.assertEquals(MessageType.Question, response.messageType);
    }

    @Test
    public void testYieldsAnswerOptions() {
        String[] answerOptions = new String[] {"A", "B", "C"};
        Bot bot = createAndStart(uId_1, Arrays.asList(
                new Question("", answerOptions, 2)
        ), new ArrayList<>(), "");
        Message question = bot.respondTo(new Message(uId_1, "/play", MessageType.User));
        Assert.assertTrue(question.hasResponseOptions());
        Assert.assertArrayEquals(answerOptions, question.responseOptions.toArray());
    }

    @Test
    public void testAsksForPassword() {
        Bot bot = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        Message response = bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Assert.assertEquals(MessageType.PasswordRequest, response.messageType);
    }

    @Test
    public void testEntersAdminMode() {
        Bot bot = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), "tomato");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Assert.assertEquals(MessageType.AdminAuthSuccess, response.messageType);
    }

    @Test
    public void testChecksPassword() {
        String requiredPassword = "pelmeny";
        String inputPassword = "chebupely";
        Bot bot = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), requiredPassword);
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, inputPassword, MessageType.User));
        Assert.assertEquals(MessageType.AdminAuthFailed, response.messageType);
    }

    @Test
    public void testExitsFromAdminMode() {
        Bot bot = createAndStart(uId_1, new ArrayList<>(), new ArrayList<>(), "");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/user", MessageType.User));
        Assert.assertEquals(MessageType.AdminModeDisabled, response.messageType);
    }

    private Bot createBotThatWaitsForAnswer(String uId, List<AppealRequest> requests, String password) {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("", ""));
        Bot bot = createAndStart(uId, questions, requests, password);
        bot.respondTo(new Message(uId, "/play", MessageType.User));
        return bot;
    }

    @Test
    public void testEntersAdminModeInGame() {
        Bot bot = createBotThatWaitsForAnswer(uId_1, new ArrayList<>(), "tomato");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Assert.assertEquals(MessageType.AdminAuthSuccess, response.messageType);
    }

    @Test
    public void testOffersAppealWhenAsked() {
        Bot bot = createBotThatWaitsForAnswer(uId_1, new ArrayList<>(), "");
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.WriteYourOpinion, response.messageType);
    }

    @Test
    public void testSkipsAppealedQuestion() {
        String question1 = "/one/";
        String question2 = "/two/";
        Bot bot = createAndStart(uId_1, Arrays.asList(
                new Question(question1, ""),
                new Question(question2, "")),
                new ArrayList<>(), "");
        String response = bot.respondTo(new Message(uId_1, "/play", MessageType.User)).content;
        boolean question1IsFirst = false;
        if (response.contains(question1))
            question1IsFirst = true;
        bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        response = bot.respondTo(new Message(uId_1, "", MessageType.User)).content;
        Assert.assertTrue(response.contains(question1IsFirst ? question2 : question1));
    }

    @Test
    public void testCreatesAppealRequest() {
        ArrayList<AppealRequest> requests = new ArrayList<>();
        Bot bot = createBotThatWaitsForAnswer(uId_1, requests, "");
        bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        String userAnswer = "Different.";
        Message response = bot.respondTo(new Message(uId_1, userAnswer, MessageType.User));
//        Assert.assertEquals(MessageType.AppealConsidered, response.messageType);
        Assert.assertEquals(1, requests.size());
        Assert.assertEquals("Different.", requests.get(0).userAnswer);
    }

    @Test
    public void testZeroAppealRequests() {
        Bot bot = createBotThatWaitsForAnswer(uId_1, new ArrayList<>(), "tomato");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.NoAppealsLeft, response.messageType);
    }

    @Test
    public void testYieldsAppealRequests() {
        List<AppealRequest> requests = new ArrayList<>();
        requests.add(new AppealRequest(uId_1, new Question("1+1=?", "2"), ""));
        Bot bot = createBotThatWaitsForAnswer(uId_1, requests, "tomato");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        Message response = bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        Assert.assertEquals(MessageType.AppealDecisionRequest, response.messageType);
    }

    @Test
    public void testAnswersOnAppeal() {
        List<AppealRequest> requests = new ArrayList<>();
        String uId_2 = "2";
        requests.add(new AppealRequest(uId_2, new Question("1+1=?", "2"), ""));
        Bot bot = createBotThatWaitsForAnswer(uId_1, requests, "tomato");
        bot.respondTo(new Message(uId_1, "/admin", MessageType.User));
        bot.respondTo(new Message(uId_1, "tomato", MessageType.User));
        bot.respondTo(new Message(uId_1, "/appeal", MessageType.User));
        String verdict = "My verdict.";
        Message response = bot.respondTo(new Message(uId_1, verdict, MessageType.User));
        Assert.assertEquals(MessageType.AppealAnswer, response.messageType);
        Assert.assertTrue(response.content.contains(verdict));
    }
}