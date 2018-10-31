package chatbot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Question {
    final String text;
    final String rightAnswer;
    final List<String> options;

    Question(String text, String rightAnswer) {
        this.text = text;
        this.rightAnswer = rightAnswer;
        options = null;
    }

    Question(String text, String[] options, int rightOptionIndex) {
        this.text = text;
        rightAnswer = options[rightOptionIndex];
        this.options = Collections.unmodifiableList(Arrays.asList(options));
    }

    boolean hasOptions() {
        return options != null;
    }
}
