package ru.vsu.putin_p_a;

import java.io.*;
import java.text.MessageFormat;

public class PageFormatLoader {
    public static MessageFormat load(String path) {
        File page = new File(path);
        try (BufferedReader reader = new BufferedReader(new FileReader(page))) {
            StringBuilder sb = new StringBuilder();
            while (reader.ready()) {
                sb.append(reader.readLine());
            }
            return new MessageFormat(sb.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
