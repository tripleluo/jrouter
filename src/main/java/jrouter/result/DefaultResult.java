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
package jrouter.result;

import jrouter.ActionInvocation;
import jrouter.annotation.Result;
import jrouter.annotation.ResultType;

/**
 * 内置结果类型。
 */
public class DefaultResult {

    /** 默认结果类型名称，不作任何处理 */
    public static final String EMPTY = "empty";

    /** action forward结果类型名称 */
    public static final String FORWARD = "forward";

    /**
     * 默认结果类型，未作任何处理。
     *
     * @param invocation Action运行时上下文。
     */
    @ResultType(type = EMPTY)
    public static void result(ActionInvocation invocation) {
        // invocation.invoke();
    }

    /**
     * Action结果直接调用映射的Action，类似forward结果类型。
     * forward可多次关联调用，需自行判断循环调用。
     *
     * @param invocation Action运行时上下文。
     *
     * @return 返回forward后的调用结果。
     *
     */
    @ResultType(type = FORWARD)
    public static Object actionForward(ActionInvocation invocation) {
        return invocation.getActionFactory().invokeAction(invocation.getResult().location());
    }
    ////////////////////////////////////////////////////////////////////////////
    /**
     * result not found
     */
    public static final String RESULT_NOT_FOUND = "resultNotFound";

    /**
     * result not found
     *
     * @param invocation Action运行时上下文。
     *
     * @return 抛出NullPointerException。
     */
    @Result(name = RESULT_NOT_FOUND)
    public static Object resultNotFound(ActionInvocation invocation) {
        throw new NullPointerException("Result not found : " + invocation.getResult().location());
    }
}