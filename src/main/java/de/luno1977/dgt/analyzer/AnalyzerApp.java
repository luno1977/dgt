package de.luno1977.dgt.analyzer;

public class AnalyzerApp {

    public static void main(String[] args) throws Exception {
        JettyServer server = new JettyServer();
        server.start(8080, 8443);
    }
}
