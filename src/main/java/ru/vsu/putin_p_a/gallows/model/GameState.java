package ru.vsu.putin_p_a.gallows.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record GameState(WinStatus winStatus, int wordLength, Map<Integer, String> openChars, Set<String> usedCharacters, int tries) {
}
