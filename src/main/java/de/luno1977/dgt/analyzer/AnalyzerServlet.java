package de.luno1977.dgt.analyzer;

import com.vaadin.flow.server.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*", asyncSupported = true)
public class AnalyzerServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener {
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        System.out.println("Servlet AnalyzerServlet is initialized");
        getService().addSessionInitListener(this);
        getService().addSessionDestroyListener(this);
    }

    @Override
    public void sessionInit(SessionInitEvent event)
        throws ServiceException {
        System.out.println("Session in  AnalyzerServlet is initialized");
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        System.out.println("Session in  AnalyzerServlet is closed");
    }
}
