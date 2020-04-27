package de.luno1977.dgt.livechess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.luno1977.dgt.livechess.model.EBoardEventResponse;
import io.reactivex.Observable;

public interface WebSocketFeed<E extends WebSocketFeed.Event<EP>, EP, P> extends WebSocketCommunication<P> {

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

    class FeedRef {
        private final long id;

        @JsonCreator
        public FeedRef(@JsonProperty("id") long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    abstract class FeedCall<P> extends WebSocketCall.BaseCall<WebSocketCall<P>> {

        private final long feedId;
        private final P param;

        FeedCall(long feedId, P param) {
            this.feedId = feedId;
            this.param = param;
        }

        @Override
        public String getCall() {
            return "call";
        }

        @Override
        public WebSocketCall<P> getParam() {
            return new WebSocketCall<P>() {
                @Override public long getId() { return feedId; }
                @Override public P getParam() { return param; }
                @Override @JsonProperty("method") public String getCall() {
                    return FeedCall.this.getClass().getSimpleName().toLowerCase(); }
            };
        }
    }


    abstract class Base<E extends WebSocketFeed.Event<EP>, EP, P> implements WebSocketFeed<E,EP,P> {

        private boolean subscribed = false;

        private final long id;
        private final P param;
        private final String feed;

        private final Class<E> eventType;
        private LiveChess.FeedHandler<E, EP> events;

        public Base(String feed, P param, Class<E> eventType) {
            this.id = IDS.getAndIncrement();
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

        @Override
        public String toString() {
            return this.getClass().getSimpleName()+"{" +
                    "id=" + id +
                    ", param=" + param +
                    ", feed='" + feed + '\'' +
                    '}';
        }
    }
}
