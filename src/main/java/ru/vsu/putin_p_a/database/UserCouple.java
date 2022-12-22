package ru.vsu.putin_p_a.database;

public record UserCouple(String username, String passwordHash, int totalGames, int totalWins) {
}
