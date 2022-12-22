package ru.vsu.putin_p_a.database;

import ru.vsu.putin_p_a.web_server.configuration.Configuration;
import ru.vsu.putin_p_a.web_server.http_protocol.ResponseStatus;
import ru.vsu.putin_p_a.web_server.servlet_server.mapper.ControllerExceptionContainer;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDataBase {
    private final File dataBase;

    public UserDataBase(String pathToFile) {
        this.dataBase = new File(pathToFile);
    }

    public void addUser(String username, byte[] passwordHash) throws IOException {
        String[] data = {username, new String(passwordHash, StandardCharsets.UTF_8), "0", "0"};
        String text = String.join(",", data) + "\n";
        Files.write(Paths.get(dataBase.toURI()), text.getBytes(), StandardOpenOption.APPEND);
    }

    public boolean containsUsername(String username) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataBase));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] data = line.split(",");

            String internalUsername = data[0];
            if (internalUsername.equals(username)) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }

    public boolean containsUser(String username, String passwordHash) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataBase));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] data = line.split(",");

            String internalUsername = data[0];
            String internalHash = data[1];
            if (internalUsername.equals(username) && Arrays.equals(internalHash.getBytes(), passwordHash.getBytes())) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }

    public UserCouple getUser(String username, String passwordHash) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataBase));
        while (reader.ready()) {
            String line = reader.readLine();
            String[] data = line.split(",");

            String internalUsername = data[0];
            String internalHash = data[1];
            if (internalUsername.equals(username) && Arrays.equals(internalHash.getBytes(), passwordHash.getBytes())) {
                reader.close();
                int totalGames = Integer.parseInt(data[2]);
                int victories = Integer.parseInt(data[3]);
                return new UserCouple(username, passwordHash, totalGames, victories);
            }
        }
        reader.close();
        return null;
    }

    public void addGameResultUserData(String username, String passwordHash, boolean isVictory) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataBase));
        List<String> data = new ArrayList<>();
        while (reader.ready()) {
            String line = reader.readLine();
            String[] lineData = line.split(",");

            String internalUsername = lineData[0];
            String internalHash = lineData[1];
            if (username.equals(internalUsername) && passwordHash.equals(internalHash)) {
                int totalGames = Integer.parseInt(lineData[2]);
                lineData[2] = Integer.toString(totalGames + 1);
                if (isVictory) {
                    int victories = Integer.parseInt(lineData[3]);
                    lineData[3] = Integer.toString(victories + 1);
                }
                data.add(String.join(",", lineData));
            } else {
                data.add(line);
            }
        }
        reader.close();

        String text = String.join("\n", data);
        Files.write(Paths.get(dataBase.toURI()), text.getBytes(), StandardOpenOption.WRITE);
    }
}
