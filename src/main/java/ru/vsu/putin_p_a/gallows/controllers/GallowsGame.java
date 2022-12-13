package ru.vsu.putin_p_a.gallows.controllers;

import ru.vsu.putin_p_a.PageFormatLoader;
import ru.vsu.putin_p_a.web_server.configuration.Configuration;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Controller;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Get;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.IController;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Param;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

@Controller("/gallowsGame")
public class GallowsGame implements IController {
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

        INDEX_PAGE = PageFormatLoader.load(Configuration.ROOT + "\\html\\index.html");
        GAME_PAGE = PageFormatLoader.load(Configuration.ROOT + "\\html\\game.html");
    }

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
}
