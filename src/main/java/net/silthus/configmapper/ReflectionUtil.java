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

import com.google.common.base.Strings;
import lombok.extern.java.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log(topic = "art-framework:util")
public final class ReflectionUtil {

    // always edit the regexr link and update the link below!
    // the regexr link and the regex should always match
    // https://regexr.com/59dgv
    private static final Pattern QUOTED_STRING_ARRAY = Pattern.compile("^(\"(?<quoted>.*?)\")?(?<value>.*?)?,(?<rest>.*)$");

    @SuppressWarnings("unchecked")
    public static <TValue> TValue toObject(Class<TValue> fieldType, Object value) {

        if (value instanceof String) {
            return (TValue) fromString(fieldType, (String) value);
        }

        return (TValue) value;
    }

    public static Object fromString(Class<?> fieldType, String value) {

        if (fieldType.isArray()) {
            return toArray(fieldType.getComponentType(), value);
        }

        if (Boolean.class == fieldType || Boolean.TYPE == fieldType) return Boolean.parseBoolean(value);
        if (Byte.class == fieldType || Byte.TYPE == fieldType) return Byte.parseByte(value);
        if (Short.class == fieldType || Short.TYPE == fieldType) return Short.parseShort(value);
        if (Integer.class == fieldType || Integer.TYPE == fieldType) return Integer.parseInt(value);
        if (Long.class == fieldType || Long.TYPE == fieldType) return Long.parseLong(value);
        if (Float.class == fieldType || Float.TYPE == fieldType) return Float.parseFloat(value);
        if (Double.class == fieldType || Double.TYPE == fieldType) return Double.parseDouble(value);
        return value;
    }

    public static Object toArray(Class<?> arrayType, String input) {
        ArrayList<String> strings = new ArrayList<>();

        Matcher matcher = QUOTED_STRING_ARRAY.matcher(input);

        while (matcher.matches()) {
            String quoted = matcher.group("quoted");
            String value = matcher.group("value");
            input = matcher.group("rest");
            if (quoted != null) {
                strings.add(quoted);
            } else {
                strings.add(value);
            }

            if (!Strings.isNullOrEmpty(input)) {
                matcher = QUOTED_STRING_ARRAY.matcher(input);
            } else {
                break;
            }
        }
        strings.add(input);

        String[] result = strings.toArray(new String[0]);
        Object array = Array.newInstance(arrayType, result.length);
        for (int i = 0; i < result.length; i++) {
            Array.set(array, i, toObject(arrayType, result[i].trim()));
        }

        return array;
    }
}
