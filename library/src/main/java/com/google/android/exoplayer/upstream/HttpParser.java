package com.google.android.exoplayer.upstream;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Http报文处理类
 *
 * @author hellogv
 */
public class HttpParser {
    final static public String TAG = "HttpParser";
    final static public String HTTP_BODY_END = "\r\n\r\n";
    final static public String HTTP_RESPONSE_BEGIN = "HTTP/";
    final static public String HTTP_REQUEST_BEGIN = "GET ";
    final static private String RANGE_PARAMS = "Range: bytes=";
    final static private String RANGE_PARAMS_0 = "Range: bytes=0-";
    final static private String CONTENT_RANGE_PARAMS = "Content-Range: bytes ";
    private static final int HEADER_BUFFER_LENGTH_MAX = 1024 * 50;
    private byte[] headerBuffer = new byte[HEADER_BUFFER_LENGTH_MAX];
    private int headerBufferLength = 0;

    public HttpParser() {
    }

    public void clearHttpBody() {
        headerBuffer = new byte[HEADER_BUFFER_LENGTH_MAX];
        headerBufferLength = 0;
    }

    /**
     * 获取Request报文
     *
     * @param source
     * @param length
     * @return
     */
    public byte[] getRequestBody(byte[] source, int length) {
        List<byte[]> httpRequest = getHttpBody(HTTP_REQUEST_BEGIN,
                HTTP_BODY_END,
                source,
                length);
        if (httpRequest.size() > 0) {
            return httpRequest.get(0);
        }
        return null;
    }

    /**
     * 获取ProxyResponse
     *
     * @param source
     * @param length
     */
    public ProxyResponse getProxyResponse(byte[] source, int length) {
        List<byte[]> httpResponse = getHttpBody(HTTP_RESPONSE_BEGIN,
                HTTP_BODY_END,
                source,
                length);

        if (httpResponse.size() == 0)
            return null;

        ProxyResponse result = new ProxyResponse();

        //获取Response正文
        result._body = httpResponse.get(0);
        String text = new String(result._body);

//		Log.i(TAG + "<---", text);
        //获取二进制数据
        if (httpResponse.size() == 2)
            result._other = httpResponse.get(1);

        // http://blog.chinaunix.net/uid-11959329-id-3088466.html
        //样例：Content-Range: bytes 2267097-257405191/257405192
        try {
            // 获取起始位置
            String currentPosition = getSubString(text, CONTENT_RANGE_PARAMS, "-");
            result._currentPosition = Integer.valueOf(currentPosition);

            // 获取最终位置
            String startStr = CONTENT_RANGE_PARAMS + currentPosition + "-";
            String duration = getSubString(text, startStr, "/");
            result._duration = Integer.valueOf(duration);
        } catch (Exception ex) {
            Log.e(TAG, "===============error message");
        }
        Log.e(TAG, "==== the response content startIndex " + result._currentPosition + " endIndex " + result._duration);
        return result;
    }

    /**
     * 替换Request报文中的Range位置,"Range: bytes=0-" -> "Range: bytes=XXX-"
     *
     * @param requestStr
     * @param position
     * @return
     */
    public String modifyRequestRange(String requestStr, int position) {
        String str = getSubString(requestStr, RANGE_PARAMS, "-");
        str = str + "-";
        String result = requestStr.replaceAll(str, position + "-");
        return result;
    }

    protected String getSubString(String source, String startStr, String endStr) {
        int startIndex = source.indexOf(startStr) + startStr.length();
        int endIndex = source.indexOf(endStr, startIndex);
        return source.substring(startIndex, endIndex);
    }

    private List<byte[]> getHttpBody(String beginStr, String endStr, byte[] source, int length) {
        if ((headerBufferLength + length) >= headerBuffer.length) {
            clearHttpBody();
        }

        System.arraycopy(source, 0, headerBuffer, headerBufferLength, length);
        headerBufferLength += length;

        List<byte[]> result = new ArrayList<byte[]>();
        String responseStr = new String(headerBuffer);
        if (responseStr.contains(beginStr)
                && responseStr.contains(endStr)) {

            int startIndex = responseStr.indexOf(beginStr, 0);
            int endIndex = responseStr.indexOf(endStr, startIndex);
            endIndex += endStr.length();

            byte[] header = new byte[endIndex - startIndex];
            System.arraycopy(headerBuffer, startIndex, header, 0, header.length);
            result.add(header);

            if (headerBufferLength > header.length) {//还有数据
                byte[] other = new byte[headerBufferLength - header.length];
                System.arraycopy(headerBuffer, header.length, other, 0, other.length);
                result.add(other);
            }
            clearHttpBody();
        }

        return result;
    }

    static public class ProxyResponse {
        public byte[] _body;
        public byte[] _other;
        public long _currentPosition;
        public long _duration;
    }

}
