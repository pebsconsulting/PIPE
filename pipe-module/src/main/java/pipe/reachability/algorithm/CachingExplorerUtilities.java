package pipe.reachability.algorithm;

import pipe.animation.Animator;
import pipe.animation.PetriNetAnimator;
import pipe.models.component.place.Place;
import pipe.models.component.token.Token;
import pipe.models.component.transition.Transition;
import pipe.models.petrinet.PetriNet;
import pipe.parsers.FunctionalResults;
import pipe.parsers.PetriNetWeightParser;
import pipe.reachability.state.HashedState;
import pipe.reachability.state.State;
import pipe.visitor.ClonePetriNet;

import java.util.*;

/**
 * Useful methods to help explore the state space.
 *
 * Performs caching of frequent computations
 */
public class CachingExplorerUtilities implements ExplorerUtilities {
    /**
     *
     */
    private final PetriNet petriNet;

    /**
     * Animator for the Petri net
     */
    private final Animator animator;

    /**
     * Cached successors is used when exploring states to quickly determine
     * a states successors it has already seen before.
     * <p/>
     * It will be most useful when exploring cyclic transitions
     */
    private Map<State, Map<State, Collection<Transition>>> cachedSuccessors = new HashMap<>();

    /**
     *
     * Takes a copy of the Petri net to use for state space exploration so
     * not to affect the reference
     *
     * @param petriNet petri net to use for state space exploration
     */
    public CachingExplorerUtilities(PetriNet petriNet) {
        this.petriNet = ClonePetriNet.clone(petriNet);
        animator = new PetriNetAnimator(this.petriNet);
    }

    /**
     *
     * Finds successors of the given state. A successor is a state that occurs
     * when one of the enabled transitions in the current state is fired.
     *
     * Performs caching of the successors to speed up computation time
     * when a state is queried more than once. This is particularly useful
     * if on the fly vanishing state exploration is used
     *
     * @param state
     * @return map of successor states to the transitions that caused them
     */
    @Override
    public Map<State, Collection<Transition>> getSuccessorsWithTransitions(State state) {

        if (cachedSuccessors.containsKey(state)) {
            return cachedSuccessors.get(state);
        }


        setState(petriNet, state);
        Collection<Transition> enabled = animator.getEnabledTransitions();
        Map<State, Collection<Transition>> successors = new HashMap<>();
        for (Transition transition : enabled) {
            setState(petriNet, state);
            animator.fireTransition(transition);
            State successor = getCurrentState();


            if (!successors.containsKey(successor)) {
                successors.put(successor, new LinkedList<Transition>());
            }
            successors.get(successor).add(transition);
        }

        cachedSuccessors.put(state, successors);

        return successors;
    }

    @Override
    public Collection<State> getSuccessors(State state) {
        return getSuccessorsWithTransitions(state).keySet();
    }

    @Override
    public double rate(State state, State successor) {
        Collection<Transition> transitionsToSuccessor = getTransitions(state, successor);
        return getWeightOfTransitions(transitionsToSuccessor);
    }


    /**
     * Creates a new state containing the token counts for the
     * current Petri net.
     *
     * @return current state of the Petri net
     */
    @Override
    public State getCurrentState() {
        Map<String, Map<Token, Integer>> tokenCounts = new HashMap<>();
        for (Place place : petriNet.getPlaces()) {
            Map<Token, Integer> counts = new HashMap<>();
            for (Token token : petriNet.getTokens()) {
                counts.put(token, place.getTokenCount(token));
            }
            tokenCounts.put(place.getId(), counts);
        }

        boolean tanigble = isTangible(tokenCounts);
        return (tanigble ? HashedState.tangibleState(tokenCounts) : HashedState.vanishingState(tokenCounts));
    }

    /**
     * A tangible state is one in which:
     * a) Has no enabled transitions
     * b) Has entirely timed transitions leaving it
     *
     *
     * @param tokenCounts to test for tangibility
     * @return true if the current token count setting is tangible
     */
    private boolean isTangible(Map<String, Map<Token, Integer>> tokenCounts) {
        setState(tokenCounts);
        Set<Transition> enabledTransitions = animator.getEnabledTransitions();
        boolean anyTimed = false;
        boolean anyImmediate = false;
        for (Transition transition : enabledTransitions) {
            if (transition.isTimed()) {
                anyTimed = true;
            } else {
                anyImmediate = true;
            }
        }
        return enabledTransitions.isEmpty() || (anyTimed && !anyImmediate);
    }

    /**
     * Sets the Petri net to this state
     *
     * @param state contains the token counts to set the places to
     */
    private void setState(PetriNet petriNet, State state) {
        for (Place place : petriNet.getPlaces()) {
            place.setTokenCounts(state.getTokens(place.getId()));
        }
    }

    /**
     * Sets the current state based on a token counts map
     * @param tokenCounts a map of place id -> token counts
     */
    private void setState(Map<String, Map<Token, Integer>> tokenCounts) {
        for (Place place : petriNet.getPlaces()) {
            place.setTokenCounts(tokenCounts.get(place.getId()));
        }
    }


    private void setState(State state) {
        for (Place place : petriNet.getPlaces()) {
            place.setTokenCounts(state.getTokens(place.getId()));
        }
    }

    /**
     * Calculates the set of transitions that will take you from one state to the successor.
     *
     * Uses the current underlying cached methods so that duplicate calls to this method
     * will not result in another computation
     *
     * @param state     initial state
     * @param successor successor state, must be directly reachable from the state
     * @return enabled transitions that take you from state to successor, if it is not directly reachable then
     * an empty Collection will be returned
     */
    @Override
    public Collection<Transition> getTransitions(State state, State successor) {
        Map<State, Collection<Transition>> stateTransitions = getSuccessorsWithTransitions(state);
        if (stateTransitions.containsKey(successor)) {
            return stateTransitions.get(successor);
        }
        return new LinkedList<>();
    }


    /**
     *
     * Sums up the weights of the transitions. Transitions may have functional rates
     *
     * @param transitions
     * @return summed up the weight of the transitions specified
     */
    @Override
    public double getWeightOfTransitions(Iterable<Transition> transitions) {
        double weight = 0;
        PetriNetWeightParser parser = new PetriNetWeightParser(petriNet);
        for (Transition transition : transitions) {
            FunctionalResults<Double> results = parser.evaluateExpression(transition.getRateExpr());
            if (!results.hasErrors()) {
                weight += results.getResult();
            } else {
                //TODO:
            }
        }
        return weight;
    }

    /**
     *
     * @param state
     * @return all enabled transitions for the specified state
     */
    @Override
    public Collection<Transition> getAllEnabledTransitions(State state) {
        Collection<Transition> results = new LinkedList<>();
        for (Collection<Transition> transitions : getSuccessorsWithTransitions(state).values()) {
            results.addAll(transitions);
        }
        return results;
    }

}