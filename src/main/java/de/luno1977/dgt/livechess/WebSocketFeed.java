package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.luno1977.dgt.livechess.model.EBoardEvent;
import io.reactivex.Observable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public interface WebSocketFeed<E extends WebSocketFeed.Event<P>, P> {

    AtomicLong FEED_IDS = new AtomicLong(0l);

    /**
     * @return The id of the feed
     */
    long getId();

    /**
     * @return the name of the feed
     */
    String getFeed();

    /**
     * Subscribes to the board feed.
     */
    void subscribe();

    /**
     * Subscribes to the board feed.
     */
    void unsubscribe();

    /**
     * @return true, if feed is subscribed, otherwise false.
     */
    boolean isSubscribed();
    @JsonIgnore Observable<E> events();

    interface Event<P> extends WebSocketResponse<P> {}

    abstract class BaseFeed<E extends WebSocketFeed.Event<P>, P> implements WebSocketFeed<E,P> {

        private boolean subscribed = false;

        private final long id;
        private final String feed;

        public BaseFeed(String feed) {
            this.feed = feed;
            this.id = FEED_IDS.getAndIncrement();
        }

        @Override
        public long getId() {
            return this.id;
        }

        @Override
        public String getFeed() {
            return feed;
        }

        @Override
        public boolean isSubscribed() {
            return subscribed;
        }

        @Override
        public void subscribe() {
            if (!isSubscribed()) {
                new WebSocketCall.Subscribe.Handler(this).call();
                subscribed = true;
            }
        }

        @Override
        public void unsubscribe() {
            if (isSubscribed()) {
                new WebSocketCall.Unsubscribe.Handler(this).call();
                subscribed = false;
            }
        }

        @Override
        public Observable<E> events() {
            return null;
        }
    }
}
