package cz.tstrecha.foundry.orm.connection;

import cz.tstrecha.foundry.orm.config.FoundryConfiguration;
import cz.tstrecha.foundry.orm.entity.EntityContext;
import lombok.Getter;
import lombok.ToString;

@ToString
public class FoundryContext {

    private final FoundryConfiguration foundryConfiguration;
    private final Class<?> entitySourceRoot;
    @Getter
    private final EntityContext entityContext;

    public FoundryContext(FoundryConfiguration foundryConfiguration, Class<?> entitySourceRoot) {
        this.foundryConfiguration = foundryConfiguration;
        this.entitySourceRoot = entitySourceRoot;
        this.entityContext = new EntityContext(entitySourceRoot);
    }

    public DBSession openSession() {
        return new DBSession(foundryConfiguration.url(), foundryConfiguration.username(), foundryConfiguration.password(), this);
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
