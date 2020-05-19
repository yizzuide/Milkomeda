package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.util.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

/**
 * CometRequestWrapper
 * 请求包装
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.5.0
 * @see org.springframework.web.util.ContentCachingRequestWrapper
 * Create at 2019/12/12 17:37
 */
@Slf4j
public class CometRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 用于可重复获取
     */
    private final byte[] body;

    public CometRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 将body数据存储起来
        String bodyStr = getBodyString(request);
        if (StringUtils.isEmpty(bodyStr)) {
            body = new byte[0];
            return;
        }
        body = bodyStr.getBytes(Charset.defaultCharset());
    }

    /**
     * 获取请求参数
     * @param request   HttpServletRequest
     * @param formBody  是否从消息体获取
     * @return  JSON格式化字符串
     * @since 3.0.3
     */
    public static String resolveRequestParams(HttpServletRequest request, boolean formBody) {
        String requestData = HttpServletUtil.getRequestData(request);
        // 如果form方式获取为空，取消息体内容
        if (formBody && "{}".equals(requestData)) {
            CometRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, CometRequestWrapper.class);
            if (requestWrapper == null) {
                return requestData;
            }
            // 从请求包装里获取
            String body = requestWrapper.getBodyString();
            // 删除换行符
            body = body == null ? "" : body.replaceAll("\\n?\\t?", "");
           return body;
        }
        return requestData;
    }

    /**
     * 获取请求Body
     *
     * @param request request
     * @return String
     */
    public String getBodyString(final ServletRequest request) {
        try {
            return inputStream2String(request.getInputStream());
        } catch (IOException e) {
            log.error("Comet get input stream error:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取请求Body
     *
     * @return String
     */
    public String getBodyString() {
        final InputStream inputStream = new ByteArrayInputStream(body);
        return inputStream2String(inputStream);
    }

    /**
     * 将流里的数据读取出来并转换成字符串
     *
     * @param inputStream inputStream
     * @return String
     */
    private String inputStream2String(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            // 如果消息体没有数据
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (ClientAbortException | SocketTimeoutException ignore) {
            return null;
        } catch (IOException e) {
            log.error("Comet read input stream error:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Comet close input stream error:{}", e.getMessage(), e);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                if (body.length == 0) return -1;
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }
}
