package com.meetcha.log.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CustomHttpRequestWrapper extends HttpServletRequestWrapper {
    private byte [] requestBody;

    public CustomHttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ServletInputStream is = request.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte [] buffer = new byte[1024];
        int length;
        while((length = is.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer,0,  length);
        }
        this.requestBody = byteArrayOutputStream.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.requestBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }
        };
    }

    public byte[] getRequestBody() {
        return this.requestBody;
    }
}
