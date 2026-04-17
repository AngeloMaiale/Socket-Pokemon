package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import models.Move;
import models.PokemonLive;

public final class TurnManager {
    private TurnManager() {
        
    }

    public static final class TurnAction {
        private final String trainerId;
        private final PokemonLive pokemon;
        private final Move selectedMove;
        private final int priority;
        private final int effectiveSpeed;
        private final int tieBreaker;

        public TurnAction(String trainerId, PokemonLive pokemon, Move selectedMove, int priority) {
            this.trainerId = trainerId;
            this.pokemon = pokemon;
            this.selectedMove = selectedMove;
            this.priority = priority;
            this.effectiveSpeed = calculateEffectiveSpeed(pokemon);
            this.tieBreaker = ThreadLocalRandom.current().nextInt();
        }

        public TurnAction(String trainerId, PokemonLive pokemon, Move selectedMove) {
            this(trainerId, pokemon, selectedMove, calculateMovePriority(selectedMove));
        }

        public String getTrainerId() {
            return trainerId;
        }

        public PokemonLive getPokemon() {
            return pokemon;
        }

        public Move getSelectedMove() {
            return selectedMove;
        }

        public int getPriority() {
            return priority;
        }

        public int getEffectiveSpeed() {
            return effectiveSpeed;
        }

        @Override
        public String toString() {
            return "TurnAction{" +
                    "trainerId='" + trainerId + '\'' +
                    ", pokemon=" + pokemon.getNickname() +
                    ", speed=" + effectiveSpeed +
                    ", priority=" + priority +
                    '}';
        }
    }

    public static int calculateEffectiveSpeed(PokemonLive pokemon) {
        if (pokemon == null) {
            return 0;
        }

        int baseSpeed = pokemon.getSpeed();
        int modifier = calculateSpeedModifier(pokemon.getStatus());
        int effectiveSpeed = (baseSpeed * modifier) / 100;
        return Math.max(effectiveSpeed, 0);
    }

    public static int calculateMovePriority(Move move) {
        if (move == null) {
            return 0;
        }
        return move.getPriority();
    }

    public static int calculateSpeedModifier(String status) {
        if (status == null) {
            return 100;
        }

        switch (status.trim().toUpperCase()) {
            case "PARALIZADO":
            case "PARALYZED":
                return 50;
            case "DORMIDO":
            case "ASLEEP":
            case "CONGELADO":
            case "FROZEN":
                return 0;
            default:
                return 100;
        }
    }

    public static List<TurnAction> orderActions(List<TurnAction> actions) {
        if (actions == null) {
            return Collections.emptyList();
        }

        List<TurnAction> ordered = new ArrayList<>(actions);
        ordered.sort(Comparator
                .comparingInt(TurnAction::getPriority).reversed()
                .thenComparingInt(TurnAction::getEffectiveSpeed).reversed()
                .thenComparingInt(action -> action.tieBreaker).reversed());
        return ordered;
    }

    public static boolean isFirstActionFaster(TurnAction first, TurnAction second) {
        if (first == null || second == null) {
            return false;
        }

        if (first.getPriority() != second.getPriority()) {
            return first.getPriority() > second.getPriority();
        }

        if (first.getEffectiveSpeed() != second.getEffectiveSpeed()) {
            return first.getEffectiveSpeed() > second.getEffectiveSpeed();
        }

        return first.tieBreaker > second.tieBreaker;
    }

    public static List<TurnAction> determineTurnOrder(TurnAction first, TurnAction second) {
        List<TurnAction> actions = new ArrayList<>(2);
        actions.add(first);
        actions.add(second);
        return orderActions(actions);
    }
}

