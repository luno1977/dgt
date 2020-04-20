package de.luno1977.dgt.livechess;

public class EBoardsCall implements WebSocketCall<Object> {

    private static final String CALL = "eboards";
    private final long id;

    public EBoardsCall(long id) {
        this.id = id;
    }

    @Override
    public String getCall() {
        return CALL;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Object getParam() {
        return null;
    }
}
