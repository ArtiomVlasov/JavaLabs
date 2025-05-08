package fabrika.config;

import fabrika.exeptions.FactoryException;
import fabrika.exeptions.InvalidConfigException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ConfigReader {
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.txt";
    private static final Set<String> REQUIRED_KEYS = Set.of(
            "StorageBodySize",
            "StorageMotorSize",
            "StorageAccessorySize",
            "StorageAutoSize",
            "SupplierBodyDelay",
            "SupplierMotorDelay",
            "SupplierAccessoryDelay",
            "DealerDelay",
            "Workers",
            "Dealers"
    );

    public static Properties loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(fis);
            Set<String> missingKeys = new HashSet<>(REQUIRED_KEYS);
            missingKeys.removeAll(properties.stringPropertyNames());

            if (!missingKeys.isEmpty()) {
                throw new InvalidConfigException(missingKeys);
            }
            return properties;

        } catch (IOException e) {
            throw new FactoryException("Error loading configuration: " + e.getMessage());
        }
    }
}
