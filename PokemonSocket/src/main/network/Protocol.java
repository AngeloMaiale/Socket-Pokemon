package network;

import java.util.Map;

public class Protocol {
    // Nombres exactos para que compile BattleRoom y BattleSession
    public static final String TYPE_START_BATTLE = "BATTLE_START";
    public static final String TYPE_UPDATE_STATE = "UPDATE_STATE";
    public static final String TYPE_END_BATTLE = "END_BATTLE";
    public static final String TYPE_ERROR = "ERROR";

    // Si tu BattleSession usa estos nombres, añádelos:
    public static final String RES_BATTLE_START = "BATTLE_START";
    public static final String RES_BATTLE_END = "BATTLE_END";

    public static String createMessage(String type, Map<String, String> payload) {
        StringBuilder sb = new StringBuilder(type).append(":");
        if (payload != null) {
            payload.forEach((k, v) -> sb.append(k).append("=").append(v).append(";"));
        }
        return sb.toString();
    }
}