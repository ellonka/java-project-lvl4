package hexlet.code;

import java.net.MalformedURLException;
import java.net.URL;

public final class Utils {
    public static String extractDomain(String input) throws MalformedURLException {
        URL url = new URL(input);
        String protocol = url.getProtocol();
        String authority = url.getAuthority();
        String result = protocol + "://" + authority;
        return result;
    }

    public static boolean isStatusPositive(int status) {
        String statusText = Integer.toString(status);
        return statusText.startsWith("2");
    }
}
