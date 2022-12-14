package ru.vsu.putin_p_a.gallows.controllers;

import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.Controller;
import ru.vsu.putin_p_a.web_server.servlet_server.contoller_api.IExceptionController;

@Controller("/gallowsGame/testScriptIOException")
public class TestScriptIOException implements IExceptionController {

    @Override
    public String showErrorPage(Exception e) {
        return "";
    }
}
