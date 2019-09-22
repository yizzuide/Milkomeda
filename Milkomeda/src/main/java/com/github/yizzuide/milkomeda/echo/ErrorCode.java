package com.github.yizzuide.milkomeda.echo;

/**
 * ErrorCode
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 1.13.1
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
