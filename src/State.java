import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class State {
    private int id;
    private boolean isAccepting;
    private Map<Character, Set<State>> transitions;
    private Set<State> epsilonTransitions;

    public State(int id) {
        this.id = id;
        this.isAccepting = false;
        this.transitions = new HashMap<>();
        this.epsilonTransitions = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public void addTransition(char symbol, State target) {
        if (!transitions.containsKey(symbol)) {
            transitions.put(symbol, new HashSet<>());
        }
        transitions.get(symbol).add(target);
    }

    public void addEpsilonTransition(State target) {
        epsilonTransitions.add(target);
    }

    public Set<State> getTransitions(char symbol) {
        return transitions.getOrDefault(symbol, new HashSet<>());
    }

    public Set<State> getEpsilonTransitions() {
        return new HashSet<>(epsilonTransitions);
    }

    public Map<Character, Set<State>> getAllTransitions() {
        return new HashMap<>(transitions);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return id == state.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}