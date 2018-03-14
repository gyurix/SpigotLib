package gyurix.mojang;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 */
public class MultipartUtility {
    private static final String LINE_FEED = "\r\n";
    private final String boundary;
    private String charset = "UTF-8";
    private HttpURLConnection con;
    private ByteArrayOutputStream outputStream;
    private PrintWriter writer;

    /**
     * This constructor initializes a new HTTP request with content type
     * is set to multipart/form-data
     *
     * @param requestURL - The url to which the request should be sent
     * @param method     - The type of the HTTP request, usually POST
     * @throws IOException - The type of the HTTP request
     */
    public MultipartUtility(String requestURL, String method) throws IOException {
        charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL(requestURL);
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        outputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  - Name attribute
     * @param uploadFile - the uploadable File
     * @throws IOException - The error what can happen during the operation
     */
    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"")
                .append(LINE_FEED);
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return A list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException - The ewrror what can happen during the operation
     */
    public String finish() throws IOException {
        writer.append(LINE_FEED).append("--").append(boundary).append("--").append(LINE_FEED);
        writer.close();
        byte[] data = outputStream.toByteArray();
        con.setRequestProperty("Content-Length", String.valueOf(data.length));
        con.getOutputStream().write(data);
        con.getOutputStream().flush();
        return IOUtils.toString(con.getInputStream(), Charset.forName("UTF-8"));
    }
}