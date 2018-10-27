package chatbot;

import com.google.common.collect.Iterables;
import com.sun.jdi.InvalidLineNumberException;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        for (String q: content.split("\r\n\r\n")) {
            try {
                questions.add(makeQuestion(q));
            } catch (QuestionParseException e) {
                e.printStackTrace();
            }
        }
        return questions;
    }

    private Question makeQuestion(String raw) throws QuestionParseException {
        String[] lines = raw.split("\r\n");
        if (lines.length < 2)
            throw new QuestionParseException(String.format("Вопрос не соответствует формату:\n%s", raw));
        String text = lines[0];
        if (lines.length == 2)
            return new Question(text, lines[1]);
        int rightOption = Integer.parseInt(lines[lines.length - 1]);
        return new Question(text, Arrays.copyOfRange(lines, 1, lines.length - 1), rightOption - 1);
    }
}
