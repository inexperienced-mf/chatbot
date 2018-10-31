package chatbot;

import java.util.*;
import java.util.concurrent.Callable;

class Bot {
    private String userId;
    private BotState state;
    private Map<BotState, Map<String, Callable<Message>>> behaviour;
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
    private static final String PASSWORD_REQUEST_TEXT = "Введите пароль";
    private String adminPassword;
    private BotState previousState;
    private ArrayList<AppealRequest> appealRequests;
    private Question lastQuestion;
    private String appealUserId;


    public Bot(String id, List<Question> questions, List<AppealRequest> appealRequests){
		userId = id;
        state = BotState.NotWorking;
        this.questions = new ArrayList<>(questions);
        this.appealRequests = new ArrayList<>(appealRequests);
        random = new Random();

        try {
            adminPassword = System.getenv("admin_password");
            if (adminPassword == null)
                throw new EnvVarNotFoundException("Переменная среды admin_password не определена.");
        } catch (EnvVarNotFoundException e) {
            e.printStackTrace();
        }

        behaviour = new HashMap<>();

        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/start", this::getGreetText);
        behaviour.put(BotState.NotWorking, inner);

        inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        inner.put("/play", this::startNewGame);
        inner.put("/admin", this::switchToAdminMode);
        behaviour.put(BotState.Ready, inner);

        inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        inner.put("/admin", this::switchToAdminMode);
        inner.put("/appeal", this::switchToUserAppealMode);
        behaviour.put(BotState.WaitAnswer, inner);

        inner = new HashMap<>();
        behaviour.put(BotState.WaitAppealRequest, inner);

        inner = new HashMap<>();
        behaviour.put(BotState.WaitAppealDecision, inner);

        inner = new HashMap<>();
        behaviour.put(BotState.WaitPassword, inner);

        inner = new HashMap<>();
        inner.put("/appeal", this::checkAppealRequestList);
        inner.put("/user", this::switchFromAdminMode);
        behaviour.put(BotState.Admin, inner);

        inner = new HashMap<>();
        inner.put("/next", this::checkAppealRequestList);
        behaviour.put(BotState.AppealAdmin, inner);
    }

    public Message respondTo(Message m) {
        Message response = null;

        Map<String, Callable<Message>> inner = behaviour.get(state);
        try {
            if (state == BotState.NotWorking)
                response = inner.getOrDefault(m.content, this::getWrongStartText).call();
            else if (state == BotState.WaitAnswer)
                response = inner.getOrDefault(m.content, () -> askQuestion(m.content)).call();
            else if (state == BotState.WaitAppealRequest)
                response = inner.getOrDefault(m.content, () -> createAppealRequest(m.content)).call();
            else if (state == BotState.WaitAppealDecision)
                response = inner.getOrDefault(m.content, () -> answerAppealRequest(m.content)).call();
            else if (state == BotState.WaitPassword)
                response = inner.getOrDefault(m.content, () -> checkPassword(m.content)).call();
            else
                response = inner.getOrDefault(m.content, this::getDefaultText).call(); //ready, admin, appealAdmin
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

        return response;
	}

	private Message switchToAdminMode() {
        previousState = state;
        state = BotState.WaitPassword;
        return new Message(userId, PASSWORD_REQUEST_TEXT, MessageType.PasswordRequest);
    }

    private Message switchFromAdminMode() {
        state = previousState;
        if (state == BotState.WaitAnswer)
            return new Message(userId, "Вы в пользовательском режиме. Следующий вопрос:" + "\n\n"
                    + currentQuestion.text, MessageType.AdminModeDisabled, currentQuestion.options);
        return new Message(userId, "Вы в пользовательском режиме.", MessageType.AdminModeDisabled);
    }

    private Message checkPassword(String content) {
        if (content.equals(adminPassword)) {
            state = BotState.Admin;
            return new Message(userId, "U're admin!", MessageType.AdminModeEnabled);
        }
        else
            state = previousState;
            return new Message(userId,"Wrong password", MessageType.AdminModeNotEnabled);
    }

    private Message checkAppealRequestList() {
        if (appealRequests.size() == 0) {
            state = BotState.Admin;
                return new Message(userId, "Сейчас нет запросов на аппеляцию",
                    MessageType.NoAppealRequestLeft);
        }
        AppealRequest request = appealRequests.remove(0);
        appealUserId = request.userId;
        state = BotState.WaitAppealDecision;
        return new Message(userId, makeStringFromRequest(request), MessageType.AppealDecisionRequest);
    }

    private Message answerAppealRequest(String content) {
        state = BotState.AppealAdmin;
        return new Message(appealUserId, "Ответ администратора: " + content, MessageType.AppealAnswer);
    }

    private String makeStringFromRequest(AppealRequest appealRequest) {
        String listString = "";

        if (appealRequest.question.options != null) {
            listString += "\nВарианты ответа: ";

            for (String s : appealRequest.question.options)
                listString += s + "\t";
        }

        return String.format("Вопрос: %s %s \n Правильный ответ %s \nВариант пользователя: %s",
                appealRequest.question.text, listString, appealRequest.question.rightAnswer,
                appealRequest.userAnswer);
    }

    private Message switchToUserAppealMode() {
        previousState = state;
        state = BotState.WaitAppealRequest;
        return new Message(userId, "Введите верный, по вашему мнению, ответ",
                MessageType.AppealContentNeeded);
    }

    private Message createAppealRequest(String content) {
        appealRequests.add(new AppealRequest(userId, lastQuestion, content));
        state = previousState;
        if (state == BotState.WaitAnswer)
            return new Message(userId, " Ваш запрос будет обработан администратором. Следующий вопрос:" + "\n\n"
                + currentQuestion.text, MessageType.AppealRequestAdded, currentQuestion.options);
        else
            return new Message(userId," Ваш запрос будет обработан администратором.",
                    MessageType.AppealRequestAdded);
    }

    public void updateRequest(List<AppealRequest> appealRequests) {
        appealRequests.forEach(request -> {if (!this.appealRequests.contains(request)) this.appealRequests.add(request);});
    }


    public List<AppealRequest> getRequests() {
        return appealRequests;
    }

	private Message getHelpText() {
        return new Message(userId, HELP_TEXT, MessageType.Help);
    }

    private Message getGreetText() {
        state = BotState.Ready;
        return new Message(userId, GREET_TEXT, MessageType.Greet);
    }

    private Message getWrongStartText(){
        return new Message(userId, WRONG_START_TEXT, MessageType.NotStarted);
    }

    private Message startNewGame()
    {
        questionsLeft = questions.size();
        score = 0;
        chooseQuestion();
        state = BotState.WaitAnswer;
        return new Message(userId, currentQuestion.text, MessageType.Question, currentQuestion.options);
    }

    private Message getDefaultText(){
        return new Message(userId, DEFAULT_TEXT, MessageType.IncorrectCommand);
    }

    private Message askQuestion(String content){
        if (isCorrect(content)) {
            score++;
            questionsLeft--;
            if (questionsLeft == 0) {
                state = BotState.Ready;
                return new Message(userId, String.format(
                        "Верно! Ура, викторина наконец-то закончилась! Ты набрал %d очков", score ),
                        MessageType.Results);
            }
            chooseQuestion();
            lastQuestion = currentQuestion;
            return new Message(userId, String.format("Верно! Следующий вопрос: \n %s",
                    currentQuestion.text), MessageType.Question, currentQuestion.options);
        }
        score--;
        return new Message(userId,
                "Неправильно :с . Ты теряешь 1 очко. Ещё варианты?",
                MessageType.WrongAnswer, currentQuestion.options);

    }

    private void chooseQuestion() {
        int index = random.nextInt(questions.size());
        this.currentQuestion = questions.get(index);
        questions.remove(index);
    }

    private boolean isCorrect(String answer) { return answer.equals(currentQuestion.rightAnswer); }
}