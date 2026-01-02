package solutions.cloudbusiness.cli;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PluginLoader {
    public Map<String, List<Map<String, String>>> loadPlugins(String configFile) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
                return yaml.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
