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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.*;

@Controller("/gallowsGame")
public class GallowsGame implements IController {

    private static final Random random = new Random();
    private static final MessageFormat INDEX_PAGE;
    private static final MessageFormat GAME_PAGE;
    private static final String ENCODING = "UTF-8";
    private static final String DIGEST_ALGORITHM = "MD5";
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        INDEX_PAGE = PageFormatLoader.load(Configuration.ROOT + "gallowsGame/index.html");
        GAME_PAGE = PageFormatLoader.load(Configuration.ROOT + "gallowsGame/game.html");
    }

    private final Map<String, String> userAndWord = new HashMap<>();

    @Get("/index")
    public String index() {
        return INDEX_PAGE.format(new Object[0]);
    }

    @Get("/start")
    public String signIn(@Param("name") String name, @Param("password") String password) {
        try {
            byte[] bytesOfMessage = password.getBytes(ENCODING);
            byte[] digestPassword = digest.digest(bytesOfMessage);
            StringBuilder hash = new StringBuilder();
            for (byte b : digestPassword) {
                hash.append(String.format("%02X ", b));
            }
            return GAME_PAGE.format(new Object[]{name, hash.toString()});
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
//        return String.format("name: %s%npassword: %s", );
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
    }
}
