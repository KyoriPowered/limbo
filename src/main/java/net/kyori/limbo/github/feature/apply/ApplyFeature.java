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

import net.kyori.event.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.Repositories;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.api.event.IssueCommentEvent;
import net.kyori.limbo.github.api.event.IssuesEvent;
import net.kyori.limbo.github.api.event.PullRequestEvent;
import net.kyori.limbo.github.api.model.User;
import net.kyori.limbo.github.issue.IssueQuery;
import net.kyori.limbo.github.label.Labels;
import net.kyori.limbo.github.repository.cache.RepositoryPermissionCache;
import net.kyori.lunar.exception.Exceptions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
  public void open(final IssuesEvent event) {
    if(event.action != IssuesEvent.Action.OPENED) {
      return;
    }
    final Issue issue = this.repositories.get(event.repository).issues().get(event.issue.number);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        final Set<ActorType> types = new HashSet<>(1);
        types.add(ActorType.AUTHOR);
        if(ApplyFeature.this.permission.get(event.repository, event.issue.user).write()) {
          types.add(ActorType.COLLABORATOR);
        }
        if(event.issue.user.login.equals(ApplyFeature.this.selfUser.login)) {
          types.add(ActorType.SELF);
        }
        return types;
      }

      @Override
      public Event event() {
        return Event.ISSUE_OPEN;
      }

      @Override
      public Collection<String> labels() {
        return Labels.labels(issue);
      }

      @Override
      public RepositoryId repository() {
        return event.repository;
      }
    }, event.issue.body).forEach(Exceptions.rethrowConsumer(action -> action.apply(issue)));
  }

  @Subscribe
  public void open(final PullRequestEvent event) {
    if(event.action != PullRequestEvent.Action.OPENED) {
      return;
    }
    final Issue issue = this.repositories.get(event.repository).issues().get(event.pull_request.number);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        final Set<ActorType> types = new HashSet<>(1);
        types.add(ActorType.AUTHOR);
        if(ApplyFeature.this.permission.get(event.repository, event.pull_request.user).write()) {
          types.add(ActorType.COLLABORATOR);
        }
        if(event.pull_request.user.login.equals(ApplyFeature.this.selfUser.login)) {
          types.add(ActorType.SELF);
        }
        return types;
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
    }, event.pull_request.body).forEach(Exceptions.rethrowConsumer(action -> action.apply(issue)));
  }

  @Subscribe
  public void comment(final IssueCommentEvent event) {
    final Issue issue = this.repositories.get(event.repository).issues().get(event.issue.number);
    this.configuration.applicators(new IssueQuery() {
      @Override
      public Set<ActorType> actorTypes() {
        final Set<ActorType> types = new HashSet<>(2);
        if(event.issue.user.equals(event.comment.user)) {
          types.add(ActorType.AUTHOR);
        }
        if(ApplyFeature.this.permission.get(event.repository, event.comment.user).write()) {
          types.add(ActorType.COLLABORATOR);
        }
        if(event.issue.user.login.equals(ApplyFeature.this.selfUser.login)) {
          types.add(ActorType.SELF);
        }
        return types;
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
    }, event.comment.body).forEach(Exceptions.rethrowConsumer(action -> action.apply(issue)));
  }
}
