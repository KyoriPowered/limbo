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
package net.kyori.limbo.github.feature.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.Action;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
/* package */ final class MoveConfiguration {
  final Collection<Entry> entries = new ArrayList<>();

  /* package */ @Nullable Entry source(final net.kyori.igloo.v3.RepositoryId source) {
    for(final Entry entry : this.entries) {
      if(entry.sourceRepository.equals(source)) {
        return entry;
      }
    }
    return null;
  }

  /* package */ static class Entry {
    /* package */ final Pattern pattern;
    /* package */ final RepositoryId sourceRepository;
    /* package */ final Action sourceAction;
    /* package */ final List<Target> targets;

    /* package */ Entry(final Pattern pattern, final RepositoryId sourceRepository, final Action sourceAction, final List<Target> targets) {
      this.pattern = pattern;
      this.sourceRepository = sourceRepository;
      this.sourceAction = sourceAction;
      this.targets = targets;
    }

    /* package */ @Nullable Target target(final String comment) {
      final Matcher matcher = this.pattern.matcher(comment);
      if(matcher.matches()) {
        final String tag = matcher.group(1);
        for(final Target target : this.targets) {
          if(target.repository.tags().contains(tag)) {
            return target;
          }
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return this.sourceRepository.toString();
    }
  }

  /* package */ static class Target {
    /* package */ final RepositoryId repository;
    /* package */ final Action action;

    /* package */ Target(final RepositoryId repository, final Action action) {
      this.repository = repository;
      this.action = action;
    }
  }
}
