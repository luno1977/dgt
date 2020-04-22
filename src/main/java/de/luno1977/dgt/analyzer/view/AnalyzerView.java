package de.luno1977.dgt.analyzer.view;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import de.luno1977.dgt.livechess.LiveChess;
import de.luno1977.dgt.livechess.WebSocketResponse;

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

    private TextArea text = new TextArea();
        private Runnable messageGenerator;
    private boolean isRunning = false;

    private class DGTMessageHandler {
        final UI ui;
        final HasComponents components;

        public DGTMessageHandler(UI ui, HasComponents components) {
            this.ui = ui;
            this.components = components;
        }

        public void handleMessage(String message) {
            System.out.println("Board Change: " + message);
            ui.access(() -> {
                //components.add(new Div(new Text("DGT response: " + message)));
                text.setValue(text.getValue() + "\n" + "DGT response: " + message);
                ui.push();
            });
        }
    }

    public AnalyzerView() {
        //Proof that my servlet is used.
        //System.out.println(VaadinServlet.getCurrent().getServletName());

        ChessBoardView board = new ChessBoardView();
        board.setMinWidth("400px");
        board.setMinHeight("400px");

        Button connectButton = new Button("Connect DGT Board");
        connectButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            connectToBoard(UI.getCurrent(), this);
        });

        VerticalLayout left = new VerticalLayout();
        left.setMinHeight("400px");
        left.setMinWidth("400px");
        left.setSizeUndefined();
        left.add(board, connectButton);

        text.setAutofocus(true);
        text.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.add(text);

        add(left, right);
        setSizeFull();
    }

    private void connectToBoard(UI ui, HasComponents view) {

        WebSocketResponse.EBoardsResponse eBoards = LiveChess.getInstance().getEBoards();

        ui.access(() -> {
            text.setValue(text.getValue() + "DGT request: " + eBoards.toString() + "\n");
            ui.push();
        });

        /*
        final String subscribeMessage = "{\n" +
                "    \"call\": \"subscribe\",\n" +
                "    \"id\": 42,\n" +
                "    \"param\": {\n" +
                "        \"feed\": \"eboardevent\",\n" +
                "        \"id\": 7,\n" +
                "        \"param\": {\n" +
                "            \"serialnr\": \"40195\"\n" +
                "        }\n" +
                "    }\n" +
                "}";

        ui.access(() -> {
            view.add(new Div(new Text("DGT request: " + subscribeMessage)));
            ui.push();
        });

        liveChessConnection.sendMessage(subscribeMessage);
        */
    }
}