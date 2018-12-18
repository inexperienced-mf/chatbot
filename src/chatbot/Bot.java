package chatbot;

import java.util.*;
import java.util.concurrent.Callable;

class Bot {
    private String userId;
    private BotState state;
    private Map<BotState, Map<String, Callable<Message>>> behaviour;
    private static final String HELP_TEXT = "Доступные команды:\n" +
            "/help - увидеть эту справочку\n" +
            "/start - бот начнёт думать о тебе и общаться с тобой\n" +
            "/play - бот сыграет с тобой викторину в стиле \"вопрос-ответ\"\n" +
            "/appeal - вызови в момент играния вопроса, чтобы отправить по нему апелляцию. Твой ответ проверит " +
            "админ, и бот отправит тебе вердикт.\n" +
            "/admin - перейти в режим админа. Будь готов ввести пароль!";
    private static final String GREET_TEXT = "Привет, дружок-пирожок! " +
            "Мне пока не придумали имени, так что я просто Бот. " +
            "Однако мне придумали назначение: проверять и повышать эрудицию моих собеседников. " +
            "Пиши /play , и мы сыграем с тобой серию вопросов на самые разные темы. " +
            "Пиши /help , чтобы уяснить, как со мной общаться.";
    private static final String WRONG_START_TEXT = "Начни с приветственного /start";
    private static final String DEFAULT_TEXT = "Не совсем ясно. Уточни правила общеня: /help";
    private static final String APPEAL_CONSIDERED = "Хорошо. Как только админ рассмотрит твою апелляцию " +
            "по предыдущему вопросу, тебе придёт его вердикт.";
    private static final String ADMIN_HELP = "/appeal - рассмотреть очередную апелляцию или убедиться, что их нет.\n" +
            "/user - вернуться в режим пользователя.\n" +
            "/help - увидеть эту справочку.";

    private Question currentQuestion;
    private Random random;
    private ArrayList<Question> questions;
    private int questionsLeft;
    private int score;
    private int gameLength = 3;
    private String adminPassword;
    private BotState previousState;
    private final List<AppealRequest> appealRequests;
    private AppealRequest currentAppealRequest;
    private String appealUserId;


    public Bot(String id, List<Question> questions, List<AppealRequest> appealRequests, String adminPassword){
        userId = id;
        state = BotState.NotWorking;
        synchronized (questions) {
            this.questions = new ArrayList<>(questions);
        }
        this.appealRequests = appealRequests;
        this.adminPassword = adminPassword;
        random = new Random();

        behaviour = new HashMap<>();
        HashMap<String, Callable<Message>> inner;
        behaviour.put(BotState.NotWorking, fillNotWorkingBehaviour());
        behaviour.put(BotState.Ready, fillReadyBehaviour());
        behaviour.put(BotState.WaitAnswer, fillWaitAnswerBehaviour());
        behaviour.put(BotState.WaitAppealRequest, fillWaitAppealRequestBehaviour());

        inner = new HashMap<>();
        behaviour.put(BotState.WaitsForAppealDecision, inner);

        inner = new HashMap<>();
        behaviour.put(BotState.WaitPassword, inner);

        behaviour.put(BotState.Admin, fillAdminBehaviour());
        behaviour.put(BotState.AppealAdmin, fillAppealAdminBehaviour());
    }

    private HashMap<String, Callable<Message>> fillAppealAdminBehaviour() {
        HashMap<String, Callable<Message>> inner;
        inner = new HashMap<>();
        inner.put("/next", this::tryShowAppealRequest);
        return inner;
    }

    private HashMap<String, Callable<Message>> fillAdminBehaviour() {
        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/appeal", this::tryShowAppealRequest);
        inner.put("/user", this::switchFromAdminMode);
        inner.put("/help", this::getAdminHelpText);
        return inner;
    }

    private HashMap<String, Callable<Message>> fillWaitAppealRequestBehaviour() {
        HashMap<String, Callable<Message>> inner = new HashMap<>();
        return inner;
    }

    private HashMap<String, Callable<Message>> fillWaitAnswerBehaviour() {
        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        inner.put("/admin", this::askForPassword);
        inner.put("/appeal", this::switchToAppealMode);
        return inner;
    }

    private HashMap<String, Callable<Message>> fillNotWorkingBehaviour() {
        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/start", this::getGreetText);
        return inner;
    }

    private HashMap<String, Callable<Message>> fillReadyBehaviour() {
        HashMap<String, Callable<Message>> inner = new HashMap<>();
        inner.put("/help", this::getHelpText);
        inner.put("/play", this::startNewGame);
        inner.put("/admin", this::askForPassword);
        return inner;
    }

    synchronized Message respondTo(Message m) {
        Message response = null;

        Map<String, Callable<Message>> inner = behaviour.get(state);
        try {
            if (state == BotState.NotWorking)
                response = inner.getOrDefault(m.content, this::getWrongStartText).call();
            else if (state == BotState.WaitAnswer)
                response = inner.getOrDefault(m.content, () -> checkAnswer(m.content)).call();
            else if (state == BotState.WaitAppealRequest)
                response = inner.getOrDefault(m.content, () -> createAppealRequest(m.content)).call();
            else if (state == BotState.WaitsForAppealDecision)
                response = inner.getOrDefault(m.content, () -> answerAppealRequest(m.content)).call();
            else if (state == BotState.WaitPassword)
                response = inner.getOrDefault(m.content, () -> checkPassword(m.content)).call();
            else
                response = inner.getOrDefault(m.content, this::getDefaultText).call(); //ready, admin, appealAdmin
        } catch (java.lang.Exception e) {
            System.out.println("respondTo сломался");
            e.printStackTrace();
        }

        return response;
    }

    private Message askForPassword() {
        previousState = state;
        state = BotState.WaitPassword;
        return new Message(userId, "Введи пароль.", MessageType.PasswordRequest);
    }

    private Message switchFromAdminMode() {
        state = previousState;
        if (state == BotState.WaitAnswer)
            return new Message(userId, "Продолжим игру.",
                    MessageType.AdminModeDisabled, currentQuestion.options);
        return new Message(userId, "Ты снова просто смертный.", MessageType.AdminModeDisabled);
    }

    private Message checkPassword(String content) {
        if (content.equals(adminPassword)) {
            state = BotState.Admin;
            return new Message(userId, "Привет, босс.", MessageType.AdminAuthSuccess);
        }
        state = previousState;
        return new Message(userId,"Неверный пароль.", MessageType.AdminAuthFailed);
    }

    private Message tryShowAppealRequest() {
        int requestsLeft;
        requestsLeft = appealRequests.size();
        if (requestsLeft == 0) {
            state = BotState.Admin;
            return new Message(userId, "Апелляций нет.",
                    MessageType.NoAppealsLeft);
        }
        currentAppealRequest = appealRequests.remove(0);
        appealUserId = currentAppealRequest.userId;
        state = BotState.WaitsForAppealDecision;
        return new Message(userId, stringifyRequest(currentAppealRequest), MessageType.AppealDecisionRequest);
    }

    private Message answerAppealRequest(String content) {
        state = BotState.AppealAdmin;
        return new Message(appealUserId, "Ответ администратора: " + content, MessageType.AppealAnswer);
    }

    private String stringifyRequest(AppealRequest appealRequest) {
        StringBuilder listString = new StringBuilder();

        if (appealRequest.question.options != null) {
            listString.append("\nВарианты ответа: ");

            for (String s : appealRequest.question.options)
                listString.append(s).append("\t");
        }

        return String.format("Вопрос: %s %s \n Правильный ответ: %s \nВариант пользователя: %s",
                appealRequest.question.text, listString.toString(), appealRequest.question.rightAnswer,
                appealRequest.userAnswer);
    }

    private Message switchToAppealMode() {
        previousState = state;
        state = BotState.WaitAppealRequest;
        return new Message(userId, "А какой ответ, по-твоему, правильный?",
                MessageType.WriteYourOpinion);
    }

    private Message createAppealRequest(String content) {
        appealRequests.add(new AppealRequest(userId, currentQuestion, content));
        state = previousState;
        if (state == BotState.WaitAnswer) {
            return tryAskNextQuestion(false);
        }
        return new Message(userId, APPEAL_CONSIDERED, MessageType.AppealConsidered);
    }

    private Message getHelpText() {
        return new Message(userId, HELP_TEXT, MessageType.Help);
    }

    private Message getAdminHelpText() {
        return new Message(userId, ADMIN_HELP, MessageType.Help);
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
        questionsLeft = Math.min(gameLength, questions.size());
        if (questionsLeft == 0)
            return new Message(userId, "Извини, вопросы кончились :с", MessageType.CantStartGame);
        score = 0;
        chooseQuestion();
        state = BotState.WaitAnswer;
        return new Message(userId, currentQuestion.text, MessageType.Question, currentQuestion.options);
    }

    private Message getDefaultText(){
        return new Message(userId, DEFAULT_TEXT, MessageType.IncorrectCommand);
    }

    private Message checkAnswer(String content){
        if (isCorrect(content)) {
            score++;
            return tryAskNextQuestion(true);
        }
        score--;
        return new Message(userId,
                "Неправильно :с  Ты теряешь 1 очко. Ещё варианты?",
                MessageType.WrongAnswer, currentQuestion.options);

    }

    private Message tryAskNextQuestion(boolean answeredCorrectly) {
        questionsLeft--;
        String responseText = answeredCorrectly ? "Верно!" : "Апелляция записана.";
        if (questionsLeft == 0) {
            state = BotState.Ready;
            return new Message(userId, String.format(responseText +
                    " Ура, викторина наконец-то закончилась! Ты набрал %d очков", score),
                    MessageType.Results);
        }
        chooseQuestion();
        return new Message(userId, String.format(responseText + " Следующий вопрос: \n %s",
                currentQuestion.text), MessageType.Question, currentQuestion.options);
    }

    private void chooseQuestion() {
        int index = random.nextInt(questions.size());
        currentQuestion = questions.get(index);
        questions.remove(index);
    }

    private boolean isCorrect(String answer) { return answer.equals(currentQuestion.rightAnswer); }
}