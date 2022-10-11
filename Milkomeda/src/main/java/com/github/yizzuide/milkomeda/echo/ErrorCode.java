/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.echo;

/**
 * ErrorCode
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 1.13.1
 * <br>
 * Create at 2019/09/03 16:47
 */
public interface ErrorCode {
    /** 记录不存在 */
    int RECORD_IS_NOT_EXISTS = 1000;
    /** 查询数据为空 */
    int QUERY_DATA_IS_EMPTY = 1001;
    /** 新增数据失败 */
    int CREATE_RECORD_FAIL = 2001;
    /** 新增记录列有唯一冲突 */
    int CREATE_RECORD_UNIQUE_CONFLICT = 2002;
    /** 当前记录状态不正确 */
    int RECODE_INVALID_STATUS = 2003;
    /** 数据匹配不一致 */
    int MATCH_DATA_FAIL = 2004;
    /** 记录更新失败 */
    int UPDATE_RECORD_FAIL = 2005;
    /** 数据解析异常 */
    int ANALYSIS_DATA_FAIL = 3000;
    /** 请求时间线错误 */
    int REQUEST_TIME_LINE_FAIL = 3001;
    /** 请求参数异常 */
    int REQUEST_PARAM_EXCEPTION = 4000;
    /** 请求参数不符合要求 */
    int REQUEST_PARAM_FORMAT_EXCEPTION = 4001;
    /** 参数不匹配 **/
    int INVALID_REQUEST_PARAM = 4002;
    /** 不支持的请求类型 **/
    int NOT_SUPPORT_REQUEST_METHOD = 4003;
    /** 服务器执行时内部抛出异常，请重试 */
    int SERVER_ERROR = 5000;
    /** 重复的请求 */
    int REPEAT_REQUEST = 6000;
    /** 服务超时异常 */
    int REQUEST_TIMEOUT = 6001;
    /** 请求过于频繁 */
    int REQUEST_FREQUENTLY = 6002;
    /** 第三方平台响应为空 */
    int VENDOR_RESPONSE_IS_NOTHING = 7000;
    /** 第三方平台响应失败 */
    int VENDOR_RESPONSE_IS_FAIL = 7001;
    /** 第三方平台处理业务失败 */
    int VENDOR_PROCESS_FAIL = 7002;
    /** 第三方平台请求错误 */
    int VENDOR_REQUEST_ERROR = 7003;
    /** 第三方平台回调错误 */
    int VENDOR_CALLBACK_ERROR = 7004;
    /** 第三方平台内部错误 */
    int VENDOR_SERVER_ERROR = 7005;
    /** 第三方平台响应数据解析错误 */
    int VENDOR_SERVER_RESPONSE_DATA_ANALYSIS_FAIL = 7006;
    /** 未知异常 */
    int UNKNOWN = 9999;
}
