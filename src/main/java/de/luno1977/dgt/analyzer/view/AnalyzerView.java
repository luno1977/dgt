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
import de.luno1977.dgt.livechess.EBoardEventFeed;
import de.luno1977.dgt.livechess.LiveChess;
import de.luno1977.dgt.livechess.WebSocketFeed;
import de.luno1977.dgt.livechess.WebSocketResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

    private final TextArea text = new TextArea();
    private Disposable eventsDisposable;
    private EBoardEventFeed eBoardEventFeed;
    private final ChessBoardView board;
    private final Button connectButton;
    private final Button disconnectButton;

    public AnalyzerView() {
        //Proof that my servlet is used.
        //System.out.println(VaadinServlet.getCurrent().getServletName());

        board = new ChessBoardView();
        board.setMinWidth("400px");
        board.setMinHeight("400px");

        connectButton = new Button("Connect DGT Board");
        disconnectButton = new Button("Disconnect DGT Board");

        connectButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            connectToBoard(UI.getCurrent());
            disconnectButton.setEnabled(true);
            connectButton.setEnabled(false);
        });

        disconnectButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            this.text.clear();
            LiveChess.getInstance().unsubscribe(eBoardEventFeed);
            disconnectButton.setEnabled(false);
            connectButton.setEnabled(true);
        });

        disconnectButton.setEnabled(false);

        VerticalLayout left = new VerticalLayout();
        left.setMinHeight("400px");
        left.setMinWidth("400px");
        left.setSizeUndefined();
        left.add(board, connectButton, disconnectButton);

        text.setAutofocus(true);
        text.setSizeFull();

        VerticalLayout right = new VerticalLayout();
        right.add(text);

        add(left, right);
        setSizeFull();
    }

    private void connectToBoard(UI ui) {

        LiveChess liveChess = LiveChess.getInstance();
        WebSocketResponse.EBoardsResponse eBoards = liveChess.getEBoards();

        ui.access(() -> {
            text.setValue(text.getValue() + "DGT request: " + eBoards.toString() + "\n");
            ui.push();
        });

        eBoardEventFeed = liveChess.subscribe(eBoards.getParam().get(0));
        Observable<WebSocketFeed.EBoardEvent> events = eBoardEventFeed.events();
        eventsDisposable = events.observeOn(Schedulers.single()).subscribe(
                message -> {
                    ui.access(() -> {
                        //text.setValue(text.getValue() + "\n" + "EBoardEventFeed: " + message);
                        board.present(message.getParam().getBoard());
                        ui.push();
                    });
                },
                error -> {
                    throw new RuntimeException(error);
                },
                () -> ui.access(() -> {
                    text.setValue(text.getValue() + "\n Completed: " + eBoardEventFeed);
                    ui.push();
                })
        );
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LiveChess.getInstance().unsubscribe(eBoardEventFeed);
        eventsDisposable.dispose();
    }
}