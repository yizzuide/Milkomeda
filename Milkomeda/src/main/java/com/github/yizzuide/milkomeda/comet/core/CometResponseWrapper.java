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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * CometResponseWrapper
 * 响应包装类，基于SpringWeb源码类ContentCachingResponseWrapper修改，因为直接用ContentCachingResponseWrapper在
 *  {@link DeferredResult} 异步响应情况下有bug。
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.14.0
 * @see org.springframework.web.util.ContentCachingResponseWrapper
 * @see org.springframework.web.filter.ShallowEtagHeaderFilter
 * <br>
 * Create at 2020/04/07 14:54
 */
public class CometResponseWrapper extends HttpServletResponseWrapper {

    private final FastByteArrayOutputStream content = new FastByteArrayOutputStream(1024);

    @Nullable
    private ServletOutputStream outputStream;

    @Nullable
    private PrintWriter writer;

    @Nullable
    private Integer contentLength;

    public CometResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void sendError(int sc) throws IOException {
        copyBodyToResponse(false);
        try {
            super.sendError(sc);
        }
        catch (IllegalStateException ex) {
            // Possibly on Tomcat when called too late: fall back to silent setStatus
            super.setStatus(sc);
        }
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        copyBodyToResponse(false);
        try {
            super.sendError(sc, msg);
        }
        catch (IllegalStateException ex) {
            // Possibly on Tomcat when called too late: fall back to silent setStatus
            super.setStatus(sc);
        }
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        copyBodyToResponse(false);
        super.sendRedirect(location);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.outputStream == null) {
            // 设置默认编码，默认为ISO-8859-1
            this.getResponse().setCharacterEncoding(StandardCharsets.UTF_8.toString());
            this.outputStream = new ResponseServletOutputStream(getResponse().getOutputStream());
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            // 设置默认编码，默认为ISO-8859-1
            this.getResponse().setCharacterEncoding(StandardCharsets.UTF_8.toString());
            String characterEncoding = getCharacterEncoding();
            this.writer = (characterEncoding != null ? new ResponsePrintWriter(characterEncoding) :
                    new ResponsePrintWriter(WebUtils.DEFAULT_CHARACTER_ENCODING));
        }
        return this.writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        // do not flush the underlying response as the content as not been copied to it yet
    }

    @Override
    public void setContentLength(int len) {
        if (len > this.content.size()) {
            this.content.resize(len);
        }
        this.contentLength = len;
    }

    // Overrides Servlet 3.1 setContentLengthLong(long) at runtime
    @Override
    public void setContentLengthLong(long len) {
        if (len > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Content-Length exceeds ContentCachingResponseWrapper's maximum (" +
                    Integer.MAX_VALUE + "): " + len);
        }
        int lenInt = (int) len;
        if (lenInt > this.content.size()) {
            this.content.resize(lenInt);
        }
        this.contentLength = lenInt;
    }

    @Override
    public void setBufferSize(int size) {
        if (size > this.content.size()) {
            this.content.resize(size);
        }
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        this.content.reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.content.reset();
    }

    /**
     * Return the cached response content as a byte array.
     * @return Content byte array.
     */
    public byte[] getContentAsByteArray() {
        return this.content.toByteArray();
    }

    /**
     * Return an {@link InputStream} to the cached content.
     * @return  InputStream
     */
    public InputStream getContentInputStream() {
        return this.content.getInputStream();
    }

    /**
     * Return the current size of the cached content.
     * @return  Content size.
     */
    public int getContentSize() {
        return this.content.size();
    }

    /**
     * Copy the complete cached body content to the response.
     * @throws IOException  Network IO exception
     */
    public void copyBodyToResponse() throws IOException {
        copyBodyToResponse(true);
    }

    /**
     * Copy the cached body content to the response.
     * @param complete whether to set a corresponding content length
     * for the complete cached body content
     * @throws IOException Socket IO exception
     */
    protected void copyBodyToResponse(boolean complete) throws IOException {
        if (this.content.size() > 0) {
            HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
            if ((complete || this.contentLength != null) && !rawResponse.isCommitted()) {
                rawResponse.setContentLength(complete ? this.content.size() : this.contentLength);
                this.contentLength = null;
            }
            boolean intercepted = false;
            if (complete) {
                if (!CollectionUtils.isEmpty(CometHolder.getResponseInterceptors())) {
                    for (CometResponseInterceptor interceptor: CometHolder.getResponseInterceptors()) {
                        HttpServletRequest request = WebContext.getRequest();
                        Object body = request == null ? null : request.getAttribute(CometResponseBodyAdvice.REQUEST_ATTRIBUTE_BODY);
                        intercepted = interceptor.writeToResponse(this.content, this, rawResponse, body);
                        if (intercepted) {
                            break;
                        }
                    }
                }
            }
            // if not intercepted from response interceptors
            if (!intercepted) {
                this.content.writeTo(rawResponse.getOutputStream());
            }
            this.content.reset();
            if (complete) {
                super.flushBuffer();
            }
        }
    }


    private class ResponseServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream os;

        public ResponseServletOutputStream(ServletOutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int b) throws IOException {
            content.write(b);
        }

        @Override
        public void write(@NonNull byte[] b, int off, int len) throws IOException {
            content.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return this.os.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            this.os.setWriteListener(writeListener);
        }

        // 在DeferredResult第二次Response响应时，会在获取OutputStream写出后，调用这个方法
        @Override
        public void flush() throws IOException {
            // 写出到响应输出流（解决 DeferredResult 异步响应问题）
            CometResponseWrapper.this.copyBodyToResponse();
            super.flush();
        }
    }


    private class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(String characterEncoding) throws UnsupportedEncodingException {
            super(new OutputStreamWriter(content, characterEncoding));
        }

        @Override
        public void write(@NotNull char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
        }

        @Override
        public void write(@NotNull String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
        }
    }
}
