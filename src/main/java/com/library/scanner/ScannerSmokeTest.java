package com.library.scanner;

/**
 * Smoke test tạm thời: chạy ScannerServer độc lập để kiểm thử bằng curl.
 */
public class ScannerSmokeTest {

    public static void main(String[] args) throws Exception {
        ScannerServer server = new ScannerServer(null, (barcode, isImport, qty, result) ->
                System.out.println("EVENT: " + result.status() + " | " + result.message()));
        String url = server.start();
        System.out.println("URL=" + url);
        Thread.sleep(120_000);
        server.stop();
    }
}
