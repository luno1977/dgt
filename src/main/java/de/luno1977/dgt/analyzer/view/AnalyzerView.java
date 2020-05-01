package de.luno1977.dgt.analyzer.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import de.luno1977.dgt.analyzer.impl.BoardFeedAnalyzer;
import de.luno1977.dgt.livechess.LiveChessException;

import javax.servlet.annotation.WebServlet;

/**
 * The main view contains a button and a click listener.
 */

@Route("")
@WebServlet(asyncSupported = true)
@PWA(name = "DGT Game Analyzer", shortName = "Analyzer")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Push(PushMode.MANUAL)
public class AnalyzerView extends HorizontalLayout {

    private final TextArea logView = new TextArea();

    private final Button connectButton;
    private final Button newGameButton;

    private final BoardFeedAnalyzer analyzer;
    private final NotationView notation;
    private final ChessBoardView board;

    public AnalyzerView() {
        //Proof that my servlet is used.
        //System.out.println(VaadinServlet.getCurrent().getServletName());

        analyzer = new BoardFeedAnalyzer();
        Icon connectIcon = new Icon(VaadinIcon.PLUG);
        connectIcon.setColor("red");

        notation = new NotationView();

        board = new ChessBoardView();
        board.setMinWidth("400px");
        board.setMinHeight("400px");

        connectButton = new Button(connectIcon);
        connectButton.getElement().setAttribute("title", "Connect DGT Board");
        newGameButton = new Button(new Icon(VaadinIcon.FILE_ADD));
        newGameButton.getElement().setAttribute("title", "New Game (from current e-board position)");

        connectButton.addClickListener( event -> {
            try {
                if (analyzer.isConnected()) {
                    analyzer.disconnect();
                    ((Icon) connectButton.getIcon()).setColor("red");
                    connectButton.getElement().setAttribute("title",
                            "Disconnected from DGT Board: Press to connect");
                    this.logView.clear();
                } else {
                    analyzer.connect();
                    ((Icon) connectButton.getIcon()).setColor("green");
                    connectButton.getElement().setAttribute("title",
                            "Connected to DGT Board: Press to disconnect");
                }
            } catch (LiveChessException lce) {
                Notification n = new Notification(lce.getMessage(), 6000, Notification.Position.BOTTOM_END);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                n.open();
            }
        });

        newGameButton.addClickListener(event -> analyzer.newGame());

        VerticalLayout left = new VerticalLayout();
        left.setMinHeight("400px");
        left.setMinWidth("400px");
        left.setSizeUndefined();
        left.add(new HorizontalLayout(connectButton, newGameButton), board);

        logView.setAutofocus(true);
        logView.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.add(notation, logView);

        add(left, right);
        setSizeFull();
    }
}