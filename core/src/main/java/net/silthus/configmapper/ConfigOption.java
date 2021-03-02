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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigOption {

    /**
     * Allows overwriting the name used inside the config.
     * <p>By default the name of the field formatted with the given {@link FieldNameFormatter} is used.
     *
     * @return the name of the config option used in the config
     */
    String value() default "";

    /**
     * @return a description about the config option
     */
    String[] description() default {};

    /**
     * @return true if the config option is required and the inject should fail if the value is missing
     */
    boolean required() default false;

    /**
     * This is useful for some parsers that use a position based approach.
     * <p>Start with the position 0 and increment by one.
     * <p>If the postion is -1 then a key must be used.
     *
     * @return the position of the config option
     */
    int position() default -1;
}
