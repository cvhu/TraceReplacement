// CS394R Assignment 4
// Exercise 7.7, Sutton & Barto

import java.util.*;
import java.io.*;

class RightWrongProcess{
	int curr;
	int ret;
	int rew;
	int n;
	public RightWrongProcess(int nodes){
		n = nodes;
		reset();
	}

	public void reset(){
		curr = 0;
		rew = 0;
		ret = 0;
	}

	public void right(){
		curr++;
	}

	public void wrong(){
	}

	public void action(String a){
		if(a.equals("right")){
			right();
		}else if(a.equals("wrong")){
			wrong();
		}
		reward();
	}

	public boolean terminate(){
		return (curr == n);
	}

	public void reward(){
		if(terminate()){
			rew = 1;
		}else{
			rew = 0;
		}
		ret += rew;
	}

	public String getState(){
		return String.format("s%d",curr);
	}

	public int getReward(){
		return rew;
	}
}

public class TraceReplacement{
	int nodes = 100;

	public String etaGreedy(HashMap<String, Double> qa, double eta){
		Random rand = new Random();
		Double maxValue = Double.NEGATIVE_INFINITY;
		int maxCount = 0;
		int totalCount = 0;
		LinkedList<String> actions = new LinkedList<String>();
		LinkedList<Double> probs = new LinkedList<Double>();
		for(String action : qa.keySet()){
			totalCount++;
			Double currValue = qa.get(action);
			// System.out.printf("%f vs. %f\n", currValue, maxValue);
			int compare = currValue.compareTo(maxValue);
			if(compare > 0){
				maxValue = currValue;
				maxCount = 1;
			}else if(compare == 0){
				// System.out.println("duplicate max");
				maxCount++;
			}
		}
		Double exploreProb = eta/totalCount;
		Double greedyProb = (1.0 - eta)/maxCount + exploreProb;
		Double oldProb = 0.0;
		// System.out.printf("eV: %f\n gV: %f\nmaxCount: %d\n max: %f\n", exploreValue, greedyValue, maxCount, max);
		for(String action : qa.keySet()){
			Double currValue = qa.get(action);
			if(currValue.compareTo(maxValue)==0){
				oldProb += greedyProb;
			}else{
				oldProb += exploreProb;
			}
			// System.out.printf("%f\n", oldValue);
			probs.add(oldProb);
			actions.add(action);
		}
		double r = rand.nextDouble();
		for(int i = 0; i < totalCount; i++){
			if(r < probs.get(i)){
				return actions.get(i);
			}
		}
		return actions.get(totalCount-1);
	}	

	public void Sarsa(double lambda, double eta, double alpha, double gamma, int n, String method){		
		HashMap<String, HashMap<String, Double>> q = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, HashMap<String, Double>> e = new HashMap<String, HashMap<String, Double>>();
		RightWrongProcess rwp = new RightWrongProcess(nodes);

		// initialize Q arbitrarily, and e = 0 for all s, a
		for(int i = 0; i < n; i++){		
			HashMap<String, Double> qa = new HashMap<String, Double>();
			qa.put("right", 0.5);
			qa.put("wrong", 0.5);
			q.put(String.format("s%d", i), qa);
			HashMap<String, Double> ea = new HashMap<String, Double>();
			ea.put("right", 0.0);
			ea.put("wrong", 0.0);
			e.put(String.format("s%d", i), ea);
		}

		// repeat for each episode
		for(int i = 0; i < n; i++){
			rwp.reset();
			String s = rwp.getState();			
			String a = etaGreedy(q.get(s), eta);
			int rs = 0;
			int steps = 0;
			while(!rwp.terminate()){
				steps++;
				rwp.action(a);
				int r = rwp.getReward();
				String s_n = rwp.getState();
				// System.out.println(s_n+s);
				String a_n = etaGreedy(q.get(s_n), eta);				
				double delta = r + gamma*q.get(s_n).get(a_n) - q.get(s).get(a);

				HashMap<String, Double> ea = e.get(s);
				double eav = ea.get(a);
				if(method.equals("accumulate")){
					ea.put(a, eav+1);
				}else if(method.equals("replace")){
					for(String action : q.get(s).keySet()){
						ea.put(action, 1);
					}					
				}else if(method.equals("replace variant")){
					for(String action : q.get(s).keySet()){
						if(action.equals(a)){
							ea.put(action, eav+1);
						}else{
							ea.put(action, 0);
						}							
					}										
				}else{
					ea.put(a, eav+1);					
				}
				e.put(s, ea);
				
				for(String state : q.keySet()){
					HashMap<String, Double> qa = q.get(state);
					ea = e.get(state);
					for(String action : q.get(state).keySet()){						
						Double qav = qa.get(action);
						eav = ea.get(action);
						qa.put(action, qav + alpha*delta*e.get(state).get(action));
						ea.put(action, eav*gamma*lambda);						
					}
					q.put(state, qa);
					e.put(state, ea);
				}
				
				s = s_n;
				a = a_n;
				rs += r;
			}			
			System.out.println(steps);
		}		
	}
	
	public static void main(String[] args){
		TraceReplacement tr = new TraceReplacement();
		tr.Sarsa(0.1, 0.1, 0.1, 1.0, 500, "accumulate");
	}
}