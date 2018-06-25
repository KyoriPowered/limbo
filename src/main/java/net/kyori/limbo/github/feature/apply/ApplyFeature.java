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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import net.kyori.event.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.Repositories;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.BulkActions;
import net.kyori.limbo.github.api.event.IssueCommentEvent;
import net.kyori.limbo.github.api.event.IssuesEvent;
import net.kyori.limbo.github.api.event.PullRequestEvent;
import net.kyori.limbo.github.api.event.PullRequestReviewEvent;
import net.kyori.limbo.github.api.model.User;
import net.kyori.limbo.github.issue.IssueQuery;
import net.kyori.limbo.github.label.Labels;
import net.kyori.limbo.github.repository.cache.RepositoryPermissionCache;
import net.kyori.limbo.util.Tokens;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

public final class ApplyFeature implements Listener {
  private final ApplyFeatureConfiguration configuration;
  private final Repositories repositories;
  private final RepositoryPermissionCache permission;
  private final User selfUser;

  @Inject
  private ApplyFeature(final ApplyFeatureConfiguration configuration, final Repositories repositories, final RepositoryPermissionCache permission, final @Named("identity") User selfUser) {
    this.configuration = configuration;
    this.repositories = repositories;
    this.permission = permission;
    this.selfUser = selfUser;
  }

  @Subscribe
  public void openClose(final IssuesEvent event) throws IOException {
    if(event.action != IssuesEvent.Action.CLOSED && event.action != IssuesEvent.Action.OPENED) {
      return;
    }
    final Issue issue = this.repositories.get(event.repository).issues().get(event.issue.number);
    final Supplier<Set<ActorType>> actorTypes = Suppliers.memoize(() -> new ActorType.Collector()
      .author(event.sender.login.equals(event.issue.user.login))
      .collaborator(this.permission.get(event.repository, event.issue.user).write())
      .self(event.issue.user.login.equals(this.selfUser.login))
      .get());
    final BulkActions applicators = new BulkActions(issue);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        return actorTypes.get();
      }

      @Override
      public Event event() {
        switch(event.action) {
          case OPENED:
            return Event.ISSUE_OPEN;
          case CLOSED:
            return Event.ISSUE_CLOSE;
        }
        throw new IllegalArgumentException(event.action.name());
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.issue.body).forEach(applicators::add);
    applicators.apply(ImmutableMap.of(
      Tokens.AUTHOR, event.issue.user.login,
      Tokens.REPOSITORY_USER, event.repository.owner,
      Tokens.REPOSITORY_NAME, event.repository.name
    ));
  }

  @Subscribe
  public void label(final IssuesEvent event) throws IOException {
    if(event.action != IssuesEvent.Action.LABELED && event.action != IssuesEvent.Action.UNLABELED) {
      return;
    }
    final Issue issue = this.repositories.get(event.repository).issues().get(event.issue.number);
    final Supplier<Set<ActorType>> actorTypes = Suppliers.memoize(() -> new ActorType.Collector()
      .author(event.sender.login.equals(event.issue.user.login))
      .collaborator(this.permission.get(event.repository, event.sender).write())
      .self(event.sender.login.equals(this.selfUser.login))
      .get());
    final BulkActions applicators = new BulkActions(issue);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        return actorTypes.get();
      }

      @Override
      public Event event() {
        switch(event.action) {
          case LABELED:
            return event.issue.pull_request != null ? Event.PULL_REQUEST_LABEL : Event.ISSUE_LABEL;
          case UNLABELED:
            return event.issue.pull_request != null ? Event.PULL_REQUEST_UNLABEL : Event.ISSUE_UNLABEL;
        }
        throw new IllegalArgumentException(event.action.name());
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.issue.body).forEach(applicators::add);
    applicators.apply(ImmutableMap.of(
      Tokens.AUTHOR, event.issue.user.login,
      Tokens.REPOSITORY_USER, event.repository.owner,
      Tokens.REPOSITORY_NAME, event.repository.name
    ));
  }

  @Subscribe
  public void open(final PullRequestEvent event) throws IOException {
    if(event.action != PullRequestEvent.Action.OPENED) {
      return;
    }
    final Issue issue = this.repositories.get(event.repository).issues().get(event.pull_request.number);
    final Supplier<Set<ActorType>> actorTypes = Suppliers.memoize(() -> new ActorType.Collector()
      .author(true)
      .collaborator(this.permission.get(event.repository, event.pull_request.user).write())
      .self(event.pull_request.user.login.equals(this.selfUser.login))
      .get());
    final BulkActions applicators = new BulkActions(issue);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        return actorTypes.get();
      }

      @Override
      public Event event() {
        return Event.PULL_REQUEST_OPEN;
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.pull_request.body).forEach(applicators::add);
    applicators.apply(ImmutableMap.of(
      Tokens.AUTHOR, event.pull_request.user.login,
      Tokens.REPOSITORY_USER, event.repository.owner,
      Tokens.REPOSITORY_NAME, event.repository.name
    ));
  }

  @Subscribe
  public void comment(final IssueCommentEvent event) throws IOException {
    final Issue issue = this.repositories.get(event.repository).issues().get(event.issue.number);
    final Supplier<Set<ActorType>> actorTypes = Suppliers.memoize(() -> new ActorType.Collector()
      .author(event.issue.user.equals(event.comment.user))
      .collaborator(this.permission.get(event.repository, event.comment.user).write())
      .self(event.comment.user.login.equals(this.selfUser.login))
      .get());
    final BulkActions applicators = new BulkActions(issue);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        return actorTypes.get();
      }

      @Override
      public Event event() {
        return event.issue.pull_request != null ? Event.PULL_REQUEST_COMMENT : Event.ISSUE_COMMENT;
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.comment.body).forEach(applicators::add);
    applicators.apply(ImmutableMap.of(
      Tokens.AUTHOR, event.issue.user.login,
      Tokens.REPOSITORY_USER, event.repository.owner,
      Tokens.REPOSITORY_NAME, event.repository.name
    ));
  }

  @Subscribe
  public void review(final PullRequestReviewEvent event) throws IOException {
    final Issue issue = this.repositories.get(event.repository).issues().get(event.pull_request.number);
    final Supplier<Set<ActorType>> actorTypes = Suppliers.memoize(() -> new ActorType.Collector()
      .author(event.pull_request.user.equals(event.review.user))
      .collaborator(this.permission.get(event.repository, event.review.user).write())
      .self(event.review.user.login.equalsIgnoreCase(this.selfUser.login))
      .get());
    final BulkActions applicators = new BulkActions(issue);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        return actorTypes.get();
      }

      @Override
      public Event event() {
        return event.action.asEvent();
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.review.body).forEach(applicators::add);
    applicators.apply(ImmutableMap.of(
      Tokens.AUTHOR, event.pull_request.user.login,
      Tokens.REPOSITORY_USER, event.repository.owner,
      Tokens.REPOSITORY_NAME, event.repository.name
    ));
  }
}
