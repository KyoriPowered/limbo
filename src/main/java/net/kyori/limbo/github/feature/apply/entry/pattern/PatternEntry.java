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
package net.kyori.limbo.github.feature.apply.entry.pattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.fragment.filter.Filter;
import net.kyori.limbo.github.feature.apply.SearchScope;
import net.kyori.limbo.github.feature.apply.entry.Entry;
import net.kyori.limbo.github.feature.apply.entry.ScopedEntry;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class PatternEntry extends Entry.Impl implements ScopedEntry {
  private static final char ESCAPE_CHARACTER = '`';
  private final SearchScope scope;
  final Pattern pattern;

  /* package */ PatternEntry(final Filter filter, final SearchScope scope, final Pattern pattern) {
    super(filter);
    this.scope = scope;
    this.pattern = pattern;
  }

  @Override
  public @NonNull SearchScope scope() {
    return this.scope;
  }

  boolean escaped(final Matcher matcher, final String string) {
    return matcher.start() != 0
      && string.charAt(matcher.start() - 1) == ESCAPE_CHARACTER
      && string.charAt(matcher.end()) == ESCAPE_CHARACTER;
  }
}
