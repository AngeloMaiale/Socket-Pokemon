package engine;

import java.util.List;

public class BattleRoom {

    public List<TurnManager.TurnAction> resolveTurnOrder(List<TurnManager.TurnAction> actions) {
        return TurnManager.orderActions(actions);
    }

    public List<TurnManager.TurnAction> resolveTurnOrder(TurnManager.TurnAction actionA,
                                                         TurnManager.TurnAction actionB) {
        return TurnManager.determineTurnOrder(actionA, actionB);
    }
}

