package chatbot;

import java.io.File;
import java.io.IOException;
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



    public Bot(String id){
		userId = id;
        state = BotState.NotWorking;
        questions = new ArrayList<Question>();
        random = new Random();

        behaviour = new HashMap<BotState, Map<String, Callable<String>>>();
        HashMap<String, Callable<String>> inner = new HashMap<String, Callable<String>>();
        inner.put("/help", () ->  getHelpText());
        behaviour.put(BotState.WaitAnswer, inner);
        inner.put("/play", () ->  startNewGame());
        behaviour.put(BotState.Ready, inner);
        inner = new HashMap<String, Callable<String>>();
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

        }

        return new Message(userId, response);
	}

	private static String getHelpText()
    {
        return HELP_TEXT;
    }

    private String getGreetText()
    {
        state = BotState.Ready;
        return GREET_TEXT;
    }

    private String getWrongStartText(){
        return WRONG_START_TEXT;
    }

    private String startNewGame()
    {
        questionsLeft = 3;
        score = 0;
        chooseQuestion();
        state = BotState.WaitAnswer;
        return currentQuestion.questionText;
    }

    private String getDefaultText(){
        return DEFAULT_TEXT;
    }

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
        if (questions.isEmpty())
            loadQuestions();
        int index = random.nextInt(questions.size());
        this.currentQuestion = questions.get(index);
        questions.remove(index);
    }

	private void loadQuestions() {
    	String content = null;
		try {
            Scanner reader = new Scanner(new File("questions.txt"));
            content = reader
            		.useDelimiter("\\A")
            		.next();
            reader.close();
            
        } catch (IOException e) {
            System.out.println("не удалось открыть questions.txt ");
        }
               
        questions = new ArrayList<Question>();
		for (String q: content.split("\r\n\r\n"))
			questions.add(makeQuestion(q));
    }

	private Question makeQuestion(String raw) {
		int splitIndex = raw.lastIndexOf('\n') + 1;
		return new Question(raw.substring(0,  splitIndex),
				raw.substring(splitIndex));
	}
    
    private boolean isCorrect(String answer) {
    	return answer.equals(currentQuestion.rightAnswer);
    }

}