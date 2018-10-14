package chatbot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class QuestionsLoader {
    public ArrayList<Question> loadQuestions() {
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
