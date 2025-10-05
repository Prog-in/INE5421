package uai.helcio.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class ResourcesUtils {
    public static Stream<String> readFileLines(String fileName, Charset charset) throws IOException {
        BufferedReader fis = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset));
        return fis.lines().onClose(() -> {
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
