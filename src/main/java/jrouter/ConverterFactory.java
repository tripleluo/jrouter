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
package jrouter;

/**
 * ConverterFactory接口。负责创建转换对象。
 *
 * @param <T> ActionInvocation type.
 */
public interface ConverterFactory<T extends ActionInvocation> {

    /**
     * 根据参数创建或返回参数转换对象。
     *
     * @param actionInvocation Action运行时上下文。
     *
     * @return 参数转换对象。
     */
    ParameterConverter getParameterConverter(T actionInvocation);
}
