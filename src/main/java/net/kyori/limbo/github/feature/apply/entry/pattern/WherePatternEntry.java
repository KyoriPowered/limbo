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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.fragment.filter.Filter;
import net.kyori.limbo.github.action.Action;
import net.kyori.limbo.github.feature.apply.CollectContext;
import net.kyori.limbo.github.feature.apply.SearchScope;

public final class WherePatternEntry extends PatternEntry {
  private static final Collection<Action> NULL = Collections.singleton(null);
  private final List<Where> where;

  /* package */ WherePatternEntry(final Filter filter, final SearchScope scope, final Pattern pattern, final List<Where> where) {
    super(filter, scope, pattern);
    this.where = where;
  }

  @Override
  public void collect(final CollectContext context, final List<Action> actions) {
    final Matcher matcher = this.pattern.matcher(context.string);
    if(!matcher.find() || this.escaped(matcher, context.string)) {
      return;
    }
    for(final Where where : this.where) {
      actions.add(where.actions.get(matcher.group(where.group)));
    }
    actions.removeAll(NULL);
  }

  static class Where {
    final int group;
    final Map<String, Action> actions;

    Where(final int group, final Map<String, Action> actions) {
      this.group = group;
      this.actions = actions;
    }
  }
}
