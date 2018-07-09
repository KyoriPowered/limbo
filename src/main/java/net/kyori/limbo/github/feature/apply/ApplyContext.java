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

import net.kyori.igloo.v3.Issue;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.BulkActions;
import net.kyori.limbo.github.issue.IssueQuery;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/* package */ class ApplyContext implements BulkActions.Context {
  private final Builder builder;

  /* package */ static Builder builder() {
    return new Builder();
  }

  private ApplyContext(final Builder builder) {
    this.builder = builder;
  }

  /* package */ BulkActions applicators(final ApplyFeatureConfiguration config, final String string) {
    final BulkActions applicators = new BulkActions(this.builder.issue);
    applicators.addAll(
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
          public Collection<String> labels() {
            return ApplyContext.this.builder.labels;
          }
        },
        string
      )
    );
    return applicators;
  }

  @Override
  public Collection<String> existingLabels() {
    return this.builder.labels;
  }

  /* package */ static class Builder {
    private Issue issue;
    private RepositoryId repository;
    private Event event;
    private Supplier<Set<ActorType>> actorTypes;
    private Collection<String> labels;

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

    /* package */ Builder labels(final Collection<String> labels) {
      this.labels = labels;
      return this;
    }

    /* package */ ApplyContext build() {
      return new ApplyContext(this);
    }
  }
}