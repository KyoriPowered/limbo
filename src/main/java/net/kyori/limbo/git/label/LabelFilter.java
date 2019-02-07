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
package net.kyori.limbo.git.label;

import com.google.common.collect.Iterables;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class LabelFilter implements TypedFilter<LabelQuery> {
  private final String label;
  private final Context context;

  /* package */ LabelFilter(final String label, final Context context) {
    this.label = label;
    this.context = context;
  }

  @Override
  public boolean queryable(final @NonNull FilterQuery query) {
    return query instanceof LabelQuery;
  }

  @Override
  public @NonNull FilterResponse typedQuery(final @NonNull LabelQuery query) {
    switch(this.context) {
      case ANY:
        return FilterResponse.from(Iterables.contains(Iterables.concat(query.oldLabels(), query.newLabels()), this.label));
      case OLD:
        return FilterResponse.from(query.oldLabels().contains(this.label));
      case NEW:
        return FilterResponse.from(query.newLabels().contains(this.label));
      default:
        throw new IllegalArgumentException();
    }
  }

  public enum Context {
    ANY,
    OLD,
    NEW;
  }
}
