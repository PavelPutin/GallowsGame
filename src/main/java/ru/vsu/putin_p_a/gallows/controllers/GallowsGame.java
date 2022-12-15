package ru.vsu.putin_p_a.gallows.controllers;

import ru.vsu.putin_p_a.PageFormatLoader;
import ru.vsu.putin_p_a.web_server.configuration.Configuration;
import ru.vsu.putin_p_a.web_server.http_protocol.ResponseStatus;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Controller;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Get;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.IController;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Param;
import ru.vsu.putin_p_a.web_server.servlet_server.mapper.ControllerExceptionContainer;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("/gallowsGame")
public class GallowsGame implements IController {

    private static final Random random = new Random();
    public static final String pagesDir = Configuration.ROOT + "gallowsGame/";
    private static final MessageFormat INDEX_PAGE,
            LOGIN_PAGE,
            SUCCESS_LOGIN_PAGE,
            REJECTED_LOGIN,
            SIGNUP_PAGE,
            SUCCESS_SIGNUP_PAGE,
            REJECTED_SIGNUP_PAGE,
            ACCOUNT_PAGE,
            GAME_PAGE;
    private static final File USER_DATA_BASE = new File(Configuration.ROOT + "userdatabase.csv");
    private static final String DIGEST_ALGORITHM = "MD5";
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


        INDEX_PAGE = PageFormatLoader.load( pagesDir + "index.html");
        LOGIN_PAGE = PageFormatLoader.load(pagesDir + "login.html");
        SUCCESS_LOGIN_PAGE = PageFormatLoader.load(pagesDir + "successLogin.html");
        REJECTED_LOGIN = PageFormatLoader.load(pagesDir + "rejectedLogin.html");
        SIGNUP_PAGE = PageFormatLoader.load(pagesDir + "signup.html");
        SUCCESS_SIGNUP_PAGE = PageFormatLoader.load(pagesDir + "successSignup.html");
        REJECTED_SIGNUP_PAGE = PageFormatLoader.load(pagesDir + "rejectedSignup.html");
        ACCOUNT_PAGE = PageFormatLoader.load(pagesDir + "account.html");
        GAME_PAGE = PageFormatLoader.load(pagesDir + "game.html");
    }

    private final Map<String, String> userAndWord = new HashMap<>();

    @Get("/index")
    public String index() {
        return INDEX_PAGE.format(new Object[0]);
    }

    /*
    * Пользователь ввёл правильные имя и пароль
    *   Пользователь ввёл правильные имя и пароль, но он уже в системе
    * Пользователь ввёл неправильное имя или пароль
    * */
    @Get("/login")
    public String login() {
        return LOGIN_PAGE.format(new Object[0]);
    }

    @Get("/loginResult")
    public String loginResult(@Param("username") String username, @Param("password") String password) {
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        if (dataBaseContainsUser(username, new String(hash, StandardCharsets.UTF_8))) {
            return SUCCESS_LOGIN_PAGE.format(new Object[] {username, URLEncoder.encode(new String(hash), StandardCharsets.UTF_8)});
        }
        return REJECTED_LOGIN.format(new Object[0]);
    }
    
    @Get("/signup")
    public String signup() {
        return SIGNUP_PAGE.format(new Object[0]);
    }

    @Get("/signupResult")
    public String signupResult(@Param("username") String username, @Param("password") String password) {
        if (!dataBaseContainsUsername(username)) {
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            addUser(username, hash);

            return SUCCESS_SIGNUP_PAGE.format(new Object[]{username, URLEncoder.encode(new String(hash), StandardCharsets.UTF_8)});
        }
        return REJECTED_SIGNUP_PAGE.format(new Object[]{username});
    }

    private void addUser(String username, byte[] passwordHash) {
        String[] data = {username, new String(passwordHash, StandardCharsets.UTF_8), "0", "0"};
        String text = String.join(",", data) + "\n";
        try {
            Files.write(Paths.get(USER_DATA_BASE.toURI()), text.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean dataBaseContainsUsername(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_BASE))) {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] data = line.split(",");

                String internalUsername = data[0];
                if (internalUsername.equals(username)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Get("/account")
    public String account(@Param("username") String username, @Param("passwordHash") String passwordHash) {
        if (dataBaseContainsUser(username, passwordHash)) {
            return ACCOUNT_PAGE.format(new Object[] {username});
        }
        return REJECTED_LOGIN.format(new Object[0]);
    }
    
    @Get("/game")
    public String game() {
        return "";
    }

    private boolean dataBaseContainsUser(String username, String passwordHash) {
        String hash = URLDecoder.decode(passwordHash, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_BASE))) {
            while (reader.ready()) {
                String line = reader.readLine();
                String[] data = line.split(",");

                String internalUsername = data[0];
                String internalHash = data[1];
                if (internalUsername.equals(username) && Arrays.equals(internalHash.getBytes(), hash.getBytes())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @Get("/preGameStage")
    public String preGameStage(@Param("name") String name, @Param("password") String password) {
//        byte[] bytesOfMessage = password.getBytes(StandardCharsets.UTF_8);
//        byte[] digestPassword = digest.digest(bytesOfMessage);
//        StringBuilder hash = new StringBuilder();
//        for (byte b : digestPassword) {
//            hash.append(String.format("%02X ", b));
//        }
//        return GAME_PAGE.format(new Object[]{name, hash.toString()});
        if (dbContainsName(name) && passwordVerified(name, password)) {

        } else
        return "";
    }

    @Get("/getWordLength")
    public int getWordLength(@Param("category") String category, @Param("name") String name) {
        File wordsPool = new File(Configuration.ROOT + "wordsCategories/" + category + ".txt");
        try (BufferedReader input = new BufferedReader(new FileReader(wordsPool))) {
            List<String> words = input.lines().toList();
            int wordIndex = random.nextInt(words.size());
            String selectedWord = words.get(wordIndex);
            userAndWord.put(name, selectedWord);
            System.out.println(userAndWord.get(name));
            return selectedWord.length();
        } catch (IOException e) {
            return -1;
        }
    }

    @Get("/checkChar")
    public boolean checkChar(@Param("character") String character, @Param("name") String name) {
        String decoded = URLDecoder.decode(character, StandardCharsets.UTF_8);
        System.out.println(decoded);
        return userAndWord.get(name).contains(decoded);
    }

    @Get("/testScript.js")
    public byte[] testScriptJs() {
        File script = new File(Configuration.ROOT + "gallowsGame/testScript.js");
        try (FileInputStream input = new FileInputStream(script)) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new ControllerExceptionContainer(e, "/gallowsGame/testScriptIOException", ResponseStatus.NOT_FOUND);
        }
    }*/
}
