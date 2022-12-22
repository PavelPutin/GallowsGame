package ru.vsu.putin_p_a.gallows.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GallowsGame {
    public static final String ROOT = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    private static final int MAX_TRIES_NUMBER = 7;
    private static final Random random = new Random();
    private final int ID;
    private final String category;
    private final Set<String> usedCharacters = new HashSet<>();
    private Word word;
    private int tries;
    private GameState current = null;
    public GallowsGame(int ID, String category) {
        this.ID = ID;
        this.category = category;
        tries = 0;
    }

    public void chooseWord() throws IOException {
        File wordsSource = new File(ROOT + "/wordsCategories/" + category + ".txt");
        BufferedReader reader = new BufferedReader(new FileReader(wordsSource));
        List<String> wordsPool = new ArrayList<>();
        while (reader.ready()) {
            wordsPool.add(reader.readLine());
        }

        int wordIndex = random.nextInt(wordsPool.size());
        word = new Word(wordsPool.get(wordIndex).toLowerCase());
        current = new GameState(WinStatus.IN_PROCESS, word.length(), word.getMarkedChars(), usedCharacters, tries);
    }

    public GameState makeChoice(String character) {
        if (current.winStatus() == WinStatus.WIN || current.winStatus() == WinStatus.LOSE) {
            return current;
        }
        usedCharacters.add(character);
        WinStatus ws = WinStatus.IN_PROCESS;

        if (word.markChar(character)) {
            if (word.isFullyMarked()) {
                ws = WinStatus.WIN;
            }
        } else if (++tries > MAX_TRIES_NUMBER) {
            ws = WinStatus.LOSE;
        }

        current = new GameState(ws, word.length(), word.getMarkedChars(), usedCharacters, tries);
        return current;
    }

    public GameState getCurrentState() {
        return current;
    }

    public int getID() {
        return ID;
    }

    public String getCategory() {
        return category;
    }

    public String getWord() {
        return word.getValue();
    }
}
