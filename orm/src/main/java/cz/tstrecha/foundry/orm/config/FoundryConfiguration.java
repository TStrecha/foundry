package cz.tstrecha.foundry.orm.config;

import lombok.Builder;

import java.util.List;

import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotEmptyArgument;
import static cz.tstrecha.foundry.orm.util.ValidationUtils.requireNotNullArgument;

@Builder
public record FoundryConfiguration(String url, String user, String password, List<Class<?>> scanningRoots) {

    public FoundryConfiguration {
        requireNotEmptyArgument(url, "url");
        requireNotEmptyArgument(user, "user");
        requireNotNullArgument(password, "password");
        requireNotNullArgument(scanningRoots, "scanningRoots");
    }

    @Override
    public String toString() {
        return "FoundryConfiguration{" +
                "scanningRoots=" + scanningRoots +
                ", user='" + user + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
