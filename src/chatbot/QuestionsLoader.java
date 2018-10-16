package chatbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class QuestionsLoader {
    ArrayList<Question> loadQuestions() {
        String content = null;
        try (Scanner reader = new Scanner(new File("questions.txt"))) {
            content = reader
                    .useDelimiter("\\A")
                    .next();
        } catch (IOException e) {
            System.out.println("не удалось открыть questions.txt ");
        }
        assert content != null : "Content is null";
        ArrayList<Question> questions = new ArrayList<>();
        for (String q: content.split("\r\n\r\n"))
            questions.add(makeQuestion(q));
        return questions;
    }

    private Question makeQuestion(String raw) {
        int splitIndex = raw.lastIndexOf('\n') + 1;
        return new Question(raw.substring(0,  splitIndex),
                raw.substring(splitIndex));
    }
}
