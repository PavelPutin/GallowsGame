package ru.vsu.putin_p_a.gallows.controllers;

import ru.vsu.putin_p_a.PageFormatLoader;
import ru.vsu.putin_p_a.database.UserDataBase;
import ru.vsu.putin_p_a.gallows.model.GallowsGame;
import ru.vsu.putin_p_a.gallows.model.GameState;
import ru.vsu.putin_p_a.gallows.model.WinStatus;
import ru.vsu.putin_p_a.web_server.configuration.Configuration;
import ru.vsu.putin_p_a.web_server.http_protocol.ResponseStatus;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Controller;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Get;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.IController;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Param;
import ru.vsu.putin_p_a.web_server.servlet_server.mapper.ControllerExceptionContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/gallowsGame")
public class GallowsGameController implements IController {
    public static final String PAGES_DIR = Configuration.ROOT + "gallowsGame/";
    private static final MessageFormat INDEX_PAGE,
            LOGIN_PAGE,
            SUCCESS_LOGIN_PAGE,
            REJECTED_LOGIN,
            SIGNUP_PAGE,
            SUCCESS_SIGNUP_PAGE,
            REJECTED_SIGNUP_PAGE,
            ACCOUNT_PAGE,
            GAME_PAGE;
    private static final UserDataBase USER_DATA_BASE = new UserDataBase(Configuration.ROOT + "userdatabase.csv");
    private static final String DIGEST_ALGORITHM = "MD5";
    private static final MessageDigest DIGEST;
    private static final List<String> CATEGORIES;
    private static int nextId = 0;
    private static final Map<UserGame, GallowsGame> USER_AND_GAME = new HashMap<>();

    static {
        try {
            DIGEST = MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        INDEX_PAGE = PageFormatLoader.load(PAGES_DIR + "index.html");
        LOGIN_PAGE = PageFormatLoader.load(PAGES_DIR + "login.html");
        SUCCESS_LOGIN_PAGE = PageFormatLoader.load(PAGES_DIR + "successLogin.html");
        REJECTED_LOGIN = PageFormatLoader.load(PAGES_DIR + "rejectedLogin.html");
        SIGNUP_PAGE = PageFormatLoader.load(PAGES_DIR + "signup.html");
        SUCCESS_SIGNUP_PAGE = PageFormatLoader.load(PAGES_DIR + "successSignup.html");
        REJECTED_SIGNUP_PAGE = PageFormatLoader.load(PAGES_DIR + "rejectedSignup.html");
        ACCOUNT_PAGE = PageFormatLoader.load(PAGES_DIR + "account.html");
        GAME_PAGE = PageFormatLoader.load(PAGES_DIR + "game.html");

        File categoriesFilesDir = new File(Configuration.ROOT + "wordsCategories");
        CATEGORIES = Arrays.stream(categoriesFilesDir.list())
                .map(fileName -> fileName.substring(0, fileName.lastIndexOf(".")))
                .toList();
    }

    @Get("/index")
    public String index() {
        return INDEX_PAGE.format(new Object[0]);
    }

    @Get("/login")
    public String login() {
        return LOGIN_PAGE.format(new Object[0]);
    }

    @Get("/loginResult")
    public String loginResult(@Param("username") String username, @Param("password") String password) {
        try {
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            byte[] hash = DIGEST.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashString = new String(hash, StandardCharsets.UTF_8);
            if (USER_DATA_BASE.containsUser(decodedUsername, hashString)) {
                return SUCCESS_LOGIN_PAGE.format(new Object[]{decodedUsername, URLEncoder.encode(new String(hash), StandardCharsets.UTF_8)});
            }
            return REJECTED_LOGIN.format(new Object[0]);
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Get("/signup")
    public String signup() {
        return SIGNUP_PAGE.format(new Object[0]);
    }

    @Get("/signupResult")
    public String signupResult(@Param("username") String username, @Param("password") String password) {
        try {
            if (!USER_DATA_BASE.containsUsername(username)) {
                byte[] hash = DIGEST.digest(password.getBytes(StandardCharsets.UTF_8));
                String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
                USER_DATA_BASE.addUser(decodedUsername, hash);

                String encoded = URLEncoder.encode(new String(hash), StandardCharsets.UTF_8);
                return SUCCESS_SIGNUP_PAGE.format(new Object[]{decodedUsername, encoded});
            }
            return REJECTED_SIGNUP_PAGE.format(new Object[]{username});
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Get("/account")
    public String account(@Param("username") String username, @Param("passwordHash") String passwordHash) {
        try {
            String hash = URLDecoder.decode(passwordHash, StandardCharsets.UTF_8);
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            boolean validUser = USER_DATA_BASE.containsUser(decodedUsername, hash);
            if (validUser) {
                return ACCOUNT_PAGE.format(new Object[]{decodedUsername});
            }
            return REJECTED_LOGIN.format(new Object[0]);
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Get("/startGame")
    public int startGame(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("category") String category) {
        try {
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            String decodedHash = URLDecoder.decode(passwordHash, StandardCharsets.UTF_8);
            String decodedCategory = URLDecoder.decode(category, StandardCharsets.UTF_8);

            UserGame userGame = new UserGame(decodedUsername, decodedHash, nextId);
            GallowsGame game = new GallowsGame(nextId, decodedCategory);
            game.chooseWord();

            USER_AND_GAME.put(userGame, game);
            return nextId++;
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Get("/game")
    public String game(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("gameId") String gameId) {
        GallowsGame game = getGame(username, passwordHash, gameId);
        if (game == null) {
            throw new ControllerExceptionContainer(
                    new IllegalAccessException("Не удалось получить данную игру"),
                    "/gallowsGame/unexcitingGame",
                    ResponseStatus.NOT_FOUND);
        }
        return GAME_PAGE.format(new Object[] {gameId, game.getCategory()});
    }

    @Get("/getGameState")
    public String getGameState(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("gameId") String gameId) {
        GallowsGame game = getGame(username, passwordHash, gameId);
        return game.getCurrentState().toString();
    }

    @Get("/makeChoice")
    public String makeChoice(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("gameId") String gameId,
            @Param("character") String character) {
        GallowsGame game = getGame(username, passwordHash, gameId);
        String decodedCharacter = URLDecoder.decode(character, StandardCharsets.UTF_8);
        GameState state = game.makeChoice(decodedCharacter);
        return state.toString();
    }

    @Get("/endGame")
    public String endGame(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("gameId") String gameId) {
        try {
            String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
            String decodedHash = URLDecoder.decode(passwordHash, StandardCharsets.UTF_8);
            UserGame ug = new UserGame(decodedUsername, decodedHash, Integer.parseInt(gameId));
            GallowsGame game = USER_AND_GAME.get(ug);
            boolean victory = game.getCurrentState().winStatus().equals(WinStatus.WIN);
            USER_DATA_BASE.addGameResultUserData(decodedUsername, decodedHash, victory);
            return game.getWord();
        } catch (IOException e) {
            throw new ControllerExceptionContainer(
                    e,
                    "/gallowsGame/cantConnectToDBException",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Get("/getCategories")
    public String getCategories() {
        return String.join(",", CATEGORIES);
    }

    @Get("/accountScript.js")
    public byte[] accountScript() {
        File script = new File(Configuration.ROOT + "gallowsGame/accountScript.js");
        try (FileInputStream input = new FileInputStream(script)) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new ControllerExceptionContainer(e, "/gallowsGame/testScriptIOException", ResponseStatus.NOT_FOUND);
        }
    }

    @Get("/gameScript.js")
    public byte[] gameScriptJS() {
        File script = new File(Configuration.ROOT + "gallowsGame/gameScript.js");
        try (FileInputStream input = new FileInputStream(script)) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new ControllerExceptionContainer(e, "/gallowsGame/testScriptIOException", ResponseStatus.NOT_FOUND);
        }
    }

    @Get("/gameStyle.css")
    public byte[] gameStyleCSS() {
        File style = new File(Configuration.ROOT + "gallowsGame/gameStyle.css");
        try (FileInputStream input = new FileInputStream(style)) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new ControllerExceptionContainer(e, "/gallowsGame/testScriptIOException", ResponseStatus.NOT_FOUND);
        }
    }

    private GallowsGame getGame(String username, String passwordHash, String gameId) {
        String decodedUsername = URLDecoder.decode(username, StandardCharsets.UTF_8);
        String decodedHash = URLDecoder.decode(passwordHash, StandardCharsets.UTF_8);
        int iGameId = Integer.parseInt(gameId);
        return USER_AND_GAME.get(new UserGame(decodedUsername, decodedHash, iGameId));
    }
}
