package ru.vsu.putin_p_a.gallows.controllers;

import ru.vsu.putin_p_a.PageFormatLoader;
import ru.vsu.putin_p_a.web_server.configuration.Configuration;
import ru.vsu.putin_p_a.web_server.http_protocol.ResponseStatus;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Controller;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Get;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.IExceptionController;

import java.text.MessageFormat;

@Controller("/gallowsGame/cantConnectToDBException")
public class CantConnectToDBException implements IExceptionController {
    public static final MessageFormat PAGE = PageFormatLoader.load(Configuration.ROOT + "gallowsGame/cantConnectToDBException.html");
    @Get("")
    @Override
    public String showErrorPage(Exception e, ResponseStatus s) {
        return PAGE.format(s);
    }
}
