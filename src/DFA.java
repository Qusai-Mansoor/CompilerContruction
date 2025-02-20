import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DFA {
    private int startState;
    private Set<Integer> acceptStates;
    private Map<Integer, Map<Character, Integer>> transitions;
    private int stateCount;
    
    public DFA() {
        this.acceptStates = new HashSet<>();
        this.transitions = new HashMap<>();
        this.stateCount = 0;
    }
    
    public void setStartState(int state) {
        this.startState = state;
    }
    
    public void addAcceptState(int state) {
        this.acceptStates.add(state);
    }
    
    public void addTransition(int fromState, char symbol, int toState) {
        if (!transitions.containsKey(fromState)) {
            transitions.put(fromState, new HashMap<>());
        }
        transitions.get(fromState).put(symbol, toState);
    }
    
    public int getStateCount() {
        return stateCount;
    }
    
    // Convert NFA to DFA using subset construction algorithm
    public static DFA fromNFA(NFA nfa) {
        DFA dfa = new DFA();
        Map<Set<State>, Integer> dfaStates = new HashMap<>();
        Queue<Set<State>> unmarkedStates = new LinkedList<>();
        
        // Get epsilon closure of NFA start state as first DFA state
        Set<State> initialClosure = nfa.getEpsilonClosure(Collections.singleton(nfa.getStartState()));
        dfaStates.put(initialClosure, 0);
        dfa.setStartState(0);
        unmarkedStates.add(initialClosure);
        
        // Check if the initial state is accepting
        for (State state : initialClosure) {
            if (state.isAccepting()) {
                dfa.addAcceptState(0);
                break;
            }
        }
        
        // Get all alphabet symbols used in NFA
        Set<Character> alphabet = new HashSet<>();
        for (State state : nfa.getAllStates()) {
            alphabet.addAll(state.getAllTransitions().keySet());
        }
        
        // Process all unmarked DFA states
        while (!unmarkedStates.isEmpty()) {
            Set<State> currentStates = unmarkedStates.poll();
            int currentDfaState = dfaStates.get(currentStates);
            
            // For each symbol, compute the next DFA state
            for (char symbol : alphabet) {
                // Move on symbol, then get epsilon closure
                Set<State> nextStates = nfa.getEpsilonClosure(nfa.move(currentStates, symbol));
                
                // Skip if no transition
                if (nextStates.isEmpty()) {
                    continue;
                }
                
                // Create new DFA state if not seen before
                if (!dfaStates.containsKey(nextStates)) {
                    int newStateId = dfaStates.size();
                    dfaStates.put(nextStates, newStateId);
                    unmarkedStates.add(nextStates);
                    
                    // Check if new state is accepting
                    for (State state : nextStates) {
                        if (state.isAccepting()) {
                            dfa.addAcceptState(newStateId);
                            break;
                        }
                    }
                }
                
                // Add transition in DFA
                dfa.addTransition(currentDfaState, symbol, dfaStates.get(nextStates));
            }
        }
        
        dfa.stateCount = dfaStates.size();
        return dfa;
    }
    
    // Method to display transition table
    public void displayTransitionTable() {
        System.out.println("DFA Transition Table:");
        System.out.println("---------------------");
        
        // Get all used symbols
        Set<Character> allSymbols = new HashSet<>();
        for (Map<Character, Integer> stateTransitions : transitions.values()) {
            allSymbols.addAll(stateTransitions.keySet());
        }
        List<Character> sortedSymbols = new ArrayList<>(allSymbols);
        Collections.sort(sortedSymbols);
        
        // Print header
        System.out.print("State\t");
        for (char symbol : sortedSymbols) {
            System.out.print("| " + symbol + "\t");
        }
        System.out.println("| Accepting");
        System.out.println("-".repeat(70));
        
        // Print transitions for each state
        for (int stateId = 0; stateId < stateCount; stateId++) {
            System.out.print("q" + stateId + "\t");
            
            Map<Character, Integer> stateTransitions = transitions.getOrDefault(stateId, new HashMap<>());
            
            for (char symbol : sortedSymbols) {
                if (stateTransitions.containsKey(symbol)) {
                    System.out.print("| q" + stateTransitions.get(symbol) + "\t");
                } else {
                    System.out.print("| -\t");
                }
            }
            
            // Print accepting status
            System.out.println("| " + (acceptStates.contains(stateId) ? "Yes" : "No"));
        }
        
        System.out.println("\nStart State: q" + startState);
        System.out.println("Accept States: " + acceptStates.stream()
                .map(id -> "q" + id)
                .reduce((a, b) -> a + ", " + b)
                .orElse("None"));
        System.out.println("Total States: " + stateCount);
    }
}