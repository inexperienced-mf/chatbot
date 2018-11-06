package chatbot;

public enum BotState {
    NotWorking,
    Ready,
    WaitAnswer,
    WaitAppealRequest,
    Admin,
    WaitPassword,
    WaitsForAppealDecision,
    AppealAdmin
}