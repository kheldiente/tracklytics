package com.orhanobut.tracklytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Annotation based tracking event handler.
 */
public class Tracklytics {

  final Map<String, Object> superAttributes = new HashMap<>();

  private final EventSubscriber eventSubscriber;

  private boolean enabled = true;
  private EventLogListener logger;

  private Tracklytics(EventSubscriber eventSubscriber) {
    this.eventSubscriber = eventSubscriber;
  }

  public static Tracklytics init(EventSubscriber subscriber) {
    Tracklytics tracklytics = new Tracklytics(subscriber);
    TracklyticsAspect.init(tracklytics);
    return tracklytics;
  }

  void event(TrackEvent trackEvent, Map<String, Object> attributes, Map<String, Object> superAttributes) {
    if (!enabled) return;

    eventSubscriber.onEvent(new Event(trackEvent, attributes), superAttributes);
  }

  public void trackEvent(Event event) {
    if (!enabled) return;

    eventSubscriber.onEvent(event, superAttributes);
  }

  void enabled(boolean enabled) {
    this.enabled = enabled;
  }

  boolean isEnabled() {
    return enabled;
  }

  void log(long start, long stopMethod, long stopTracking, TrackEvent event, Map<String, Object> attrs,
           Map<String, Object> superAttrs) {
    if (logger != null) {
      long method = TimeUnit.NANOSECONDS.toMillis(stopMethod - start);
      long total = TimeUnit.NANOSECONDS.toMillis(stopTracking - start);

      @SuppressWarnings("StringBufferReplaceableByString")
      StringBuilder builder = new StringBuilder()
          .append("[")
          .append(method)  // Method execution time
          .append("+")
          .append(total - method)  // Tracking execution time
          .append("=")
          .append(total)  // Total execution time
          .append("ms] ")
          .append(event.value())
          .append("-> ")
          .append(attrs.toString())
          .append(", super attrs: ")
          .append(superAttrs.toString())
          .append(", filters: ")
          .append(Arrays.toString(event.filters()));
      logger.log(builder.toString());
    }
  }

  public void setEventLogListener(EventLogListener logger) {
    this.logger = logger;
  }

  /**
   * Allows you to add super attribute without requiring to use annotation
   */
  public void addSuperAttribute(String key, Object value) {
    this.superAttributes.put(key, value);
  }

  /**
   * Allows you to remove super attribute without requiring to use annotation
   */
  public void removeSuperAttribute(String key) {
    this.superAttributes.remove(key);
  }

}