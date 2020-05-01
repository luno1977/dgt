package de.luno1977.dgt.analyzer.impl;

import de.luno1977.dgt.livechess.EBoardEventFeed;
import de.luno1977.dgt.livechess.LiveChess;
import de.luno1977.dgt.livechess.WebSocketFeed;
import de.luno1977.dgt.livechess.WebSocketResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class BoardFeedAnalyzer  {

    //EBoard management
    private final LiveChess liveChess;
    private Disposable eventsDisposable;
    private EBoardEventFeed eBoardEventFeed;
    private boolean connected = false;

    //Game
    private final List<AnalyzerGame> games;
    private AnalyzerGame currentGame;
    //private String currentBoardRep;

    public BoardFeedAnalyzer() {
        liveChess = LiveChess.getInstance();
        games = new ArrayList<>();
    }

    public void newGame() {
        if (connected) {

        }
    }

    public void connect() {
        liveChess.connect();
        WebSocketResponse.EBoardsResponse eBoards = liveChess.getEBoards();

        eBoardEventFeed = liveChess.subscribe(eBoards.getParam().get(0));
        Observable<WebSocketFeed.EBoardEvent> events = eBoardEventFeed.events();
        eventsDisposable = events.observeOn(Schedulers.single()).subscribe(
                message -> System.out.println("Analyzer:" + message.getParam().getBoard()),
                error -> { throw new RuntimeException(error); },
                () -> System.out.println("Analyzer: Completed: " + eBoardEventFeed)
        );

        connected = true;
    }

    public void disconnect() {
        liveChess.unsubscribe(eBoardEventFeed);
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    protected void finalize() throws Throwable {
        eBoardEventFeed.unsubscribe();
        eventsDisposable.dispose();
        super.finalize();
    }
}
