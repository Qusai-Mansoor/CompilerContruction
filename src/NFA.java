import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class NFA {
    private State startState;
    private State acceptState;
    private Set<State> allStates;
    private int stateIdCounter;

    public NFA() {
        this.allStates = new HashSet<>();
        this.stateIdCounter = 0;
    }

    public State createState() {
        State newState = new State(stateIdCounter++);
        allStates.add(newState);
        return newState;
    }

    public State getStartState() {
        return startState;
    }

    public void setStartState(State startState) {
        this.startState = startState;
    }

    public State getAcceptState() {
        return acceptState;
    }

    public void setAcceptState(State acceptState) {
        this.acceptState = acceptState;
    }

    public Set<State> getAllStates() {
        return new HashSet<>(allStates);
    }

    public int getTotalStates() {
        return allStates.size();
    }

    // Thompson's Construction for basic NFA from character
    public static NFA fromSymbol(char symbol) {
        NFA nfa = new NFA();
        State start = nfa.createState();
        State accept = nfa.createState();
        accept.setAccepting(true);
        
        start.addTransition(symbol, accept);
        
        nfa.setStartState(start);
        nfa.setAcceptState(accept);
        return nfa;
    }

    // Thompson's Construction for concatenation
    public static NFA concatenate(NFA first, NFA second) {
        // Connect first's accept state to second's start state
        first.getAcceptState().addEpsilonTransition(second.getStartState());
        first.getAcceptState().setAccepting(false);
        
        // Combine states
        NFA result = new NFA();
        result.allStates.addAll(first.getAllStates());
        result.allStates.addAll(second.getAllStates());
        result.stateIdCounter = Math.max(first.stateIdCounter, second.stateIdCounter);
        
        // Set start and accept states
        result.setStartState(first.getStartState());
        result.setAcceptState(second.getAcceptState());
        
        return result;
    }

    // Thompson's Construction for union (OR)
    public static NFA union(NFA first, NFA second) {
        NFA result = new NFA();
        
        // Create new start and accept states
        State newStart = result.createState();
        State newAccept = result.createState();
        newAccept.setAccepting(true);
        
        // Add epsilon transitions from new start to both NFAs' starts
        newStart.addEpsilonTransition(first.getStartState());
        newStart.addEpsilonTransition(second.getStartState());
        
        // Add epsilon transitions from both NFAs' accepts to new accept
        first.getAcceptState().addEpsilonTransition(newAccept);
        second.getAcceptState().addEpsilonTransition(newAccept);
        
        // Old accept states are no longer accepting
        first.getAcceptState().setAccepting(false);
        second.getAcceptState().setAccepting(false);
        
        // Combine all states
        result.allStates.addAll(first.getAllStates());
        result.allStates.addAll(second.getAllStates());
        result.stateIdCounter = Math.max(Math.max(first.stateIdCounter, second.stateIdCounter), result.stateIdCounter);
        
        // Set start and accept states
        result.setStartState(newStart);
        result.setAcceptState(newAccept);
        
        return result;
    }

    // Thompson's Construction for Kleene star (*)
    public static NFA kleeneStar(NFA nfa) {
        NFA result = new NFA();
        
        // Create new start and accept states
        State newStart = result.createState();
        State newAccept = result.createState();
        newAccept.setAccepting(true);
        
        // Add epsilon from new start to NFA start and to new accept
        newStart.addEpsilonTransition(nfa.getStartState());
        newStart.addEpsilonTransition(newAccept);
        
        // Add epsilon from NFA accept to NFA start (loop) and to new accept
        nfa.getAcceptState().addEpsilonTransition(nfa.getStartState());
        nfa.getAcceptState().addEpsilonTransition(newAccept);
        nfa.getAcceptState().setAccepting(false);
        
        // Combine all states
        result.allStates.addAll(nfa.getAllStates());
        result.stateIdCounter = Math.max(nfa.stateIdCounter, result.stateIdCounter);
        
        // Set start and accept states
        result.setStartState(newStart);
        result.setAcceptState(newAccept);
        
        return result;
    }

    // Get epsilon closure of a state
    private Set<State> getEpsilonClosure(State state) {
        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();
        
        stack.push(state);
        closure.add(state);
        
        while (!stack.isEmpty()) {
            State current = stack.pop();
            for (State nextState : current.getEpsilonTransitions()) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    stack.push(nextState);
                }
            }
        }
        
        return closure;
    }

    // Get epsilon closure of a set of states
    public Set<State> getEpsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>();
        
        for (State state : states) {
            closure.addAll(getEpsilonClosure(state));
        }
        
        return closure;
    }

    // Helper method to get moves from a set of states on a specific symbol
    public Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();
        
        for (State state : states) {
            result.addAll(state.getTransitions(symbol));
        }
        
        return result;
    }

    // Method to display transition table
    public void displayTransitionTable() {
        System.out.println("NFA Transition Table:");
        System.out.println("---------------------");
        
        // Get all used symbols
        Set<Character> allSymbols = new HashSet<>();
        for (State state : allStates) {
            allSymbols.addAll(state.getAllTransitions().keySet());
        }
        List<Character> sortedSymbols = new ArrayList<>(allSymbols);
        Collections.sort(sortedSymbols);
        
        // Print header
        System.out.print("State\t| ε\t");
        for (char symbol : sortedSymbols) {
            System.out.print("| " + symbol + "\t");
        }
        System.out.println("| Accepting");
        System.out.println("-".repeat(80));
        
        // Sort states by ID for cleaner display
        List<State> sortedStates = new ArrayList<>(allStates);
        sortedStates.sort(Comparator.comparingInt(State::getId));
        
        // Print transitions
        for (State state : sortedStates) {
            System.out.print("q" + state.getId() + "\t| ");
            
            // Print epsilon transitions
            Set<State> epsilonTargets = state.getEpsilonTransitions();
            if (epsilonTargets.isEmpty()) {
                System.out.print("-\t");
            } else {
                StringBuilder sb = new StringBuilder();
                for (State target : epsilonTargets) {
                    sb.append("q").append(target.getId()).append(",");
                }
                System.out.print(sb.substring(0, sb.length() - 1) + "\t");
            }
            
            // Print transitions for each symbol
            for (char symbol : sortedSymbols) {
                Set<State> targets = state.getTransitions(symbol);
                if (targets.isEmpty()) {
                    System.out.print("| -\t");
                } else {
                    StringBuilder sb = new StringBuilder("| ");
                    for (State target : targets) {
                        sb.append("q").append(target.getId()).append(",");
                    }
                    System.out.print(sb.substring(0, sb.length() - 1) + "\t");
                }
            }
            
            // Print accepting status
            System.out.println("| " + (state.isAccepting() ? "Yes" : "No"));
        }
        
        System.out.println("\nStart State: q" + startState.getId());
        System.out.println("Accept State: q" + acceptState.getId());
        System.out.println("Total States: " + allStates.size());
    }
}
