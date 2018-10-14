package chatbot;

import java.util.*;
import java.util.concurrent.Callable;

class Bot {
    private String userId;
    private BotState state;
    private static final String HELP_TEXT = "Доступные команды:\n" +
            "/help - увидеть эту справочку\n" +
            "/start - бот начнёт думать о вас и общаться с вами\n" +
            "/stop - бот перестанет думать о вас и общаться с вами\n" +
            "/play - бот сыграет с вами викторину в стиле \"вопрос-ответ\"";
    private static final String GREET_TEXT = "Привет, дружок-пирожок! " +
            "Мне пока не придумали имени, так что я просто Бот. " +
            "Однако мне придумали назначение: проверять и повышать эрудицию моих собеседников. " +
            "Пиши /play , и мы сыграем с тобой серию вопросов на самые разные темы. " +
            "Пиши /help , чтобы уяснить, как со мной общаться.";
    private static final String WRONG_START_TEXT = "Начни с приветственного /start";
    private static final String DEFAULT_TEXT = "Не совсем ясно. Уточни правила общеня: /help";
    private Question currentQuestion;
    private Random random;
    private List<Question> questions;
    private int questionsLeft;
    private int score;
    private Map<BotState, Map<String, Callable<String >>> behaviour;



    public Bot(String id, List<Question> questions){
		userId = id;
        state = BotState.NotWorking;
        this.questions = questions;
        random = new Random();

        behaviour = new HashMap<>();
        HashMap<String, Callable<String>> inner = new HashMap<>();
        inner.put("/help", () ->  getHelpText());
        behaviour.put(BotState.WaitAnswer, inner);
        inner.put("/play", () ->  startNewGame());
        behaviour.put(BotState.Ready, inner);
        inner = new HashMap<>();
        inner.put("/start", () ->  getGreetText());
        behaviour.put(BotState.NotWorking, inner);
    }

    public Message respondTo(Message m) {
        String response = null;

        Map<String, Callable<String>> inner = behaviour.get(state);
        try {
            if (state == BotState.NotWorking)
                response = inner.getOrDefault(m.content, () -> getWrongStartText()).call();
            else if (state == BotState.WaitAnswer)
                response = inner.getOrDefault(m.content, () -> askQuestion(m.content)).call();
            else
                response = inner.getOrDefault(m.content, () -> getDefaultText()).call();
        } catch (java.lang.Exception e) {
            System.out.println("боту не удалось выполнить действие");
        }

        return new Message(userId, response);
	}

	private static String getHelpText()
    {
        return HELP_TEXT;
    }

    private String getGreetText() {
        state = BotState.Ready;
        return GREET_TEXT;
    }

    private String getWrongStartText(){ return WRONG_START_TEXT; }

    private String startNewGame()
    {
        questionsLeft = 3;
        score = 0;
        chooseQuestion();
        state = BotState.WaitAnswer;
        return currentQuestion.questionText;
    }

    private String getDefaultText(){ return DEFAULT_TEXT; }

    private String askQuestion(String content){
        if (isCorrect(content)) {
            score++;
            questionsLeft--;
            if (questionsLeft == 0) {
                state = BotState.Ready;
                return String.format(
                        "Верно! Ура, викторина наконец-то закончилась! Ты набрал %d очков", score );
            }
            else {
                chooseQuestion();
                return String.format("Верно! Следующий вопрос: \n %s",
                        currentQuestion.questionText);
            }
        }
        else {
            score--;
            return "Неправильно :с . Ты теряешь 1 очко. Ещё варианты?";
        }
    }

    private void chooseQuestion() {
        int index = random.nextInt(questions.size());
        this.currentQuestion = questions.get(index);
        questions.remove(index);
    }

    private boolean isCorrect(String answer) { return answer.equals(currentQuestion.rightAnswer); }
}