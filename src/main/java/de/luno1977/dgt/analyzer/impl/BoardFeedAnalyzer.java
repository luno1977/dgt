package de.luno1977.dgt.analyzer.impl;

import de.luno1977.dgt.analyzer.AnalyzerApp;
import de.luno1977.dgt.analyzer.view.ChessBoardView;
import de.luno1977.dgt.livechess.EBoardEventFeed;
import de.luno1977.dgt.livechess.LiveChess;
import de.luno1977.dgt.livechess.WebSocketFeed;
import de.luno1977.dgt.livechess.WebSocketResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.Arrays;

public class BoardFeedAnalyzer {

    private final LiveChess liveChess;
    private Disposable eventsDisposable;
    private EBoardEventFeed eBoardEventFeed;

    public BoardFeedAnalyzer() {
        liveChess = LiveChess.getInstance();
    }

    public void newGame() {

    }

    private void connectToBoard() {
        WebSocketResponse.EBoardsResponse eBoards = liveChess.getEBoards();

        eBoardEventFeed = liveChess.subscribe(eBoards.getParam().get(0));
        Observable<WebSocketFeed.EBoardEvent> events = eBoardEventFeed.events();
        eventsDisposable = events.observeOn(Schedulers.single()).subscribe(
                message -> System.out.println("Analyzer:" + message.getParam().getBoard()),
                error -> { throw new RuntimeException(error); },
                () -> System.out.println("Analyzer: Completed: " + eBoardEventFeed)
        );
    }
}
