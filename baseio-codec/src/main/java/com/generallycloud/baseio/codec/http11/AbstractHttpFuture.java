/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
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
 */
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.SHAUtil;
import com.generallycloud.baseio.common.StringLexer;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayOutputStream;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public abstract class AbstractHttpFuture extends AbstractChannelFuture implements HttpFuture {

    private static final Map<String, String> REQ_MAPPING    = HttpHeader.REQ_MAPPING;

    protected static final KMPUtil           KMP_BOUNDARY   = new KMPUtil("boundary=");

    private ByteArrayOutputStream                  binaryBuffer;
    private byte[]                           bodyArray;
    private int                              bodyLimit;
    private String                           boundary;
    private int                              contentLength;
    private String                           contentType;
    private ChannelContext                   context;
    private List<Cookie>                     cookieList;
    private Map<String, String>              cookies;
    private StringBuilder                    currentHeaderLine;
    private boolean                          hasBodyContent;
    private boolean                          header_complete;
    private int                              headerLength;
    private int                              headerLimit;
    private String                           host;
    private String                           method;
    private Map<String, String>              params;
    private boolean                          parseFirstLine = true;
    private String                           readText;
    private Map<String, String>              request_headers;
    private String                           requestURI;
    private String                           requestURL;
    private Map<String, String>              response_headers;
    private HttpStatus                       status         = HttpStatus.C200;
    private boolean                          updateWebSocketProtocol;
    private String                           version;

    public AbstractHttpFuture(NioSocketChannel channel, int headerLimit, int bodyLimit) {
        this.context = channel.getContext();
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.request_headers = new HashMap<>();
        this.currentHeaderLine = new StringBuilder();
    }

    public AbstractHttpFuture(ChannelContext context) {
        this.context = context;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }
        cookieList.add(cookie);
    }

    @Override
    public ByteArrayOutputStream getBinaryBuffer() {
        return binaryBuffer;
    }

    @Override
    public byte[] getBodyContent() {
        return bodyArray;
    }

    @Override
    public String getBoundary() {
        return boundary;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getCookie(String name) {
        if (cookies == null) {
            return null;
        }
        return cookies.get(name);
    }

    @Override
    public List<Cookie> getCookieList() {
        return cookieList;
    }

    @Override
    public String getFutureName() {
        return getRequestURI();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public String getRequestHeader(String name) {
        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }
        String _name = REQ_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        return request_headers.get(_name);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return request_headers;
    }

    @Override
    public String getRequestParam(String key) {
        return params.get(key);
    }

    @Override
    public Map<String, String> getRequestParams() {
        return params;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getRequestURL() {
        return requestURL;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        if (response_headers == null) {
            response_headers = new HashMap<>();
            setDefaultResponseHeaders(response_headers);
        }
        return response_headers;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean hasBodyContent() {
        return hasBodyContent;
    }

    public boolean isUpdateWebSocketProtocol() {
        return updateWebSocketProtocol;
    }

    private void parse_cookies(String line) {
        if (cookies == null) {
            cookies = new HashMap<>();
        }
        StringLexer l = new StringLexer(0, StringUtil.stringToCharArray(line));
        StringBuilder value = new StringBuilder();
        String k = null;
        String v = null;
        boolean findKey = true;
        for (;;) {
            char c = l.current();
            switch (c) {
                case ' ':
                    break;
                case '=':
                    if (!findKey) {
                        throw new IllegalArgumentException();
                    }
                    k = value.toString();
                    value = new StringBuilder();
                    findKey = false;
                    break;
                case ';':
                    if (findKey) {
                        throw new IllegalArgumentException();
                    }
                    findKey = true;
                    v = value.toString();
                    value = new StringBuilder();
                    cookies.put(k, v);
                    break;
                default:
                    value.append(c);
                    break;
            }
            if (!l.next()) {
                break;
            }
        }
        cookies.put(k, value.toString());
    }

    protected abstract void parseContentType(String contentType);

    protected abstract void parseFirstLine(String line);

    protected void parseParamString(String paramString) {
        boolean findKey = true;
        int lastIndex = 0;
        String key = null;
        String value = null;
        for (int i = 0; i < paramString.length(); i++) {
            if (findKey) {
                if (paramString.charAt(i) == '=') {
                    key = paramString.substring(lastIndex, i);
                    findKey = false;
                    lastIndex = i+1;
                }
            }else{
                if (paramString.charAt(i) == '&') {
                    value = paramString.substring(lastIndex, i);
                    findKey = true;
                    lastIndex = i+1;
                    params.put(key, value);
                }
            }
        }
        if (lastIndex < paramString.length()) {
            value = paramString.substring(lastIndex);
            params.put(key, value);
        }
        
    }

    @Override
    public boolean read(NioSocketChannel channel, ByteBuf buffer) throws IOException {
        if (!header_complete) {
            readHeader(buffer);
            if (!header_complete) {
                return false;
            }
            host = getRequestHeader(HttpHeader.Req_Host);
            String contentLengthStr = getRequestHeader(HttpHeader.Req_Content_Length);
            if (!StringUtil.isNullOrBlank(contentLengthStr)) {
                this.contentLength = Integer.parseInt(contentLengthStr);
            }
            String contentType = getRequestHeader(HttpHeader.Req_Content_Type);
            parseContentType(contentType);
            String cookie = getRequestHeader(HttpHeader.Req_Cookie);
            if (!StringUtil.isNullOrBlank(cookie)) {
                parse_cookies(cookie);
            }
            if (contentLength < 1) {
                return true;
            } else {
                hasBodyContent = true;
                // FIXME 写入临时文件
                setByteBuf(allocate(channel, contentLength, bodyLimit));
            }
        }
        ByteBuf buf = getByteBuf();
        buf.read(buffer);
        if (buf.hasRemaining()) {
            return false;
        }
        buf.flip();
        bodyArray = buf.getBytes();
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            // FIXME encoding
            String paramString = new String(bodyArray, context.getEncoding());
            parseParamString(paramString);
            this.readText = paramString;
        } else {
            // FIXME 解析BODY中的内容
        }
        return true;
    }

    private void readHeader(ByteBuf buffer) throws IOException {
        StringBuilder currentHeaderLine = this.currentHeaderLine;
        for (; buffer.hasRemaining();) {
            if (++headerLength > headerLimit) {
                throw new IOException("max http header length " + headerLimit);
            }
            byte b = buffer.getByte();
            if (b == '\n') {
                if (currentHeaderLine.length() == 0) {
                    header_complete = true;
                    break;
                } else {
                    String line = currentHeaderLine.toString();
                    if (parseFirstLine) {
                        parseFirstLine = false;
                        parseFirstLine(line);
                    } else {
                        int p = line.indexOf(":");
                        if (p == -1) {
                            continue;
                        }
                        String name = line.substring(0, p).trim();
                        String value = line.substring(p + 1).trim();
                        setRequestHeader(name, value);
                    }
                    currentHeaderLine.setLength(0);
                }
                continue;
            } else if (b == '\r') {
                continue;
            } else {
                currentHeaderLine.append((char) b);
            }
        }
    }

    protected abstract void setDefaultResponseHeaders(Map<String, String> headers);

    @Override
    public void setRequestHeader(String name, String value) {
        if (StringUtil.isNullOrBlank(name)) {
            return;
        }
        String _name = REQ_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        request_headers.put(_name, value);
    }

    @Override
    public void setRequestHeaders(Map<String, String> headers) {
        this.request_headers = headers;
    }

    @Override
    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public void setRequestURL(String url) {
        this.requestURL = url;
        int index = url.indexOf("?");
        if (index > -1) {
            String paramString = url.substring(index + 1, url.length());
            parseParamString(paramString);
            requestURI = url.substring(0, index);
        } else {
            this.requestURI = url;
        }
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if (response_headers == null) {
            response_headers = new HashMap<>();
            setDefaultResponseHeaders(response_headers);
        }
        response_headers.put(name, value);
    }

    @Override
    public void setResponseHeaders(Map<String, String> headers) {
        this.response_headers = headers;
    }

    @Override
    public void setReuestParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        this.params.put(key, value);
    }

    @Override
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getRequestURL();
    }

    @Override
    public void updateWebSocketProtocol() {
        String Sec_WebSocket_Key = getRequestHeader(HttpHeader.Req_Sec_WebSocket_Key);
        if (!StringUtil.isNullOrBlank(Sec_WebSocket_Key)) {
            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = SHAUtil.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = BASE64Util.byteArrayToBase64(key_array);
            setStatus(HttpStatus.C101);
            setResponseHeader(HttpHeader.Connection, "Upgrade");
            setResponseHeader(HttpHeader.Upgrade, "WebSocket");
            setResponseHeader(HttpHeader.Sec_WebSocket_Accept, acceptKey);
            updateWebSocketProtocol = true;
            return;
        }
        throw new IllegalArgumentException("illegal http header : empty Sec-WebSocket-Key");
    }

    @Override
    public void writeBinary(byte[] binary) {
        if (binaryBuffer == null) {
            binaryBuffer = new ByteArrayOutputStream(binary);
            return;
        }
        binaryBuffer.write(binary);
    }

    protected void setMethod(String method) {
        this.method = method;
    }

    protected ChannelContext getContext() {
        return context;
    }

    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    protected void clear(Collection<?> coll) {
        if (coll == null) {
            return;
        }
        coll.clear();
    }

    protected void clear(Map<?, ?> map) {
        if (map == null) {
            return;
        }
        map.clear();
    }

    protected HttpFuture reset(NioSocketChannel channel, int headerLimit, int bodyLimit) {
        this.binaryBuffer = null;
        this.bodyArray = null;
        this.boundary = null;
        this.contentLength = 0;
        this.contentType = null;
        this.clear(cookieList);
        this.clear(cookies);
        this.hasBodyContent = false;
        this.header_complete = false;
        this.headerLength = 0;
        this.headerLimit = headerLimit;
        this.host = null;
        this.method = null;
        this.parseFirstLine = true;
        this.readText = null;
        this.requestURI = null;
        this.requestURL = null;
        this.clear(response_headers);
        ;
        this.status = HttpStatus.C200;
        this.updateWebSocketProtocol = false;
        this.version = null;
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.context = channel.getContext();
        if (currentHeaderLine == null) {
            currentHeaderLine = new StringBuilder();
        } else {
            currentHeaderLine.setLength(0);
        }
        if (request_headers == null) {
            request_headers = new HashMap<>();
        } else {
            request_headers.clear();
        }
        if (params == null) {
            params = new HashMap<>();
        } else {
            params.clear();
        }
        setByteBuf(EmptyByteBuf.get());
        super.reset();
        return this;
    }

}
