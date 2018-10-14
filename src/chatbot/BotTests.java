package chatbot;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class BotTests {
    private String uId_1 = "1";
    private String uId_2 = "2";
    private QuestionsLoader questionsLoader = new QuestionsLoader();
    private ArrayList<Question> questions = questionsLoader.loadQuestions();

    private String expectedGreetText = "Привет, дружок-пирожок! " +
            "Мне пока не придумали имени, так что я просто Бот. " +
            "Однако мне придумали назначение: проверять и повышать эрудицию моих собеседников. " +
            "Пиши /play , и мы сыграем с тобой серию вопросов на самые разные темы. " +
            "Пиши /help , чтобы уяснить, как со мной общаться.";
    private String expectedHelpText = "Доступные команды:\n" +
            "/help - увидеть эту справочку\n" +
            "/start - бот начнёт думать о вас и общаться с вами\n" +
            "/stop - бот перестанет думать о вас и общаться с вами\n" +
            "/play - бот сыграет с вами викторину в стиле \"вопрос-ответ\"";

    @Test
    public void testCreate(){
        new Bot("1", questions);
    }

    private String wrongStartText = "Начни с приветственного /start";

    @Test
    public void testUnstartedIgnores() {
        Bot bot_1 = new Bot(uId_1, questions);
        for (String content : Arrays.asList("", "/help")) {
            Message response = bot_1.respondTo(new Message(uId_1, content));
            Assert.assertEquals(wrongStartText, response.content);
        }
    }

    @Test
    public void testStarts() {
        Bot bot_1 = new Bot(uId_1, questions);
        Message response = bot_1.respondTo(new Message(uId_1, "/start"));
        Assert.assertEquals(expectedGreetText, response.content);
    }

    private Bot createStartBot(String uId) {
        Bot bot = new Bot(uId, questions);
        bot.respondTo(new Message(uId, "/start"));
        return bot;
    }

    @Test
    public void testHelps() {
        Bot bot_1 = createStartBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "/help"));
        Assert.assertEquals(expectedHelpText, response.content);
    }

    private String notUnderstandingAnswer;

    private void sayNonsense() {
        Bot bot_1 = createStartBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "qweqweqwe"));
        notUnderstandingAnswer = response.content;
    }


    @Test
    public void testPlayable() {
        Bot bot_1 = createStartBot(uId_1);
        sayNonsense();
        Message response = bot_1.respondTo(new Message(uId_1, "/play"));
        Assert.assertNotEquals(notUnderstandingAnswer, response.content);
        response = bot_1.respondTo(new Message(uId_1, ""));
        Assert.assertNotEquals(notUnderstandingAnswer, response.content);
    }

    @Test
    public void testHelpsWhileGame() {
        Bot bot_1 = createStartBot(uId_1);
        bot_1.respondTo(new Message(uId_1, "/play"));
        Message response = bot_1.respondTo(new Message(uId_1, "/help"));
        Assert.assertEquals(expectedHelpText, response.content);
    }

    private  String expectedDefaultText = "Не совсем ясно. Уточни правила общеня: /help";

    @Test
    public void testNotUnderstanding() {
        Bot bot_1 = createStartBot(uId_1);
        Message response = bot_1.respondTo(new Message(uId_1, "alala"));
        Assert.assertEquals(expectedDefaultText, response.content);
    }
}