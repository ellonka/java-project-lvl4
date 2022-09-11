package hexlet.code;

import java.net.MalformedURLException;
import java.net.URL;

public final class Utils {
    public static String extractDomain(String input) throws MalformedURLException {
        URL url = new URL(input);
        String[] urlParts = input.split("/");
        String result = urlParts[0] + "//" + urlParts[2];
        return result;
    }
}
