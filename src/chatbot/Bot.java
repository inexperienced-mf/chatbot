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
    private ArrayList<Question> questions;
    private int questionsLeft;
    private int score;
    private Map<BotState, Map<String, Callable<Message>>> behaviour;

    public Bot(String id, List<Question> questions){
		userId = id;
        state = BotState.NotWorking;
        this.questions = new ArrayList<>(questions);
        random = new Random();

        behaviour = new HashMap<>();

        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        behaviour.put(BotState.WaitAnswer, inner);

        inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        inner.put("/play", this::startNewGame);
        behaviour.put(BotState.Ready, inner);

        inner = new HashMap<>();
        inner.put("/start", this::getGreetText);
        behaviour.put(BotState.NotWorking, inner);
    }

    public Message respondTo(Message m) {
        Message response = null;

        Map<String, Callable<Message>> inner = behaviour.get(state);
        try {
            if (state == BotState.NotWorking)
                response = inner.getOrDefault(m.content, this::getWrongStartText).call();
            else if (state == BotState.WaitAnswer)
                response = inner.getOrDefault(m.content, () -> askQuestion(m.content)).call();
            else
                response = inner.getOrDefault(m.content, this::getDefaultText).call();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

        return response;
	}

	private Message getHelpText() {
        return new Message(userId, HELP_TEXT, MessageType.HelpMessage);
    }

    private Message getGreetText() {
        state = BotState.Ready;
        return new Message(userId, GREET_TEXT, MessageType.GreetMessage);
    }

    private Message getWrongStartText(){
        return new Message(userId, WRONG_START_TEXT, MessageType.NotStartedMessage);
    }

    private Message startNewGame()
    {
        questionsLeft = questions.size();
        score = 0;
        chooseQuestion();
        state = BotState.WaitAnswer;
        return new Message(userId, currentQuestion.text, MessageType.QuestionMessage, currentQuestion.options);
    }

    private Message getDefaultText(){
        return new Message(userId, DEFAULT_TEXT, MessageType.IncorrectCommandMessage );
    }

    private Message askQuestion(String content){
        if (isCorrect(content)) {
            score++;
            questionsLeft--;
            if (questionsLeft == 0) {
                state = BotState.Ready;
                return new Message(userId, String.format(
                        "Верно! Ура, викторина наконец-то закончилась! Ты набрал %d очков", score ),
                        MessageType.ResultsMessage);
            }
            else {
                chooseQuestion();
                return new Message(userId, String.format("Верно! Следующий вопрос: \n %s",
                        currentQuestion.text), MessageType.QuestionMessage, currentQuestion.options);
            }
        }
        else {
            score--;
            return new Message(userId,
                    "Неправильно :с . Ты теряешь 1 очко. Ещё варианты?",
                    MessageType.WrongAnswerMessage, currentQuestion.options);
        }
    }

    private void chooseQuestion() {
        int index = random.nextInt(questions.size());
        this.currentQuestion = questions.get(index);
        questions.remove(index);
    }

    private boolean isCorrect(String answer) { return answer.equals(currentQuestion.rightAnswer); }
}