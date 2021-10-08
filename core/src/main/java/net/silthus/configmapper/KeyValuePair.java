/*
 * Copyright 2020 ART-Framework Contributors (https://github.com/Silthus/art-framework)
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

package net.silthus.configmapper;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Optional;

@Value
@EqualsAndHashCode(of = "key")
public class KeyValuePair {

    public static KeyValuePair of(String key, Object value) {
        return new KeyValuePair(key, value);
    }

    String key;
    Object value;

    private KeyValuePair(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }
}
