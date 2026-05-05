package com.lab6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AST {
    public static abstract class Node {
    }

    public static class ProgramNode extends Node {
        public HeaderNode header;
        public List<EntityNode> entities = new ArrayList<>();
        public List<RelationshipNode> relationships = new ArrayList<>();
        public LayoutNode layout;

        @Override
        public String toString() {
            return "ProgramNode{\n  header=" + header + ",\n  entities=" + entities + ",\n  relationships="
                    + relationships + ",\n  layout=" + layout + "\n}";
        }
    }

    public static class HeaderNode extends Node {
        public String name, type, theme;
        public int resX, resY;

        @Override
        public String toString() {
            return String.format("Header(name='%s', type='%s', theme='%s', res=%dx%d)", name, type, theme, resX, resY);
        }
    }

    public static class EntityNode extends Node {
        public String entityType, id, label;

        public EntityNode(String entityType, String id, String label) {
            this.entityType = entityType;
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return String.format("Entity(%s: %s='%s')", entityType, id, label);
        }
    }

    public static class RelationshipNode extends Node {
        public String sourceId, targetId, connectionType, label;
        public Map<String, String> properties = new HashMap<>();

        @Override
        public String toString() {
            return String.format("Rel(%s -[%s]-> %s, label='%s', props=%s)", sourceId, connectionType, targetId, label,
                    properties);
        }
    }

    public static class LayoutNode extends Node {
        public Map<String, String> coordinates = new HashMap<>();

        @Override
        public String toString() {
            return "Layout(" + coordinates + ")";
        }
    }
}