package chatbot;

public class AppealRequest {
    final Question question;
    final String userId;
    final String userAnswer;

    public AppealRequest(String userId, Question question, String userAnswer)
    {
        this.userId = userId;
        this.question = question;
        this.userAnswer = userAnswer;
    }
}
