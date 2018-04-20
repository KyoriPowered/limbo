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
package net.kyori.limbo.feature.discord.feature.gir;

import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.PullRequest;
import net.kyori.limbo.feature.discord.action.Action;
import net.kyori.limbo.feature.git.repository.RepositoryId;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

@Singleton
final class Configuration {
  final List<Search> searches = new ArrayList<>();
  @MonotonicNonNull Action openAction;
  @MonotonicNonNull Action mergedAction;
  @MonotonicNonNull Action closedAction;

  @NonNull Optional<SearchResult> search(final String string) {
    for(final Search search : this.searches) {
      final Matcher matcher = search.pattern.matcher(string);
      if(matcher.find()) {
        final String tag = matcher.group(1).toLowerCase(Locale.ENGLISH);
        final @Nullable RepositoryId repository = search.repositories.stream().filter(rid -> rid.tags().contains(tag)).findAny().orElse(null);
        if(repository != null) {
          try {
            final int number = Integer.parseInt(matcher.group(2));
            return Optional.of(new SearchResult(tag.toUpperCase(Locale.ENGLISH), repository, number));
          } catch(final NumberFormatException ignored) {
          }
        }
      }
    }
    return Optional.empty();
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

  static class Search {
    final Pattern pattern;
    final List<RepositoryId> repositories;

    Search(final Pattern pattern, final List<RepositoryId> repositories) {
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
