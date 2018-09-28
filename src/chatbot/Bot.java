package chatbot;

import java.io.File;
import java.io.IOException;
import java.util.*;

class Bot {
    private ArrayList<Message> history;
    private String userId;
    private BotState state;
    private static final String HELP_TEXT = "very helpful text";
    private static final String GREET_TEXT = "friendly greeting text";
    private Question currentQuestion;
    private Random random;
    private List<Question> list;

    public Bot(String id){
		userId = id;
		history = new ArrayList<Message>();
        list = new ArrayList<Question>();
        random = new Random();
    }
	  
    public Message respondTo(Message m) {
        history.add(m);
        String content = m.content;
        String id = m.userId;
        switch(m.content) {
        	case "/help":
        		return new Message(id, HELP_TEXT);
        	case "/start":
        		return new Message(id, GREET_TEXT);
        	case "/askme":
        		if (state == BotState.WaitAnswer)
        			return new Message(id,"Dude, think again");
        		state = BotState.WaitAnswer;
        		chooseQuestion();
        		return new Message(id, currentQuestion.questionText);
        	default:
        		if (state == BotState.WaitAnswer)
        		{
        			if (isCorrect(content)) {
        				state = BotState.Initial;
        				return new Message(id,"Good job dude");
        			}
        			else
        				return new Message(id, "Bad job. Try again");
        		}
        		else
                    return new Message(id, "Dude, think again");
        }
	}

    private void chooseQuestion() {
        if (list.isEmpty())
            loadQuestions();
        int index = random.nextInt(list.size());
        this.currentQuestion = list.get(index);
        list.remove(index);
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
            System.out.println("HAHAHA GOTCHA");
        }
               
        list = new ArrayList<Question>();
		for (String q: content.split("\n\n"))
			list.add(makeQuestion(q));
    }

	private Question makeQuestion(String raw) {
		int splitIndex = raw.lastIndexOf('\n') + 1;
		return new Question(raw.substring(0,  splitIndex),
				raw.substring(splitIndex));
	}
    
    private boolean isCorrect(String m) {
    	return m.equals(currentQuestion.rightAnswer);
    }

}