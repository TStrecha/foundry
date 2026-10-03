package cz.tstrecha.foundry.orm.config;

import lombok.Builder;

import java.util.List;

@Builder
public record FoundryConfiguration(String url, String user, String password, List<Class<?>> scanningRoots) {

    @Override
    public String toString() {
        return "FoundryConfiguration{" +
                "scanningRoots=" + scanningRoots +
                ", user='" + user + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
