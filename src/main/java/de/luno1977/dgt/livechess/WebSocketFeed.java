package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.reactivex.Observable;

import java.util.concurrent.atomic.AtomicLong;

public interface WebSocketFeed<E extends WebSocketFeed.Event<P>, P> {

    AtomicLong FEED_IDS = new AtomicLong(0l);

    long getId();
    String getFeed();

    boolean isSubscribed();
    void subscribe();
    void unsubscribe();
    @JsonIgnore Observable<E> events();

    interface Event<P> extends WebSocketResponse<P> {}

    abstract class BaseFeed<E extends WebSocketFeed.Event<P>, P> implements WebSocketFeed<E,P> {

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public boolean isSubscribed() {
            return false;
        }

        @Override
        public void subscribe() {

        }

        @Override
        public void unsubscribe() {

        }

        @Override
        public Observable<E> events() {
            return null;
        }
    }
}
