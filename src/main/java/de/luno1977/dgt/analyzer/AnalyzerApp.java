package de.luno1977.dgt.analyzer;

import com.vaadin.flow.server.*;
import de.luno1977.dgt.server.JettyServer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

public class AnalyzerApp {

    public static void main(String[] args) throws Exception {
        JettyServer server = new JettyServer();
        server.start(8080, 8443);
    }

    @WebServlet(urlPatterns = "/*", asyncSupported = true)
    public static class AnalyzerServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener {
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            System.out.println("Servlet AnalyzerServlet is initialized");
            getService().addSessionInitListener(this);
            getService().addSessionDestroyListener(this);
        }

        @Override
        public void sessionInit(SessionInitEvent event) throws ServiceException {
            System.out.println("Session in  AnalyzerServlet is initialized");
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            System.out.println("Session in  AnalyzerServlet is closed");
        }
    }
}
