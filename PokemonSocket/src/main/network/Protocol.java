package network;

import java.util.HashMap;
import java.util.Map;

public final class Protocol {
    public static final String TYPE_HANDSHAKE = "HANDSHAKE";
    public static final String TYPE_WAITING = "WAITING";
    public static final String TYPE_MATCHMADE = "MATCHMADE";
    public static final String TYPE_START_BATTLE = "START_BATTLE";
    public static final String TYPE_REQUEST_MOVE = "REQUEST_MOVE";
    public static final String TYPE_SEND_MOVE = "SEND_MOVE";
    public static final String TYPE_UPDATE_STATE = "UPDATE_STATE";
    public static final String TYPE_END_BATTLE = "END_BATTLE";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_PING = "PING";

    private Protocol() {
        // Utility class
    }

    public static String createMessage(String type, Map<String, String> payload) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        builder.append("\"type\":\"").append(escape(type)).append("\"");
        builder.append(",\"payload\":");
        builder.append(buildPayload(payload));
        builder.append('}');
        return builder.toString();
    }

    public static Message parseMessage(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return null;
        }

        String type = parseField(trimmed, "type");
        String payloadRaw = parseNested(trimmed, "payload");
        Map<String, String> payload = parsePayload(payloadRaw);
        return new Message(type, payload);
    }

    public static String buildPayload(Map<String, String> payload) {
        if (payload == null) {
            return "{}";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append('\"').append(escape(entry.getKey())).append('\"');
            builder.append(':');
            builder.append('\"').append(escape(entry.getValue())).append('\"');
            first = false;
        }
        builder.append('}');
        return builder.toString();
    }

    private static String parseField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int index = json.indexOf(key);
        if (index < 0) {
            return null;
        }
        int colon = json.indexOf(':', index + key.length());
        if (colon < 0) {
            return null;
        }
        int start = json.indexOf('"', colon + 1);
        if (start < 0) {
            return null;
        }
        int end = json.indexOf('"', start + 1);
        if (end < 0) {
            return null;
        }
        return unescape(json.substring(start + 1, end));
    }

    private static String parseNested(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int index = json.indexOf(key);
        if (index < 0) {
            return "{}";
        }
        int colon = json.indexOf(':', index + key.length());
        if (colon < 0) {
            return "{}";
        }
        int start = json.indexOf('{', colon + 1);
        int end = json.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            return "{}";
        }
        return json.substring(start, end + 1);
    }

    private static Map<String, String> parsePayload(String raw) {
        Map<String, String> payload = new HashMap<>();
        String trimmed = raw.trim();
        if (trimmed.length() < 2 || !trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return payload;
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        int index = 0;
        while (index < body.length()) {
            while (index < body.length() && Character.isWhitespace(body.charAt(index))) {
                index++;
            }
            if (index >= body.length()) {
                break;
            }
            if (body.charAt(index) != '"') {
                break;
            }
            int keyStart = index + 1;
            int keyEnd = body.indexOf('"', keyStart);
            if (keyEnd < 0) {
                break;
            }
            String key = unescape(body.substring(keyStart, keyEnd));
            index = keyEnd + 1;
            while (index < body.length() && (body.charAt(index) == ' ' || body.charAt(index) == ':')) {
                index++;
            }
            if (index >= body.length() || body.charAt(index) != '"') {
                break;
            }
            int valueStart = index + 1;
            int valueEnd = body.indexOf('"', valueStart);
            if (valueEnd < 0) {
                break;
            }
            String value = unescape(body.substring(valueStart, valueEnd));
            payload.put(key, value);
            index = valueEnd + 1;
            while (index < body.length() && (body.charAt(index) == ' ' || body.charAt(index) == ',')) {
                index++;
            }
        }
        return payload;
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String unescape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }

    public static final class Message {
        private final String type;
        private final Map<String, String> payload;

        public Message(String type, Map<String, String> payload) {
            this.type = type;
            this.payload = payload != null ? payload : new HashMap<>();
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getPayload() {
            return payload;
        }
    }
}
