/**
 *    Copyright (C) 2015 Mesosphere, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mesosphere.mesos.frameworks.cassandra.scheduler.util;

import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public final class Env {

    @NotNull
    public static String get(@NotNull final String key) {
        final Optional<String> opt = option(key);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            throw new IllegalStateException(String.format("Environment variable %s is not defined", key));
        }
    }

    @NotNull
    public static Optional<String> option(@NotNull final String key) {
        return Optional.fromNullable(System.getenv(key));
    }

    @NotNull
    public static Map<String, String> filterStartsWith(@NotNull final String prefix, final boolean trimPrefix) {
        final Map<String, String> result = newHashMap();

        for (final Map.Entry<String, String> entry : System.getenv().entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith(prefix)) {
                result.put(trimPrefix ? key.substring(prefix.length()) : key, entry.getValue());
            }
        }

        return result;
    }

    @NotNull
    public static String workingDir(final String defaultFileName) {
        return System.getProperty("user.dir") + defaultFileName;
    }

    public static String osFromSystemProperty() {
        final String osName = System.getProperty("os.name").toLowerCase();
        final String os;
        if (osName.contains("mac") || osName.contains("darwin")) {
            os = "macosx";
        } else if (osName.contains("linux")) {
            os = "linux";
        } else {
            throw new IllegalArgumentException("Unknown OS " + osName);
        }
        return os;
    }
}
