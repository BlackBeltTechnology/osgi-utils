package hu.blackbelt.osgi.utils.test;

/*-
 * #%L
 * OSGi util tester (mock)
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Function;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Bean functions to work with an instance of object
 */
public class BeanUtil {

    private BeanUtil() {
    }

    /**
     * Call a method in an object and returns with the result.
     * @param target The bean object where method is called
     * @param instance The method parameters called on method
     * @return
     */
    public static Function<Method, Object> callMethod(final Object target, final Object... instance) {
        return new Function<Method, Object>() {
            @Nullable
            @Override
            @SneakyThrows(ReflectiveOperationException.class)
            public Object apply(@Nullable Method input) {
                return input.invoke(target, instance);
            }
        };
    }

    /**
     * Sets the field value of the given target with the given instance.
     * @param target
     * @param instance
     * @return
     */
    public static Function<Field, Void> setField(final Object target, final Object instance) {
        return new Function<Field, Void>() {
            @Nullable
            @Override
            @SneakyThrows(IllegalAccessException.class)
            public Void apply(@Nullable Field input) {
                boolean acc = input.isAccessible();
                input.setAccessible(true);
                input.set(target, instance);
                input.setAccessible(acc);
                return null;
            }
        };
    }
}
