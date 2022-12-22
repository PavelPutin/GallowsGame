package ru.vsu.putin_p_a.gallows.model;

import java.util.Map;
import java.util.Set;

public record GameState(WinStatus winStatus, int wordLength, Map<Integer, String> guessedChars, Set<String> usedCharacters, int tries) {
}
