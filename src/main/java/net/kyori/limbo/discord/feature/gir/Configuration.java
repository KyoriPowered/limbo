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
package net.kyori.limbo.discord.feature.gir;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import net.kyori.fragment.filter.Filter;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.PullRequest;
import net.kyori.kassel.guild.Guild;
import net.kyori.limbo.discord.action.Action;
import net.kyori.limbo.discord.filter.GuildQuery;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.git.repository.RepositoryQuery;
import net.kyori.mu.Maybe;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
/* package */ final class Configuration {
  final List<Search> searches = new ArrayList<>();
  @MonotonicNonNull Action openAction;
  @MonotonicNonNull Action mergedAction;
  @MonotonicNonNull Action closedAction;

  @NonNull Maybe<SearchResult> search(final Guild guild, final String string) {
    for(final Search search : this.searches) {
      final Matcher matcher = search.pattern.matcher(string);
      if(matcher.find()) {
        final String tag = matcher.group(1).toLowerCase(Locale.ENGLISH);
        final /* @Nullable */ RepositoryId repository = search.repositories.stream().filter(rid -> rid.tags().contains(tag)).findAny().orElse(null);
        if(repository == null) {
          continue;
        }
        if(search.filter != null && search.filter.denied(new Query() {
          @Override
          public @NonNull Guild guild() {
            return guild;
          }

          @Override
          public @NonNull RepositoryId repository() {
            return repository;
          }
        })) {
          continue;
        }
        try {
          final int number = Integer.parseInt(matcher.group(2));
          return Maybe.just(new SearchResult(tag.toUpperCase(Locale.ENGLISH), repository, number));
        } catch(final NumberFormatException ignored) {
        }
      }
    }
    return Maybe.nothing();
  }

  Action actionFor(final Issue issue, final @Nullable PullRequest pr) {
    if(pr != null && pr.merged()) {
      return this.mergedAction;
    }
    switch(issue.state()) {
      case OPEN: return this.openAction;
      case CLOSED: return this.closedAction;
    }
    throw new IllegalStateException("no action");
  }

  interface Query extends GuildQuery, RepositoryQuery {
  }

  static class Search {
    final Filter filter;
    final Pattern pattern;
    final List<RepositoryId> repositories;

    Search(final Filter filter, final Pattern pattern, final List<RepositoryId> repositories) {
      this.filter = filter;
      this.pattern = pattern;
      this.repositories = repositories;
    }
  }

  static class SearchResult {
    final String tag;
    final RepositoryId repository;
    final int number;

    SearchResult(final String tag, final RepositoryId repository, final int number) {
      this.tag = tag;
      this.repository = repository;
      this.number = number;
    }
  }
}
