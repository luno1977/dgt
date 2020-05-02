package de.luno1977.dgt.analyzer.impl;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.UI;
import de.luno1977.dgt.analyzer.view.ChessBoardView;
import de.luno1977.dgt.livechess.EBoardEventFeed;
import de.luno1977.dgt.livechess.LiveChess;
import de.luno1977.dgt.livechess.WebSocketFeed;
import de.luno1977.dgt.livechess.WebSocketResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

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

    //Events
    private PublishSubject<String> boardRepEvents;
    private PublishSubject<String> notationLog;

    public BoardFeedAnalyzer() {
        liveChess = LiveChess.getInstance();
        games = new ArrayList<>();
        boardRepEvents = PublishSubject.create();
        notationLog = PublishSubject.create();
    }

    public void newGame() {
       if (connected) {
           Position startPosition = new Position(getStartFenFrom());
           this.currentGame = new AnalyzerGame(startPosition);
           games.add(this.currentGame);
       }
    }

    public void connect() {
        liveChess.connect();
        WebSocketResponse.EBoardsResponse eBoards = liveChess.getEBoards();

        //currentBoardRep = eBoards.getParam().get(0).getBoard();
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

    public void disconnect() {
        eBoardEventFeed.unsubscribe();
        eventsDisposable.dispose();
        boardRepEvents.onComplete();
        notationLog.onComplete();
        //reconstruct
        boardRepEvents = PublishSubject.create();
        notationLog = PublishSubject.create();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public Observable<String> getBoardRepEvents() {
        return boardRepEvents;
    }

    public PublishSubject<String> getNotationLog() {
        return notationLog;
    }

    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }

    private void handleEBoardEvent(WebSocketFeed.EBoardEvent event) {
        System.out.println("Analyzer:" + event.getParam().getBoard());

        String newRep = event.getParam().getBoard();
        if (newRep != null && !newRep.equals(this.currentBoardRep)) {
            //New position comes in

            //updates
            this.currentBoardRep = newRep;
            this.boardRepEvents.onNext(newRep);

            if (currentGame != null) {
                boolean isFollowUp = currentGame.insertIfFollowUpPosition(newRep);

                if (!isFollowUp) {
                    //is direct take back?

                    //is it a former position
                    boolean mustJump = currentGame.mustJumpToFormerPosition(newRep);
                }

                String notationLogEntry = currentGame.getNotation().toString();
                //System.out.println(notationLogEntry);
                notationLog.onNext(notationLogEntry);
            }
            //is it a followUpPosition?
            //yes?
                //is the follow up valid
                //yes

                    //update notation by followUp Moves
                    //update followUpPositions
                //No
                    //is it a direct follow up
                    //yes: insert move - mark as invalid - create subline
                    //no: do nothing.
            //No?
                //Is it a former position?
                //Goto former position
                //update followUpPositions

        }
    }

    private String getStartFenFrom() {
        Board b = new Board();
        String defaultDetails = "w KQkq - 0 1";
        String preparedFen = currentBoardRep + " " + defaultDetails;
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

        String whiteCastleRights = "";
        if (b.getPiece(Square.E1) == Piece.WHITE_KING) {
            //Assumes that rooks has never moved
            if (b.getPiece(Square.H1) == Piece.WHITE_ROOK) {
                whiteCastleRights += "K";
            }
            if (b.getPiece(Square.A1) == Piece.WHITE_ROOK) {
                whiteCastleRights += "Q";
            }
        }

        String blackCastleRights = "";
        if (b.getPiece(Square.E8) == Piece.BLACK_KING) {
            //Assumes that rooks has never moved
            if (b.getPiece(Square.H8) == Piece.BLACK_ROOK) {
                blackCastleRights += "k";
            }
            if (b.getPiece(Square.A8) == Piece.BLACK_ROOK) {
                blackCastleRights += "q";
            }
        }

        String castleRights = whiteCastleRights+blackCastleRights;
        if (castleRights.isEmpty()) castleRights = "-";

        String correctedSuffix;
        if (isBlackInChess) {
            //black is in check => black need to be to move
            correctedSuffix = "b " + castleRights + " - 1 1";
        } else {
            correctedSuffix = "w " + castleRights + " - 0 1";
        }

        correctedFen = correctedFen.substring(0,
                correctedFen.length()-defaultDetails.length()) + correctedSuffix;

        System.out.println("CorrectedFen: " + correctedFen);
        return correctedFen;
    }
}
