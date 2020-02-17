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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.kyori.igloo.v3.Issue;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.BulkActions;
import net.kyori.limbo.github.issue.IssueQuery;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/* package */ class ApplyContext implements BulkActions.Context {
  private final Issue issue;
  private final RepositoryId repository;
  private final Event event;
  private final Supplier<Set<ActorType>> actorTypes;
  private final Collection<String> oldLabels;
  private final Collection<String> newLabels;

  /* package */ static Builder builder() {
    return new Builder();
  }

  private ApplyContext(final Builder builder) {
    this.issue = builder.issue;
    this.repository = builder.repository;
    this.event = builder.event;
    this.actorTypes = builder.actorTypes;
    this.oldLabels = builder.oldLabels;
    this.newLabels = builder.newLabels;
  }

  /* package */ final BulkActions applicators(final ApplyFeatureConfiguration config, final SearchInstance... searches) {
    final BulkActions applicators = new BulkActions(this.issue);
    for(final SearchInstance search : searches) {
      this.gatherApplicators(config, search).accept(applicators);
    }
    return applicators;
  }

  /* package */ Consumer<BulkActions> gatherApplicators(final ApplyFeatureConfiguration config, final SearchInstance search) {
    return applicators -> applicators.addAll(
      config.applicators(
        new IssueQuery() {
          @Override
          public @NonNull RepositoryId repository() {
            return ApplyContext.this.repository;
          }

          @Override
          public @NonNull Event event() {
            return ApplyContext.this.event;
          }

          @Override
          public @NonNull Set<ActorType> actorTypes() {
            return ApplyContext.this.actorTypes.get();
          }

          @Override
          public Collection<String> oldLabels() {
            return ApplyContext.this.oldLabels;
          }

          @Override
          public Collection<String> newLabels() {
            return ApplyContext.this.newLabels;
          }
        },
        search.scope,
        search.query
      )
    );
  }

  @Override
  public Collection<String> existingLabels() {
    return this.oldLabels;
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

    /* package */ Builder newLabels(final @Nullable String newLabel) {
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
