package ru.vsu.putin_p_a;

import ru.vsu.putin_p_a.web_server.Server;
import ru.vsu.putin_p_a.web_server.servlet_server.container.WebContainer;
import ru.vsu.putin_p_a.web_server.servlet_server.mapper.ServletMapException;

import java.io.IOException;

public class ServerRunner {
    public static void main(String[] args) throws IOException, ServletMapException {
        Server s = new WebContainer();
        s.start();
    }
}
