package ru.vsu.putin_p_a.gallows.model;

import java.util.HashMap;
import java.util.Map;

class Word {
    private String value;
    private boolean[] isCharAtMarked;

    public Word(String value) {
        this.value = value;
        isCharAtMarked = new boolean[value.length()];
    }

    public boolean markChar(String character) {
        boolean anyMarked = false;
        int i = 0;
        while (value.indexOf(character, i) != -1) {
            anyMarked = true;
            int index = value.indexOf(character, i);
            isCharAtMarked[index] = true;
            i = index + 1;
        }
        return anyMarked;
    }

    public boolean isFullyMarked() {
        for (boolean markedCharacter : isCharAtMarked) {
            if (!markedCharacter) {
                return false;
            }
        }
        return true;
    }

    public Map<Integer, String> getMarkedChars() {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < isCharAtMarked.length; i++) {
            if (isCharAtMarked[i]) {
                result.put(i, String.valueOf(value.charAt(i)));
            }
        }
        return result;
    }

    public int length() {
        return value.length();
    }

    public String getValue() {
        return value;
    }
}
