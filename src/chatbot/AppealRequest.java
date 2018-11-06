package chatbot;

class AppealRequest {
    final Question question;
    final String userId;
    final String userAnswer;

    AppealRequest(String userId, Question question, String userAnswer)
    {
        this.userId = userId;
        this.question = question;
        this.userAnswer = userAnswer;
    }
}