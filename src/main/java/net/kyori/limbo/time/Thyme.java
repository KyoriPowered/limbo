/*
 * This file is part of limbo, licensed under the MIT License.
 *
 * Copyright (c) 2017-2019 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.limbo.time;

import java.time.Instant;
import java.util.Locale;
import net.time4j.CalendarUnit;
import net.time4j.ClockUnit;
import net.time4j.Duration;
import net.time4j.IsoUnit;
import net.time4j.Moment;
import net.time4j.PlainTimestamp;
import net.time4j.PrettyTime;
import net.time4j.engine.TimeMetric;
import net.time4j.tz.Timezone;
import net.time4j.tz.ZonalOffset;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Thyme {
  public static final PrettyTime PRETTY = PrettyTime.of(Locale.ENGLISH);
  public static final TimeMetric<IsoUnit, Duration<IsoUnit>> YMWDHMS_METRIC = metric(
    CalendarUnit.YEARS,
    CalendarUnit.MONTHS,
    CalendarUnit.WEEKS,
    CalendarUnit.DAYS,
    ClockUnit.HOURS,
    ClockUnit.MINUTES,
    ClockUnit.SECONDS
  );

  private Thyme() {
  }

  private static TimeMetric<IsoUnit, Duration<IsoUnit>> metric(final IsoUnit... units) {
    return Duration.in(Timezone.of(ZonalOffset.UTC), units);
  }

  public static @NonNull Duration<IsoUnit> duration(final @NonNull TimeMetric<IsoUnit, Duration<IsoUnit>> metric, final @NonNull Instant start, final @NonNull Instant end) {
    return metric.between(pts(start), pts(end));
  }

  private static PlainTimestamp pts(final Instant instant) {
    return Moment.from(instant).toZonalTimestamp(ZonalOffset.UTC);
  }
}
