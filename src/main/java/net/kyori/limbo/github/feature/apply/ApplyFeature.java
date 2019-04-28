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

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.igloo.v3.Repositories;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.git.GitTokens;
import net.kyori.limbo.git.actor.ActorType;
import net.kyori.limbo.git.event.Event;
import net.kyori.limbo.github.api.event.IssueCommentEvent;
import net.kyori.limbo.github.api.event.IssuesEvent;
import net.kyori.limbo.github.api.event.PullRequestEvent;
import net.kyori.limbo.github.api.event.PullRequestReviewEvent;
import net.kyori.limbo.github.api.model.User;
import net.kyori.limbo.github.label.Labels;
import net.kyori.limbo.github.repository.cache.RepositoryPermissionCache;
import net.kyori.limbo.util.Tokens;
import net.kyori.mu.Pair;
import net.kyori.mu.function.ThrowingConsumer;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

public final class ApplyFeature implements Listener {
  private static final Set<IssuesEvent.Action> ISSUES_EVENT_ACTIONS = EnumSet.of(
    IssuesEvent.Action.CLOSED,
    IssuesEvent.Action.OPENED,
    IssuesEvent.Action.LABELED,
    IssuesEvent.Action.UNLABELED
  );
  private static final Set<PullRequestEvent.Action> PULL_REQUEST_EVENT_ACTIONS = EnumSet.of(
    PullRequestEvent.Action.CLOSED,
    PullRequestEvent.Action.OPENED
  );
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
  public void issues(final IssuesEvent event) throws IOException {
    if(!ISSUES_EVENT_ACTIONS.contains(event.action)) {
      return;
    }

    final MultiApplyContext mac = MultiApplyContext.builder()
      .repository(event.repository)
      .issue(this.repositories.get(event.repository).issues().get(event.issue.number))
      .labels(Labels.labels(event.issue))
      .build();
    final Consumer<Event> consumer = ThrowingConsumer.of(e -> {
      final ApplyContext context = mac.child()
        .event(e)
        .actorTypes(
          Suppliers.memoize(() -> new ActorType.Collector()
            .author(event.sender, event.issue.user)
            .collaborator(this.permission.get(event.repository, event.sender).write())
            .self(event.sender, this.selfUser)
            .get())
        )
        .oldLabels(Labels.labels(event.issue, event.label == null ? Predicates.alwaysTrue() : Predicates.not(label -> event.label.name.equals(label))))
        .newLabels(event.label != null ? event.label.name : null)
        .build();
      mac.collectApplicators(context.gatherApplicators(this.configuration, SearchScope.TITLE, event.issue.title));
      mac.collectApplicators(context.gatherApplicators(this.configuration, SearchScope.DESCRIPTION, event.issue.body));
    });
    consumer.accept(event.action.asEvent(event.issue.pull_request != null));
    if(event.action == IssuesEvent.Action.OPENED) {
      if(!event.issue.labels.isEmpty()) {
        consumer.accept(IssuesEvent.Action.LABELED.asEvent(event.issue.pull_request != null));
      }
    }
    mac.applicators()
      .apply(
        mac,
        ImmutableMap.of(
          Tokens.AUTHOR, event.issue.user.login,
          GitTokens.REPOSITORY_USER, event.repository.owner,
          GitTokens.REPOSITORY_NAME, event.repository.name
        )
      );
  }

  @Subscribe
  public void openClose(final PullRequestEvent event) throws IOException {
    if(!PULL_REQUEST_EVENT_ACTIONS.contains(event.action)) {
      return;
    }

    final ApplyContext context = ApplyContext.builder()
      .issue(this.repositories.get(event.repository).issues().get(event.pull_request.number))
      .repository(event.repository)
      .event(event.action.asEvent())
      .actorTypes(
        Suppliers.memoize(() -> new ActorType.Collector()
          .author(event.sender, event.pull_request.user)
          .collaborator(this.permission.get(event.repository, event.sender).write())
          .self(event.sender, this.selfUser)
          .get())
      )
      .oldLabels(Labels.labels(event.pull_request))
      .build();
    context.applicators(this.configuration, Pair.of(SearchScope.TITLE, event.pull_request.title), Pair.of(SearchScope.DESCRIPTION, event.pull_request.body))
      .apply(
        context,
        ImmutableMap.of(
          Tokens.AUTHOR, event.pull_request.user.login,
          GitTokens.REPOSITORY_USER, event.repository.owner,
          GitTokens.REPOSITORY_NAME, event.repository.name
        )
      );
  }

  @Subscribe
  public void comment(final IssueCommentEvent event) throws IOException {
    final ApplyContext context = ApplyContext.builder()
      .issue(this.repositories.get(event.repository).issues().get(event.issue.number))
      .repository(event.repository)
      .event(event.issue.pull_request != null ? Event.PULL_REQUEST_COMMENT : Event.ISSUE_COMMENT)
      .actorTypes(
        Suppliers.memoize(() -> new ActorType.Collector()
          .author(event.issue.user, event.comment.user)
          .collaborator(this.permission.get(event.repository, event.comment.user).write())
          .self(event.comment.user, this.selfUser)
          .get())
      )
      .oldLabels(Labels.labels(event.issue))
      .build();
    context.applicators(this.configuration, Pair.of(SearchScope.DESCRIPTION, event.comment.body))
      .apply(
        context,
        ImmutableMap.of(
          Tokens.AUTHOR, event.issue.user.login,
          GitTokens.REPOSITORY_USER, event.repository.owner,
          GitTokens.REPOSITORY_NAME, event.repository.name
        )
      );
  }

  @Subscribe
  public void review(final PullRequestReviewEvent event) throws IOException {
    final ApplyContext context = ApplyContext.builder()
      .issue(this.repositories.get(event.repository).issues().get(event.pull_request.number))
      .repository(event.repository)
      .event(event.action.asEvent())
      .actorTypes(
        Suppliers.memoize(() -> new ActorType.Collector()
          .author(event.sender, event.review.user)
          .collaborator(this.permission.get(event.repository, event.sender).write())
          .self(event.sender, this.selfUser)
          .get())
      )
      .oldLabels(Labels.labels(event.pull_request))
      .build();
    context.applicators(this.configuration, Pair.of(SearchScope.DESCRIPTION, event.review.body))
      .apply(
        context,
        ImmutableMap.of(
          Tokens.AUTHOR, event.pull_request.user.login,
          GitTokens.REPOSITORY_USER, event.repository.owner,
          GitTokens.REPOSITORY_NAME, event.repository.name
        )
      );
  }
}
