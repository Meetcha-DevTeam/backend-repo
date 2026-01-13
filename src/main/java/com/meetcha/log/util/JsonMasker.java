package com.meetcha.log.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class JsonMasker {

    private static final ObjectMapper om = new ObjectMapper();

    // 요청 마스킹 전략
    public static final Map<String, UnmaskedArea> REQUEST_MASK_RULES = Map.of(
            "code", new UnmaskedArea(6, 6),
            "accessToken", new UnmaskedArea(6, 6),
            "refreshToken", new UnmaskedArea(6, 6)
    );

    // 응답 마스킹 전략
    public static final Map<String, UnmaskedArea> RESPONSE_MASK_RULES = Map.of(
            "token", new UnmaskedArea(6, 6),
            "accessToken", new UnmaskedArea(6, 6),
            "refreshToken", new UnmaskedArea(6, 6)
    );

    public static String mask(String json, Map<String, UnmaskedArea> maskingRules) {
        try {
            JsonNode root = om.readTree(json);
            maskNode(root, maskingRules);
            return om.writeValueAsString(root);
        }
        catch (Exception e) {
            return json; // JSON 파싱 실패 시 그대로
        }
    }

    private static void maskNode(JsonNode node, Map<String, UnmaskedArea> maskingRules) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode child = obj.get(field);

                if (maskingRules.containsKey(field) && child != null && child.isValueNode()) {
                    UnmaskedArea rule = maskingRules.get(field);
                    obj.put(field, partialMask(child.asText(), rule.prefix, rule.suffix));
                }
                else {
                    maskNode(child, maskingRules);
                }
            });
        }
        else if (node.isArray()) {
            for (JsonNode child : node) {
                maskNode(child, maskingRules);
            }
        }
    }

    private record UnmaskedArea(int prefix, int suffix) {}

    private static String partialMask(String v, int p, int s) {
        if (v == null || v.length() <= p + s) return "***";
        return v.substring(0, p) + "..." + v.substring(v.length() - s);
    }
}