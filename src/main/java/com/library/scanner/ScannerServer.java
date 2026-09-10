package com.library.scanner;

import com.library.controller.StockController;
import com.library.controller.StockController.ScanResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.concurrent.Executors;

/**
 * HTTPS server nhúng phục vụ trang quét mã vạch cho điện thoại.
 * - GET  /?t=TOKEN     : trang scanner.html
 * - POST /api/scan     : nhận {token, barcode, mode, qty} và nhập/xuất kho
 *
 * HTTPS bắt buộc vì trình duyệt chỉ cho phép camera trong secure context.
 * Dùng chứng chỉ self-signed đóng gói sẵn (scanner/scanner-keystore.p12).
 */
public class ScannerServer {

    /** Callback báo sự kiện quét về UI (được gọi ngoài EDT) */
    public interface ScanListener {
        void onScan(String barcode, boolean isImport, int qty, ScanResult result);
    }

    public static final int PORT = 8765;
    private static final String KEYSTORE_PATH = "/scanner/scanner-keystore.p12";
    private static final char[] KEYSTORE_PASS = "scanner123".toCharArray();

    private final StockController stockController = new StockController();
    private final ScanListener listener;
    private final Integer userId;

    private HttpsServer server;
    private String token;

    public ScannerServer(Integer userId, ScanListener listener) {
        this.userId = userId;
        this.listener = listener;
    }

    /** Khởi động server, trả về URL để điện thoại kết nối */
    public String start() throws Exception {
        if (server != null) return getUrl();

        // Token ngẫu nhiên cho mỗi phiên
        token = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        SSLContext ssl = createSslContext();
        server = HttpsServer.create(new InetSocketAddress(PORT), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(ssl));
        server.createContext("/", this::handleRoot);
        server.createContext("/html5-qrcode.min.js", this::handleJs);
        server.createContext("/api/scan", this::handleScan);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        return getUrl();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public boolean isRunning() {
        return server != null;
    }

    public String getUrl() {
        return "https://" + getLocalIp() + ":" + PORT + "/?t=" + token;
    }

    // ── Handlers ──

    private void handleRoot(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        try (InputStream in = getClass().getResourceAsStream("/scanner/scanner.html")) {
            if (in == null) {
                sendText(exchange, 500, "scanner.html not found");
                return;
            }
            byte[] body = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    private void handleJs(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        try (InputStream in = getClass().getResourceAsStream("/scanner/html5-qrcode.min.js")) {
            if (in == null) {
                sendText(exchange, 500, "html5-qrcode.min.js not found");
                return;
            }
            byte[] body = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }
    }

    private void handleScan(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        String reqToken = jsonString(body, "token");
        if (token == null || !token.equals(reqToken)) {
            sendJson(exchange, 403, "{\"ok\":false,\"message\":\"Sai token! Hãy quét lại QR trên máy tính.\"}");
            return;
        }

        String barcode = jsonString(body, "barcode");
        String mode = jsonString(body, "mode");
        int qty = jsonInt(body, "qty", 1);
        boolean isImport = !"export".equalsIgnoreCase(mode);

        ScanResult result = stockController.processScan(barcode, isImport, qty, userId);

        if (listener != null) {
            listener.onScan(barcode, isImport, qty, result);
        }

        String json = "{\"ok\":" + result.isSuccess()
                + ",\"status\":\"" + result.status() + "\""
                + ",\"message\":\"" + escapeJson(result.message()) + "\""
                + (result.document() != null
                    ? ",\"title\":\"" + escapeJson(result.document().getTitle()) + "\",\"stock\":" + result.newStock()
                    : "")
                + "}";
        sendJson(exchange, 200, json);
    }

    // ── Helpers ──

    private SSLContext createSslContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream in = getClass().getResourceAsStream(KEYSTORE_PATH)) {
            if (in == null) throw new IllegalStateException("Không tìm thấy keystore: " + KEYSTORE_PATH);
            ks.load(in, KEYSTORE_PASS);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, KEYSTORE_PASS);
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), null, null);
        return ssl;
    }

    /** Lấy IP LAN của máy (IPv4 trên interface đang định tuyến ra ngoài, ví dụ 192.168.x.x) */
    public static String getLocalIp() {
        // Cách 1: UDP "trick" — không gửi gói tin nào, chỉ để OS chọn interface định tuyến
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            InetAddress local = socket.getLocalAddress();
            if (local instanceof Inet4Address && !local.isLoopbackAddress() && !local.isAnyLocalAddress()) {
                return local.getHostAddress();
            }
        } catch (Exception ignored) {}

        // Cách 2 (fallback): quét interface, ưu tiên site-local, sau đó bất kỳ IPv4 nào không phải loopback
        String candidate = null;
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface ni = nets.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                var addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    var addr = addrs.nextElement();
                    if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress()) continue;
                    if (addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                    if (candidate == null) {
                        candidate = addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return candidate != null ? candidate : "localhost";
    }

    private void sendText(HttpExchange exchange, int code, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    /** Trích giá trị chuỗi từ JSON đơn giản: "key":"value" */
    private static String jsonString(String json, String key) {
        var m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                .matcher(json);
        if (!m.find()) return null;
        return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /** Trích giá trị số từ JSON đơn giản: "key":123 */
    private static int jsonInt(String json, String key, int defaultValue) {
        var m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(\\d+)")
                .matcher(json);
        if (!m.find()) return defaultValue;
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}
