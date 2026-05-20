import java.io.*;
import java.util.*;

public class EnvLoader {
    public static Map<String, String> loadEnv(String path) throws IOException {
        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                env.put(parts[0], parts[1]);
            }
        }
        return env;
    }
}
