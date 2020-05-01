package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.google.common.collect.Lists;
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
    private String currentBoardRep;

    public BoardFeedAnalyzer() {
        liveChess = LiveChess.getInstance();
        games = new ArrayList<>();
    }

    public void newGame() {
       if (connected) {
           Position startPosition = new Position(getStartFenFrom());
           this.currentGame = new AnalyzerGame(startPosition);
           games.add(this.currentGame);
           handleBoard();
       }
    }

    public void connect() {
        liveChess.connect();
        WebSocketResponse.EBoardsResponse eBoards = liveChess.getEBoards();

        currentBoardRep = eBoards.getParam().get(0).getBoard();

        eBoardEventFeed = liveChess.subscribe(eBoards.getParam().get(0));
        Observable<WebSocketFeed.EBoardEvent> events = eBoardEventFeed.events();
        eventsDisposable = events.observeOn(Schedulers.single()).subscribe(
                this::handleEBoardEvent,
                error -> { throw new RuntimeException(error); },
                () -> System.out.println("Analyzer: Completed: " + eBoardEventFeed)
        );

        connected = true;

        if (currentGame == null) {
            newGame();
        }
    }

    public void handleEBoardEvent(WebSocketFeed.EBoardEvent event) {
        System.out.println("Analyzer:" + event.getParam().getBoard());

        if (event.getParam().getBoard() != null) {
            this.currentBoardRep = event.getParam().getBoard();
            handleBoard();
        }
    }

    public void handleBoard() {
        if (this.currentGame != null) {
            Position current = this.currentGame.getNotation().getCurrentPosition();
            Board board = new Board();
            board.loadFromFen(current.getFen());
            try {
                MoveList moves = MoveGenerator.generateLegalMoves(board);
                System.out.println("Legal moves: " + moves);
                for (Move m : moves) {
                    board.doMove(m);
                    System.out.println(board.getFen());
                    board.undoMove();
                }
            } catch (MoveGeneratorException e) {
                e.printStackTrace();
            }
        }
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

    private String getStartFenFrom() {
        Board b = new Board();
        String preparedFen = currentBoardRep + " w KQkq - 0 1";
        System.out.println("Prepared Fen: " + preparedFen);
        String correctedFen = preparedFen;
        b.loadFromFen(preparedFen);

        //we assumed white is to move
        //we check if white can be at move
        //if white in chess?
        boolean isBlackInChess = b.isSquareAttackedBy(
                Lists.newArrayList(b.getKingSquare(b.getSideToMove().flip())),
                        b.getSideToMove());

        boolean isWhiteInChess = b.isSquareAttackedBy(
                Lists.newArrayList(b.getKingSquare(b.getSideToMove())),
                b.getSideToMove().flip());

        System.out.println(
                "IsWhiteInChess: " + isWhiteInChess +
                " IsBlackInChess: " + isBlackInChess);

        if (isBlackInChess && isWhiteInChess) {
            throw new IllegalStateException(
                    "Not both players can be in check. Illegal Start Position");
        }

        if (isBlackInChess) {
            //black is in check => black need to be to move
            String correctedSuffix = "b KQkq - 1 1";
            correctedFen = correctedFen.substring(0,
                    correctedFen.length()-correctedSuffix.length()) + correctedSuffix;
        } //isWhiteInChess: ok!

        System.out.println("CorrectedFen: " + correctedFen);

        return correctedFen;
    }
}
