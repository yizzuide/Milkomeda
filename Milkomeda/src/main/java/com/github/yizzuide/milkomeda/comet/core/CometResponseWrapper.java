package com.github.yizzuide.milkomeda.comet.core;

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * CometResponseWrapper
 * 响应包装类
 *
 * @author yizzuide
 * @since 3.0.0
 * @see org.springframework.web.util.ContentCachingResponseWrapper
 * @see org.springframework.web.filter.ShallowEtagHeaderFilter
 * Create at 2020/04/07 14:54
 */
public class CometResponseWrapper extends HttpServletResponseWrapper {

    // 包装ContentCachingResponseWrapper实现，为了不影响Spring的处理流程
    private ContentCachingResponseWrapper responseWrapper;

    public CometResponseWrapper(HttpServletResponse response) {
        super(response);
        responseWrapper = new ContentCachingResponseWrapper(response);
    }

    @Override
    public void sendError(int sc) throws IOException {
        responseWrapper.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        responseWrapper.sendError(sc, msg);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        responseWrapper.sendRedirect(location);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return responseWrapper.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return responseWrapper.getWriter();
    }

    @Override
    public void flushBuffer() throws IOException {
        responseWrapper.flushBuffer();
    }

    @Override
    public void setContentLength(int len) {
        responseWrapper.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        responseWrapper.setContentLengthLong(len);
    }

    @Override
    public void setBufferSize(int size) {
        responseWrapper.setBufferSize(size);
    }

    @Override
    public void resetBuffer() {
        responseWrapper.reset();
    }

    @Override
    public void reset() {
        responseWrapper.reset();
    }

    public byte[] getContentAsByteArray() {
        return responseWrapper.getContentAsByteArray();
    }

    public InputStream getContentInputStream() {
        return responseWrapper.getContentInputStream();
    }

    public int getContentSize() {
        return this.responseWrapper.getContentSize();
    }

    public void copyBodyToResponse() throws IOException {
        responseWrapper.copyBodyToResponse();
    }
}
