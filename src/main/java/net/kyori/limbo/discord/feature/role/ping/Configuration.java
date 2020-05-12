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
package net.kyori.limbo.discord.feature.role.ping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.limbo.discord.action.Action;
import net.kyori.mu.Maybe;
import org.checkerframework.checker.nullness.qual.NonNull;

@Deprecated
@Singleton
/* package */ final class Configuration {
  final List<Search> searches = new ArrayList<>();

  @NonNull Maybe<SearchResult> search(final FilterQuery query, final String string) {
    for(final Search search : this.searches) {
      final Matcher matcher = search.pattern.matcher(string);
      if(matcher.find()) {
        final String id = matcher.group(1);
        if(!search.ids.contains(id)) {
          continue;
        }
        if(search.filter.allowed(query)) {
          return Maybe.just(new SearchResult(search, matcher.group(2)));
        }
      }
    }
    return Maybe.nothing();
  }

  static class Search {
    final Pattern pattern;
    final Set<String> ids;
    final Filter filter;
    final @Snowflake long role;
    final Action action;

    Search(final Pattern pattern, final Set<String> ids, final Filter filter, final @Snowflake long role, final Action action) {
      this.pattern = pattern;
      this.ids = ids;
      this.filter = filter;
      this.role = role;
      this.action = action;
    }
  }

  static class SearchResult {
    final Search search;
    final String content;

    SearchResult(final Search search, final String content) {
      this.search = search;
      this.content = content;
    }
  }
}
