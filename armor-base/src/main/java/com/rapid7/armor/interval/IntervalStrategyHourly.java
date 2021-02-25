package com.rapid7.armor.interval;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class IntervalStrategyHourly implements IntervalStrategy {
  private static final String INTERVAL = "hourly";

  @Override
  public String getInterval() {
    return INTERVAL;
  }

  @Override
  public String getIntervalStart(Instant timestamp) {
    ZonedDateTime dateTime = timestamp.atZone(ZoneId.of("UTC"));

    return dateTime
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant()
        .toString();
  }

  @Override
  public String getIntervalStart(Instant timestamp, int offset) {
    ZonedDateTime dateTime = timestamp.atZone(ZoneId.of("UTC"));

    return dateTime
        .plusHours(offset)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant()
        .toString();
  }

  @Override
  public boolean supports(String interval) {
    return INTERVAL.equals(interval);
  }
}
