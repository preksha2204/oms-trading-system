package com.oms.envcheck;

import java.io.*;
import java.net.Socket;

/**
 * OMS Environment Check Program
 * Validates all prerequisites for running the OMS Trading System.
 * Usage: java -cp "common-lib.jar;quickfixj-core.jar;quickfixj-messages-fix44.jar;
 *               mysql-connector-j.jar;kafka-clients.jar" com.oms.envcheck.EnvCheck
 */
public class EnvCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        OMS Trading System — Environment Check            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        checkJava();
        checkQuickFixJ();
        checkMysqlDriver();
        checkKafkaClient();
        checkSpringBoot();
        checkNodeJs();
        checkAngularCli();
        checkMysqlPort();
        checkKafkaPort();
        checkDiskSpace();
        checkMemory();

        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("  Results: %d passed / %d failed%n", passed, failed);
        if (failed == 0) {
            System.out.println("  ✅  Environment is READY for OMS Trading System");
        } else {
            System.out.println("  ❌  Fix the issues above before starting the system");
            System.exit(1);
        }
        System.out.println("══════════════════════════════════════════════════════════");
    }

    static void checkJava() {
        String version = System.getProperty("java.version");
        String vendor  = System.getProperty("java.vendor");
        try {
            int major = Integer.parseInt(version.split("[._]")[0]);
            boolean ok = major >= 17;
            printResult(ok, "Java Runtime",
                    String.format("%s (%s) — required: 17+", version, vendor));
        } catch (NumberFormatException e) {
            printResult(false, "Java Runtime", "Could not parse version: " + version);
        }
    }

    static void checkQuickFixJ() {
        try {
            Class.forName("quickfix.Session");
            Class.forName("quickfix.fix44.NewOrderSingle");
            Class.forName("quickfix.SocketAcceptor");
            printResult(true, "QuickFIX/J",
                    "Core + FIX44 messages + SocketAcceptor found");
        } catch (ClassNotFoundException e) {
            printResult(false, "QuickFIX/J",
                    "Not found: " + e.getMessage() +
                    "\n         → Add quickfixj-core and quickfixj-messages-fix44 to classpath");
        }
    }

    static void checkMysqlDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            printResult(true, "MySQL JDBC Driver", "mysql-connector-j found");
        } catch (ClassNotFoundException e) {
            printResult(false, "MySQL JDBC Driver",
                    "Not found → add mysql-connector-j to classpath");
        }
    }

    static void checkKafkaClient() {
        try {
            Class.forName("org.apache.kafka.clients.producer.KafkaProducer");
            Class.forName("org.apache.kafka.clients.consumer.KafkaConsumer");
            printResult(true, "Kafka Client", "kafka-clients found");
        } catch (ClassNotFoundException e) {
            printResult(false, "Kafka Client",
                    "Not found → add kafka-clients to classpath");
        }
    }

    static void checkSpringBoot() {
        try {
            Class<?> sc = Class.forName("org.springframework.boot.SpringApplication");
            Package pkg  = sc.getPackage();
            String ver   = pkg != null ? pkg.getImplementationVersion() : "unknown";
            printResult(true, "Spring Boot", "Version: " + ver);
        } catch (ClassNotFoundException e) {
            printResult(false, "Spring Boot", "Not found on classpath");
        }
    }

    static void checkNodeJs() {
        try {
            Process p = Runtime.getRuntime().exec(isWindows() ? "node.exe --version" : "node --version");
            String ver = readProcess(p).trim();
            boolean ok = ver.startsWith("v");
            printResult(ok, "Node.js", ok ? ver : "Not found");
        } catch (IOException e) {
            printResult(false, "Node.js", "Not found — Angular UI will not work");
        }
    }

    static void checkAngularCli() {
        try {
            Process p = Runtime.getRuntime().exec(isWindows() ? "ng.cmd version" : "ng version");
            String out = readProcess(p).trim();
            boolean ok = out.contains("Angular CLI");
            printResult(ok, "Angular CLI", ok ? "Installed" : "Not found — run: npm install -g @angular/cli");
        } catch (IOException e) {
            printResult(false, "Angular CLI", "Not found — run: npm install -g @angular/cli");
        }
    }

    static void checkMysqlPort() {
        checkPort("MySQL", "localhost", 3306);
    }

    static void checkKafkaPort() {
        checkPort("Kafka Broker", "localhost", 9092);
    }

    static void checkPort(String name, String host, int port) {
        try (Socket s = new Socket(host, port)) {
            printResult(true, name + " Port", host + ":" + port + " reachable");
        } catch (IOException e) {
            printResult(false, name + " Port",
                    host + ":" + port + " NOT reachable — is the service running?");
        }
    }

    static void checkDiskSpace() {
        File root  = new File(".");
        long freeGb = root.getFreeSpace() / (1024 * 1024 * 1024);
        boolean ok  = freeGb >= 10;
        printResult(ok, "Disk Space",
                freeGb + " GB free (recommended: ≥10 GB for logs, FIX store, MySQL data)");
    }

    static void checkMemory() {
        long maxMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        boolean ok = maxMb >= 2048;
        printResult(ok, "JVM Heap",
                maxMb + " MB max (recommended: ≥2048 MB, use -Xmx8g for production)");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static void printResult(boolean ok, String component, String detail) {
        String icon = ok ? "✅" : "❌";
        String tag  = ok ? " OK  " : "FAIL ";
        System.out.printf("  %s [%s] %-28s %s%n", icon, tag, component, detail);
        if (ok) passed++; else failed++;
    }

    static String readProcess(Process p) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
