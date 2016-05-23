/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.google.android.exoplayer.upstream;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.HttpParser.ProxyResponse;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Predicate;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * A {@link HttpDataSource} that uses Android's {@link HttpURLConnection}.
 * <p/>
 * By default this implementation will not follow cross-protocol redirects (i.e. redirects from
 * HTTP to HTTPS or vice versa). Cross-protocol redirects can be enabled by using the
 * {@link #DefaultHttpDataSource(String, Predicate, TransferListener, int, int, boolean)}
 * constructor and passing {@code true} as the final argument.
 */
public class DefaultSocketDataSource implements HttpDataSource {

    /**
     * The default connection timeout, in milliseconds.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 8 * 1000;
    /**
     * The default read timeout, in milliseconds.
     */
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 8 * 1000;

    private static final int MAX_REDIRECTS = 20; // Same limit as okhttp.
    private static final String TAG = "DefaultSocketDataSource";
    private static final Pattern CONTENT_RANGE_HEADER =
            Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$");
    private static final AtomicReference<byte[]> skipBufferReference = new AtomicReference<>();

    private final boolean allowCrossProtocolRedirects;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final String userAgent;
    private final Predicate<String> contentTypePredicate;
    private final TransferListener listener;
    Map<String, String> responseHeaders;
    private DataSpec dataSpec;
    private Socket socket;
    private InputStream inputStream;
    private boolean opened;
    private long bytesToSkip;
    private long bytesToRead;
    private long bytesSkipped;
    private long bytesRead;
    private String proxyHost;
    private int proxyPort;
    private String cookies;
    private ProxyResponse proxyResponse = null;

    /**
     * @param userAgent                   The User-Agent string that should be used.
     * @param contentTypePredicate        An optional {@link Predicate}. If a content type is
     *                                    rejected by the predicate then a {@link HttpDataSource.InvalidContentTypeException} is
     *                                    thrown from {@link #open(DataSpec)}.
     * @param listener                    An optional listener.
     * @param connectTimeoutMillis        The connection timeout, in milliseconds. A timeout of zero is
     *                                    interpreted as an infinite timeout. Pass {@link #DEFAULT_CONNECT_TIMEOUT_MILLIS} to use
     *                                    the default value.
     * @param readTimeoutMillis           The read timeout, in milliseconds. A timeout of zero is interpreted
     *                                    as an infinite timeout. Pass {@link #DEFAULT_READ_TIMEOUT_MILLIS} to use the default value.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *                                    to HTTPS and vice versa) are enabled.
     */
    public DefaultSocketDataSource(Map<String, String> headers, Predicate<String> contentTypePredicate,
                                   TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis,
                                   boolean allowCrossProtocolRedirects, String proxyHost, int proxyPort) {
        this.userAgent = Assertions.checkNotEmpty(headers.get("User-Agent"));
        this.cookies = headers.get("Cookie");
        this.contentTypePredicate = contentTypePredicate;
        this.listener = listener;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.responseHeaders = new HashMap<String, String>();
    }

    @Override
    public String getUri() {
        return dataSpec == null ? null : dataSpec.uri.toString();
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return null;
    }

    @Override
    public void setRequestProperty(String name, String value) {
    }

    @Override
    public void clearRequestProperty(String name) {
    }

    @Override
    public void clearAllRequestProperties() {
    }

    @Override
    public long open(DataSpec dataSpec) throws HttpDataSourceException {
        this.dataSpec = dataSpec;
        this.bytesRead = 0;
        this.bytesSkipped = 0;
        try {
            makeConnection(dataSpec);
        } catch (IOException e) {
            throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e,
                    dataSpec);
        }

        int responseCode;
        try {
            responseCode = getResponseCode();
        } catch (IOException e) {
            closeConnectionQuietly();
            throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e,
                    dataSpec);
        }

        // Check for a valid response code.
        if (responseCode < 200 || responseCode > 299) {
            Map<String, List<String>> headers = null;//connection.getHeaderFields();
            closeConnectionQuietly();
            throw new InvalidResponseCodeException(responseCode, headers, dataSpec);
        }

        // Check for a valid content type.
        //       String contentType = getContentType();
        //       if (contentTypePredicate != null && !contentTypePredicate.evaluate(contentType)) {
        //           closeConnectionQuietly();
        //           throw new InvalidContentTypeException(contentType, dataSpec);
        //       }

        // If we requested a range starting from a non-zero position and received a 200 rather than a
        // 206, then the server does not support partial requests. We'll need to manually skip to the
        // requested position.
        bytesToSkip = responseCode == 200 && dataSpec.position != 0 ? dataSpec.position : 0;

        // Determine the length of the data to be read, after skipping.
        if ((dataSpec.flags & DataSpec.FLAG_ALLOW_GZIP) == 0) {
            long contentLength = getContentLength();
            bytesToRead = dataSpec.length != C.LENGTH_UNBOUNDED ? dataSpec.length
                    : contentLength != C.LENGTH_UNBOUNDED ? contentLength - bytesToSkip
                    : C.LENGTH_UNBOUNDED;
        } else {
            // Gzip is enabled. If the server opts to use gzip then the content length in the response
            // will be that of the compressed data, which isn't what we want. Furthermore, there isn't a
            // reliable way to determine whether the gzip was used or not. Always use the dataSpec length
            // in this case.
            bytesToRead = dataSpec.length;
        }

//        try {
//            inputStream = socket.getInputStream();
//        } catch (IOException e) {
//            closeConnectionQuietly();
//            throw new HttpDataSourceException(e, dataSpec);
//        }

        opened = true;
        if (listener != null) {
            listener.onTransferStart();
        }

        return bytesToRead;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws HttpDataSourceException {
        try {
            skipInternal();
            return readInternal(buffer, offset, readLength);
        } catch (IOException e) {
            throw new HttpDataSourceException(e, dataSpec);
        }
    }

    @Override
    public void close() throws HttpDataSourceException {
        try {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new HttpDataSourceException(e, dataSpec);
                }
            }
        } finally {
            inputStream = null;
            closeConnectionQuietly();
            if (opened) {
                opened = false;
                if (listener != null) {
                    listener.onTransferEnd();
                }
            }
        }
    }

    /**
     * Establishes a connection, following redirects to do so where permitted.
     */
    private void makeConnection(DataSpec dataSpec) throws IOException {
        URL url = new URL(dataSpec.uri.toString());
        byte[] postBody = dataSpec.postBody;
        long position = dataSpec.position;
        long length = dataSpec.length;
        boolean allowGzip = (dataSpec.flags & DataSpec.FLAG_ALLOW_GZIP) != 0;

        if (!allowCrossProtocolRedirects) {
            // HttpURLConnection disallows cross-protocol redirects, but otherwise performs redirection
            // automatically. This is the behavior we want, so use it.
            makeConnection(
                    url, postBody, position, length, allowGzip, true /* followRedirects */);
        }
    }

    /**
     * Configures a connection and opens it.
     *
     * @param url             The url to connect to.
     * @param postBody        The body data for a POST request.
     * @param position        The byte offset of the requested data.
     * @param length          The length of the requested data, or {@link C#LENGTH_UNBOUNDED}.
     * @param allowGzip       Whether to allow the use of gzip.
     * @param followRedirects Whether to follow redirects.
     */
    private void makeConnection(URL url, byte[] postBody, long position,
                                long length, boolean allowGzip, boolean followRedirects) throws IOException {
        Log.d(TAG, "====in makeConnection " + proxyHost + " " + proxyPort + " " + position + " " + length + "\n"
                + cookies + " " + userAgent + " " + url);

        // Construct request headers
        String redirectUrl = "";
        String requestUrl = "";
        String remoteHost = "";
        int remotePort = -1;
        try {
            redirectUrl = url.toURI().toString();
            remoteHost = url.toURI().getHost();
            remotePort = url.toURI().getPort();
        } catch (URISyntaxException var5) {
            Log.e("DefaultHttpDataSource", "URISyntaxException");
        }
//        redirectUrl = Utils.getRedirectUrl(redirectUrl);
        if (remotePort != -1) {
            requestUrl = redirectUrl.replace("http://" + remoteHost + ":" + remotePort, "");
        } else {
            requestUrl = redirectUrl.replace("http://" + remoteHost, "");
        }

        try {
            if (TextUtils.isEmpty(proxyHost)) {
                socket = new Socket();
            } else {
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("140.207.47.119"), 10014);
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, socketAddress);
                socket = new Socket(proxy);
            }

            InetSocketAddress serverAddress;
            if (remotePort != -1) {// URL带Port
                serverAddress = new InetSocketAddress(remoteHost, remotePort);// 使用默认端口
            } else {// URL不带Port
                serverAddress = new InetSocketAddress(remoteHost, 80);// 使用80端口
            }
            socket.setKeepAlive(true);
            socket.connect(serverAddress);
            //socket.setSoTimeout(readTimeoutMillis);

            //
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.print("GET " + requestUrl + " HTTP/1.1\r\n");
            out.print("Host: " + remoteHost + "\r\n");
            out.print("Cookie: " + cookies + "\r\n");
            out.print("User-Agent: " + userAgent + "\r\n");
            out.print("Accept-Encoding: " + "gzip,deflate" + "\r\n");

            if (!(position == 0 && length == C.LENGTH_UNBOUNDED)) {
                String rangeRequest = "bytes=" + position + "-";
                if (length != C.LENGTH_UNBOUNDED) {
                    rangeRequest += (position + length - 1);
                }
                out.print("Range: " + rangeRequest + "\r\n");
            }
            out.print("\r\n");
            out.flush();

            inputStream = socket.getInputStream();
            // Get response headers.
   /*         int bytes_read = -1;
            HttpParser httpParser = new HttpParser();
            byte[] remote_reply = new byte[1024 * 50];
            if ((bytes_read = inputStream.read(remote_reply)) != -1) {
                proxyResponse = httpParser.getProxyResponse(remote_reply, bytes_read);
                getRequset(new String(proxyResponse._body));
            }
            String aa = new String(remote_reply);
            Log.e(TAG, "====end make connection " + bytes_read);
*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
//        connection.setReadTimeout(readTimeoutMillis);
    }

    private int getResponseCode() throws IOException {
        return 200;//(String) parseRequestHeader(buffer).get("Remote-Host");
    }

    private String getContentType() {
        return (String) responseHeaders.get("Content-Type");
    }

    /**
     * Attempts to extract the length of the content from the response headers of an open connection.
     *
     * @param connection The open connection.
     * @return The extracted length, or {@link C#LENGTH_UNBOUNDED}.
     */
    private long getContentLength() {
        long contentLength = C.LENGTH_UNBOUNDED;
        String contentLengthHeader = (String) responseHeaders.get("Content-Length");
        if (!TextUtils.isEmpty(contentLengthHeader)) {
            try {
                contentLength = Long.parseLong(contentLengthHeader);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Unexpected Content-Length [" + contentLengthHeader + "]");
            }
        }
//        String contentRangeHeader = connection.getHeaderField("Content-Range");
//        if (!TextUtils.isEmpty(contentRangeHeader)) {
//            Matcher matcher = CONTENT_RANGE_HEADER.matcher(contentRangeHeader);
//            if (matcher.find()) {
//                try {
//                    long contentLengthFromRange =
//                            Long.parseLong(matcher.group(2)) - Long.parseLong(matcher.group(1)) + 1;
//                    if (contentLength < 0) {
//                        // Some proxy servers strip the Content-Length header. Fall back to the length
//                        // calculated here in this case.
//                        contentLength = contentLengthFromRange;
//                    } else if (contentLength != contentLengthFromRange) {
//                        // If there is a discrepancy between the Content-Length and Content-Range headers,
//                        // assume the one with the larger value is correct. We have seen cases where carrier
//                        // change one of them to reduce the size of a request, but it is unlikely anybody would
//                        // increase it.
//                        Log.w(TAG, "Inconsistent headers [" + contentLengthHeader + "] [" + contentRangeHeader
//                                + "]");
//                        contentLength = Math.max(contentLength, contentLengthFromRange);
//                    }
//                } catch (NumberFormatException e) {
//                    Log.e(TAG, "Unexpected Content-Range [" + contentRangeHeader + "]");
//                }
//            }
//        }
        return contentLength;
    }

    /**
     * Skips any bytes that need skipping. Else does nothing.
     * <p/>
     * This implementation is based roughly on {@code libcore.io.Streams.skipByReading()}.
     *
     * @throws InterruptedIOException If the thread is interrupted during the operation.
     * @throws EOFException           If the end of the input stream is reached before the bytes are skipped.
     */
    private void skipInternal() throws IOException {
        if (bytesSkipped == bytesToSkip) {
            return;
        }

        // Acquire the shared skip buffer.
        byte[] skipBuffer = skipBufferReference.getAndSet(null);
        if (skipBuffer == null) {
            skipBuffer = new byte[4096];
        }

        Log.e(TAG, "=====in skipInternal " + bytesSkipped + " " + bytesToSkip);
        while (bytesSkipped != bytesToSkip) {
            int readLength = (int) Math.min(bytesToSkip - bytesSkipped, skipBuffer.length);
            int read = inputStream.read(skipBuffer, 0, readLength);
            if (Thread.interrupted()) {
                throw new InterruptedIOException();
            }
            if (read == -1) {
                throw new EOFException();
            }
            bytesSkipped += read;
            if (listener != null) {
                listener.onBytesTransferred(read);
            }
        }

        // Release the shared skip buffer.
        skipBufferReference.set(skipBuffer);
    }

    /**
     * Reads up to {@code length} bytes of data and stores them into {@code buffer}, starting at
     * index {@code offset}.
     * <p/>
     * This method blocks until at least one byte of data can be read, the end of the opened range is
     * detected, or an exception is thrown.
     *
     * @param buffer     The buffer into which the read data should be stored.
     * @param offset     The start offset into {@code buffer} at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or {@link C#RESULT_END_OF_INPUT} if the end of the opened
     * range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    private int readInternal(byte[] buffer, int offset, int readLength) throws IOException {
        readLength = bytesToRead == C.LENGTH_UNBOUNDED ? readLength
                : (int) Math.min(readLength, bytesToRead - bytesRead);
        if (readLength == 0) {
            // We've read all of the requested data.
            return C.RESULT_END_OF_INPUT;
        }

        //m3u8 maybe < 50K
/*        int read = 0;
        if (offset < proxyResponse._other.length) {
        readLength = proxyResponse._other.length <= readLength ? proxyResponse._other.length : readLength;
        readLength = readLength - offset;
        System.arraycopy(proxyResponse._other, offset, buffer, 0, readLength);
        read = readLength;
}*/
        int read = inputStream.read(buffer, offset, readLength);
//        Log.e(TAG, "=====readInternal 1 " + read + " " + offset + " " + readLength + " ");
        //String temp = new String(buffer);
        if (read == -1) {
            if (bytesToRead != C.LENGTH_UNBOUNDED && bytesToRead != bytesRead) {
                // The server closed the connection having not sent sufficient data.
                throw new EOFException();
            }
            return C.RESULT_END_OF_INPUT;
        }

        bytesRead += read;
        if (listener != null) {
            listener.onBytesTransferred(read);
        }
        return read;
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private void closeConnectionQuietly() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting", e);
            }
            socket = null;
        }
    }

    /**
     * 传入一个Socket对象，解析出通过该对象传入这个链接中的所有HTTP请求信息
     *
     * @param client
     */
    private void getRequset(String headers) {
        try {
            //InputStream in = client.getInputStream();
            //InputStreamReader reader = new InputStreamReader(in);
            //BufferedReader bd = new BufferedReader(reader);//三重封装
            BufferedReader bd = new BufferedReader(new StringReader(headers));
            String t = null;

            while ((t = bd.readLine()) != null) {
                System.out.println(t);
                if (t.startsWith("Connection")) {
                    break;
                }
                parser(t);//解析每一句的方法
            }
        } catch (Exception ef) {
            ef.printStackTrace();
            System.out.println("getRequset error--->");
        }
    }

    /**
     * 传入HTTP请求中需要解析的某一句 解析该句，并请放入对应的Request对象中
     *
     * @param s
     */
    private void parser(String s) {
        if (s.startsWith("HTTP")) {
            int index = s.indexOf("HTTP");
            System.out.println("index--->" + index);
//            String uri = s.substring(3 + 1, index - 1);// 用index-1可以去掉连接中的空格
//            System.out.println("uri--->" + uri);
//            request.setRequestURI(uri);
        } else if (s.startsWith("Date:")) {
            String date = s.substring("Date:".length() + 1);
            System.out.println("Date--->" + date);
            responseHeaders.put("Date", date);
        } else if (s.startsWith("Content-Length:")) {
            String length = s.substring("Content-Length:".length() + 1);
            System.out.println("Length--->" + length);
            responseHeaders.put("Content-Length", length);
        } else if (s.startsWith("Content-Type:")) {
            String type = s.substring("Content-Type:".length() + 1);
            System.out.println("Type--->" + type);
            responseHeaders.put("Content-Type", type);
        }
    }
}

