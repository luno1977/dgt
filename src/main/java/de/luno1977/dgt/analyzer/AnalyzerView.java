package de.luno1977.dgt.analyzer;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import de.luno1977.dgt.livechess.EBoardsResponse;
import de.luno1977.dgt.livechess.LiveChessConnector;
import de.luno1977.dgt.livechess.WebSocketCommunicator;

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
public class AnalyzerView extends VerticalLayout {

    public static LiveChessConnector liveChessConnection;

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
        text.setHeight("50%");
        text.setWidthFull();
        text.setAutofocus(true);

        //Proof that my servlet is used.
        //System.out.println(VaadinServlet.getCurrent().getServletName());

        Button connectButton = new Button("Connect DGT Board");
        connectButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            connectToBoard(UI.getCurrent(), this);
        });

        Button newMessage = new Button("generate messages");
        newMessage.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            Button source = event.getSource();
            if (source.getText().startsWith("generate")) {
                isRunning = true;
                startMessageGeneration(UI.getCurrent(), this);
                source.setText("stop message generation");
            } else {
                isRunning = false;
                source.setText("generate messages");
            }
        });

        add(text, connectButton);
        setSizeFull();
    }

    private void connectToBoard(UI ui, HasComponents view) {

        WebSocketCommunicator boardCommunicator = WebSocketCommunicator.getInstance();
        EBoardsResponse eBoards = boardCommunicator.getEBoards();

        ui.access(() -> {
            view.add(new Div(new Text("DGT request: " + eBoards.toString())));
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

    private void startMessageGeneration(UI ui, HasComponents view) {
        messageGenerator = () -> {
            while (isRunning) {
                ui.access(() -> {
                    view.add(new Div(new Text(String.valueOf(System.currentTimeMillis()))));
                });

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    System.out.println("Error Interrupted");
                }

                ui.access(ui::push);
            }
        };

        new Thread(messageGenerator).start();
    }
}