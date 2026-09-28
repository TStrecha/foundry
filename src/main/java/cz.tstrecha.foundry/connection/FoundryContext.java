package cz.tstrecha.foundry.connection;

import cz.tstrecha.foundry.config.FoundryConfiguration;
import cz.tstrecha.foundry.entity.EntityCreationStrategy;
import cz.tstrecha.foundry.entity.EntityDefinition;
import cz.tstrecha.foundry.entity.EntityScanner;
import cz.tstrecha.foundry.entity.system.SystemEntities;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
public class FoundryContext {

    private final FoundryConfiguration foundryConfiguration;
    private final Class<?> entitySourceRoot;

    @Getter
    private static Map<Class<?>, EntityDefinition> entityDefinitions = null;
    @Getter
    private static Map<Class<?>, EntityCreationStrategy<?>> entityCreationStrategies = null;

    public FoundryContext(FoundryConfiguration foundryConfiguration, Class<?> entitySourceRoot) {
        this.foundryConfiguration = foundryConfiguration;
        this.entitySourceRoot = entitySourceRoot;

        entityDefinitions = EntityScanner.scan(SystemEntities.class, entitySourceRoot);
        entityCreationStrategies = EntityScanner.scanStrategies(SystemEntities.class, entitySourceRoot);
    }

    public DBSession openSession() {
        return new DBSession(foundryConfiguration.url(), foundryConfiguration.username(), foundryConfiguration.password());
    }

    public static FoundryContext.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private String username;
        private String password;
        private Class<?> entitySourceRoot;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder entitySourceRoot(Class<?> entitySourceRoot) {
            this.entitySourceRoot = entitySourceRoot;
            return this;
        }

        public FoundryContext build() {
            var config = new FoundryConfiguration(this.url, this.username, this.password);
            return new FoundryContext(config, entitySourceRoot);
        }
    }

}
