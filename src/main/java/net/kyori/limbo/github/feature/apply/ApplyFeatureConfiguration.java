/*
 * This file is part of limbo, licensed under the MIT License.
 *
 * Copyright (c) 2017-2018 KyoriPowered
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
package net.kyori.limbo.github.feature.apply;

import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.limbo.github.action.Action;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

@Singleton
final class ApplyFeatureConfiguration {
  final Collection<Entry> entries = new ArrayList<>();

  public List<Action> applicators(final FilterQuery query, final String string) {
    final List<Action> applicators = new ArrayList<>();
    for(final Entry entry : this.entries) {
      if(entry.filter == null || entry.filter.allowed(query)) {
        for(final net.kyori.limbo.github.feature.apply.entry.Entry action : entry.actions) {
          if(action.filter().allowed(query)) {
            action.collect(string, applicators);
          }
        }
      }
    }
    return applicators;
  }

  static class Entry {
    private final @Nullable Filter filter;
    private final List<net.kyori.limbo.github.feature.apply.entry.Entry> actions;

    Entry(final @Nullable Filter filter, final List<net.kyori.limbo.github.feature.apply.entry.Entry> actions) {
      this.filter = filter;
      this.actions = actions;
    }
  }
}
