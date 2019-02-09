/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.jrouter.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.jrouter.ActionInvocation;
import net.jrouter.ConverterFactory;
import net.jrouter.JRouterException;
import net.jrouter.ParameterConverter;
import net.jrouter.util.CollectionUtil;

/**
 * 创建多参数自动映射转换器的工厂类。
 */
public class MultiParameterConverterFactory implements ConverterFactory {

    /**
     * Primitive types.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPES = new HashMap<>(8);

    static {
        PRIMITIVE_TYPES.put(boolean.class, Boolean.class);
        PRIMITIVE_TYPES.put(byte.class, Byte.class);
        PRIMITIVE_TYPES.put(char.class, Character.class);
        PRIMITIVE_TYPES.put(double.class, Double.class);
        PRIMITIVE_TYPES.put(float.class, Float.class);
        PRIMITIVE_TYPES.put(int.class, Integer.class);
        PRIMITIVE_TYPES.put(long.class, Long.class);
        PRIMITIVE_TYPES.put(short.class, Short.class);
        PRIMITIVE_TYPES.put(void.class, Void.class);
    }

    /**
     * 缓存转换参数匹配的位置
     */
    private Map<Method, int[]> methodParametersCache;

    /**
     * 转换参数类型是否固定顺序，默认固定参数。
     *
     * @see ActionInvocation#getConvertParameters()
     */
    @lombok.Getter
    private final boolean fixedOrder;

    /**
     * 多参数转换器，线程安全的单例对象。
     */
    private final ParameterConverter parameterConverter;

    /**
     * 不缓存转换参数位置的工厂类。提供一个便捷的无参数构造类。
     * MultiParameterConverterFactory.NoFixedOrder()即 MultiParameterConverterFactory(false)。
     */
    public static class NoFixedOrder extends MultiParameterConverterFactory {

        /**
         * 不缓存转换参数位置的工厂类。
         */
        public NoFixedOrder() {
            super(false);
        }

    }

    /**
     * 创建固定参数自动映射转换器的工厂类。
     */
    public MultiParameterConverterFactory() {
        this(true);
    }

    /**
     * 创建多参数自动映射转换器的工厂类。
     *
     * @param fixedOrder 参数类型是否固定顺序。
     */
    public MultiParameterConverterFactory(boolean fixedOrder) {
        this.fixedOrder = fixedOrder;
        if (fixedOrder) {
            methodParametersCache = new ConcurrentHashMap<>();
        }
        parameterConverter = new MultiParameterConverter();
    }

    /**
     * 返回线程安全的多参数自动映射转换器。
     * 此参数转换器可能需要ActionFactory支持，在创建ActionInvocation时区分处理原始参数和转换参数。
     */
    @Override
    public ParameterConverter getParameterConverter(ActionInvocation actionInvocation) {
        return parameterConverter;
    }

    /**
     * 提供多参数自动映射的转换器。不包含任何成员对象，线程安全。
     * 注入并自动映射调用的参数（无类型匹配的参数映射为{@code null}）。
     */
    public class MultiParameterConverter implements ParameterConverter {

        @Override
        public Object[] convert(Method method, Object obj, Object[] originalParams, Object[] convertParams) throws
                JRouterException {
            Class<?>[] parameterTypes = method.getParameterTypes();
            int paramSize = parameterTypes.length;
            //无参数的方法
            if (paramSize == 0) {
                return CollectionUtil.EMPTY_OBJECT_ARRAY;
            }
            if (CollectionUtil.isEmpty(originalParams) && CollectionUtil.isEmpty(convertParams)) {
                return new Object[paramSize];
            }
            Object[] allParams = CollectionUtil.append(originalParams, convertParams);
            int[] idx = match(method, 0, parameterTypes, allParams);
            if (paramSize == allParams.length && !CollectionUtil.contains(-1, idx)) {
                return allParams;
            }
            Object[] newArgs = new Object[paramSize];
            for (int i = 0; i < paramSize; i++) {
                newArgs[i] = (idx[i] == -1 ? null : allParams[idx[i]]);
            }
            return newArgs;
        }

        /**
         * 匹配追加注入的参数相对于方法参数类型中的映射；
         * 匹配顺序不考虑父子优先级，追加的参数按顺序优先匹配；{@code null}不匹配任何参数类型。
         * 如果追加注入的参数类型固定，则会缓存记录。
         *
         * @param method 指定的方法。
         * @param parameterTypes 方法的参数类型。
         * @param parameters 注入的参数。
         *
         * @return 注入的参数相对于方法参数类型中的映射。
         *
         * @see #methodParametersCache
         */
        private int[] match(Method method, int matchStart, Class<?>[] parameterTypes, Object[] parameters) {
            int[] idx = null;
            if (fixedOrder) {
                //get from cache
                idx = methodParametersCache.get(method);
                if (idx != null) {
                    return idx;
                }
            }
            idx = new int[parameterTypes.length];
            boolean[] convertMatched = null;
            if (parameters != null) {
                convertMatched = new boolean[parameters.length];
            }
            for (int i = matchStart; i < idx.length; i++) {
                //初始值-1, 无匹配
                idx[i] = -1;
                if (parameters != null) {
                    Class<?> parameterType = getClass(parameterTypes[i]);
                    for (int j = 0; j < parameters.length; j++) {
                        //不考虑父子优先级，参数按顺序优先匹配。
                        if (!convertMatched[j] && parameterType.isInstance(parameters[j])) {
                            idx[i] = j;
                            convertMatched[j] = true;
                            break;
                        }
                    }
                }
            }
            if (fixedOrder) {
                //put in cache
                methodParametersCache.put(method, idx);
            }
            return idx;
        }

        /**
         * Get Class.
         *
         * @param cls Original Class.
         *
         * @return Class.
         */
        private Class<?> getClass(Class<?> cls) {
            if (cls.isPrimitive()) {
                Class<?> pCls = PRIMITIVE_TYPES.get(cls);
                if (pCls != null) {
                    return pCls;
                }
            }
            return cls;
        }
    }
}