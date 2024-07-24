package antiCaptcha;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;

public class StringHelper {
    public static String toCamelCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";

        for (String part : parts) {
            camelCaseString += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
        }

        return camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
    }

    public static String imageFileToBase64String(String path) {
        try {
            return new String(Base64.encodeBase64(Files.readAllBytes(Paths.get(path))));
        } catch (Exception e) {
            return null;
        }
    }
}
