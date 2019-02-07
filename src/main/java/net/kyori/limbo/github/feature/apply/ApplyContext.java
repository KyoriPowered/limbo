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
package net.kyori.limbo.github.feature.apply;

import net.kyori.igloo.v3.Issue;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.BulkActions;
import net.kyori.limbo.github.issue.IssueQuery;
import net.kyori.lunar.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.validation.constraints.Null;

/* package */ class ApplyContext implements BulkActions.Context {
  private final Builder builder;

  /* package */ static Builder builder() {
    return new Builder();
  }

  private ApplyContext(final Builder builder) {
    this.builder = builder;
  }

  @SafeVarargs
  /* package */ final BulkActions applicators(final ApplyFeatureConfiguration config, final Pair<SearchScope, String>... scopes) {
    final BulkActions applicators = new BulkActions(this.builder.issue);
    for(final Pair<SearchScope, String> entry : scopes) {
      this.gatherApplicators(config, entry.left(), entry.right()).accept(applicators);
    }
    return applicators;
  }

  /* package */ Consumer<BulkActions> gatherApplicators(final ApplyFeatureConfiguration config, final SearchScope scope, final String string) {
    return applicators -> applicators.addAll(
      config.applicators(
        new IssueQuery() {
          @Override
          public @NonNull RepositoryId repository() {
            return ApplyContext.this.builder.repository;
          }

          @Override
          public @NonNull Event event() {
            return ApplyContext.this.builder.event;
          }

          @Override
          public @NonNull Set<ActorType> actorTypes() {
            return ApplyContext.this.builder.actorTypes.get();
          }

          @Override
          public Collection<String> oldLabels() {
            return ApplyContext.this.builder.oldLabels;
          }

          @Override
          public Collection<String> newLabels() {
            return ApplyContext.this.builder.newLabels;
          }
        },
        scope,
        string
      )
    );
  }

  @Override
  public Collection<String> existingLabels() {
    return this.builder.oldLabels;
  }

  /* package */ static class Builder {
    private Issue issue;
    private RepositoryId repository;
    private Event event;
    private Supplier<Set<ActorType>> actorTypes;
    private Collection<String> oldLabels = Collections.emptySet();
    private Collection<String> newLabels = Collections.emptySet();

    /* package */ Builder issue(final Issue issue) {
      this.issue = issue;
      return this;
    }

    /* package */ Builder repository(final RepositoryId repository) {
      this.repository = repository;
      return this;
    }

    /* package */ Builder event(final Event event) {
      this.event = event;
      return this;
    }

    /* package */ Builder actorTypes(final Supplier<Set<ActorType>> actorTypes) {
      this.actorTypes = actorTypes;
      return this;
    }

    /* package */ Builder oldLabels(final Collection<String> oldLabels) {
      this.oldLabels = oldLabels;
      return this;
    }

    /* package */ Builder newLabels(final @Null String newLabel) {
      if(newLabel != null) {
        return this.newLabels(Collections.singleton(newLabel));
      }
      return this;
    }

    /* package */ Builder newLabels(final Collection<String> newLabels) {
      this.newLabels = newLabels;
      return this;
    }

    /* package */ ApplyContext build() {
      return new ApplyContext(this);
    }
  }
}
