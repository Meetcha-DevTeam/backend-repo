package com.meetcha.log.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class JsonMasker {

    private static final ObjectMapper om = new ObjectMapper();

    // 키별 마스킹 전략
    private static final Map<String, UnmaskedArea> MASK_RULES = Map.of(
            "token", new UnmaskedArea(8, 6),
            "accessToken", new UnmaskedArea(8, 6),
            "refreshToken", new UnmaskedArea(6, 4)
    );

    public static String mask(String json) {
        try {
            JsonNode root = om.readTree(json);
            maskNode(root);
            return om.writeValueAsString(root);
        }
        catch (Exception e) {
            return json; // JSON 파싱 실패 시 그대로
        }
    }

    private static void maskNode(JsonNode node) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode child = obj.get(field);

                if (MASK_RULES.containsKey(field) && child != null && child.isValueNode()) {
                    UnmaskedArea rule = MASK_RULES.get(field);
                    obj.put(field, partialMask(child.asText(), rule.prefix, rule.suffix));
                }
                else {
                    maskNode(child);
                }
            });
        }
        else if (node.isArray()) {
            for (JsonNode child : node) {
                maskNode(child);
            }
        }
    }

    private record UnmaskedArea(int prefix, int suffix) {}

    private static String partialMask(String v, int p, int s) {
        if (v == null || v.length() <= p + s) return "***";
        return v.substring(0, p) + "..." + v.substring(v.length() - s);
    }
}