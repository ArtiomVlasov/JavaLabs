package fabrika.exeptions;

import java.util.Set;

public class InvalidConfigException extends FactoryException {
    public InvalidConfigException(Set<String> missingKeys) {
        super("Error: Configuration file is missing required fields: " + missingKeys);
    }
}