package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import io.reactivex.Observable;

import java.util.concurrent.atomic.AtomicLong;

public interface WebSocketFeed<E extends WebSocketFeed.Event<EP>, EP, P> extends WebSocketCommunication<P> {

    AtomicLong FEED_IDS = new AtomicLong(0L);

    /**
     * @return the name of the feed.
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

    /**
     * @return An {@link Observable} that feeds the client with events form this feed,
     *         if clients subscribe to this observable.
     */
    @JsonIgnore Observable<E> events();

    /**
     * Event emitted by a feed.
     * @param <P> the type of parameters carried form the event.
     */
    interface Event<P> extends WebSocketResponse<P> {}

    /**
     * The events emitted by {@link EBoardEventFeed}.
     */
    class EBoardEvent extends WebSocketResponse.Base<EBoardEventResponse>
                      implements Event<EBoardEventResponse> {}


    abstract class Base<E extends WebSocketFeed.Event<EP>, EP, P> implements WebSocketFeed<E,EP,P> {

        private boolean subscribed = false;

        private final long id;
        private final P param;
        private final String feed;

        private final Class<E> eventType;
        private LiveChess.FeedHandler<E, EP> events;

        public Base(String feed, P param, Class<E> eventType) {
            this.id = FEED_IDS.getAndIncrement();
            this.feed = feed;
            this.param = param;
            this.eventType = eventType;
        }

        @Override public String getFeed() { return feed; }
        @Override public long getId() { return this.id; }
        @Override public P getParam() { return param; }
        @Override @JsonIgnore public boolean isSubscribed() { return subscribed; }

        @Override @JsonIgnore
        public void subscribe() {
            if (!isSubscribed()) {
                if (events != null) {
                    throw new IllegalStateException(
                            "Feed " + this.getClass().getSimpleName() + " is not subscribed, " +
                                    "but transmitter (Observable) is still open and not complete.");

                }
                new WebSocketCall.Subscribe.Handler(this).call();
                events = new LiveChess.FeedHandler<>(this.id, eventType);
                events.start();
                subscribed = true;
            }
        }

        @Override @JsonIgnore
        public void unsubscribe() {
            if (isSubscribed()) {
                new WebSocketCall.Unsubscribe.Handler(this).call();
                if (this.events != null) {
                    events.stop();
                    events = null;
                }
                subscribed = false;
            }
        }

        @Override @JsonIgnore
        public Observable<E> events() {
            return events.getObservable();
        }
    }
}
