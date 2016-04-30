package Reactor.protocol;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Collections;
import ThreadPerClient.MultipleClientProtocolServer;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
*
* This class represents the Bluffer game. It handles the questions and answers of the game and manage the points of each player.
*/
public class Bluffer {

	private HashMap<String, String> qNa;
	private Vector<String> questions;
	private HashMap<String , TBGProtocol> playerAnswers;
	private String[] AllAnswers;
	private String choices;
	private boolean allAnswered;
	private int selectCounter;

	public Bluffer(){
		questions = new Vector<>();
		qNa = new HashMap<>();
		playerAnswers = new HashMap<>();
		selectCounter = 0;
	}


	public void parseJson(String jsonPath) throws IOException, ParseException {
		FileReader reader = new FileReader(jsonPath);

		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(reader);

		// get an array from the JSON object
		JsonArray questionsJsonArray= (JsonArray) jsonObject.get("questions");

		// take each value from the json array separately
		for (Object aQuestionsJsonArray : questionsJsonArray) {
			JsonObject questionJson = (JsonObject) aQuestionsJsonArray;
			String question =questionJson.get("questionText").getAsString();
			String answer = questionJson.get("realAnswer").getAsString();
			questions.add(question);
			qNa.put(question, answer);
		}
		Collections.shuffle(questions);
	}

	public String getQuestion(int i){
		return questions.get(i);
	}
	public String getAnswer(String q){
		return qNa.get(q);
	}

	public boolean allAnswerd(){
		return allAnswered;
	}
	public void storeAnswer(String answer, TBGProtocol proto, int max , String realAnswer){
		playerAnswers.put(answer, proto);
		allAnswered = false;
		if (playerAnswers.size() == max){
			List<String> keys = new ArrayList();
			keys.addAll(playerAnswers.keySet());
			keys.add(realAnswer);
			AllAnswers = new String[keys.size()];
			Collections.shuffle(keys);
			
			choices = "";
			for (int i = 0; i<keys.size(); i++) {
				String o = keys.get(i);
				choices = choices + i + ". "+ o + " ";				
				AllAnswers[i]=o;
			}	
			allAnswered=true;
		}
	}
	public String getChoices(){
		return choices;
	}

	public boolean answerSummary(int ans,TBGProtocol proto, String realAnswer){
		allAnswered = false;
		selectCounter++;
		String answer = AllAnswers[ans];
		if(answer.equals(realAnswer)){
			proto.setPoints(10);
			if(selectCounter == AllAnswers.length-1){
				selectCounter = 0;
				allAnswered = true;
			}
			return true;
		}
		
		else{
			for(String str : playerAnswers.keySet()){
				if(str.equals(answer)){
					playerAnswers.get(str).setPoints(5);
					
				}
			}
			if(selectCounter == AllAnswers.length-1){
				selectCounter = 0;
				allAnswered = true;
			}
			return false;
		}

	}
	
	public void reset(){
		playerAnswers.clear();
//		AllAnswers = null;
		choices = "";
	}
	
	public String pointsSummary(){
		String summary = "Summary: ";
		for(String s : playerAnswers.keySet()){
			summary = summary + playerAnswers.get(s).getMyName() + ": " + playerAnswers.get(s).getPoints() + "pts, ";
		}
		qNa.clear();
		questions.clear();
		reset();
		return summary;
		
	}
}
