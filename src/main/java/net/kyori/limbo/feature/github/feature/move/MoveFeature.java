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
package net.kyori.limbo.feature.github.feature.move;

import com.google.common.collect.ImmutableMap;
import net.kyori.event.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.Repositories;
import net.kyori.igloo.v3.RepositoryId;
import net.kyori.limbo.core.event.Listener;
import net.kyori.limbo.feature.Feature;
import net.kyori.limbo.feature.git.issue.IssueToken;
import net.kyori.limbo.feature.github.api.event.IssueCommentEvent;
import net.kyori.limbo.feature.github.api.model.Repository;
import net.kyori.limbo.feature.github.repository.GitHubRepositoryIdImpl;
import net.kyori.limbo.feature.github.repository.cache.RepositoryPermissionCache;
import net.kyori.limbo.util.Tokens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class MoveFeature implements Feature, Listener {
  private static final Logger LOGGER = LogManager.getLogger();
  private final MoveConfiguration configuration;
  private final Repositories repositories;
  private final RepositoryPermissionCache permission;

  @Inject
  private MoveFeature(final MoveConfiguration configuration, final Repositories repositories, final RepositoryPermissionCache permission) {
    this.configuration = configuration;
    this.repositories = repositories;
    this.permission = permission;
  }

  @Subscribe
  public void move(final IssueCommentEvent event) throws IOException {
    final Repository sourceRepository = event.repository;

    final MoveConfiguration.@Nullable Entry source = this.configuration.source(sourceRepository);
    if(source == null) {
      LOGGER.error("Could not find source entry for '{}'", sourceRepository);
      return;
    }

    final MoveConfiguration.@Nullable Target target = source.target(event.comment.body);
    if(target == null) {
      LOGGER.error("Could not find target entry for '{}'", source);
      return;
    }

    final RepositoryId targetRepository = new GitHubRepositoryIdImpl(target.repository);

    LOGGER.info("Trying to move issue '{}#{}' to '{}'", sourceRepository.asString(), event.issue.number, targetRepository.asString());
    if(!this.permission.get(sourceRepository, event.comment.user).write() && !this.permission.get(targetRepository, event.comment.user).write()) {
      LOGGER.error("Could not move issue - '{}' is not a collaborator", event.comment.user.login);
      return;
    }

    final net.kyori.igloo.v3.Repository sourceRepo = this.repositories.get(sourceRepository);
    final net.kyori.igloo.v3.Repository targetRepo = this.repositories.get(targetRepository);

    final Issue sourceIssue = sourceRepo.issues().get(event.issue.number);
    final Issue targetIssue = targetRepo.issues().create(new Issue.Create.Full() {
      @Override
      public String title() {
        return event.issue.title;
      }

      @Override
      public String body() {
        return Tokens.format(
          target.action.comment(),
          ImmutableMap.of(
            MoveToken.AUTHOR, event.issue.user.login,
            IssueToken.BODY, event.issue.body,
            MoveToken.SOURCE, sourceRepository.asString(),
            MoveToken.SOURCE_ID, event.issue.number,
            MoveToken.SOURCE_URL, event.issue.html_url
          )
        );
      }

      @Override
      public Collection<String> assignees() {
        return event.issue.assignees.stream().map(user -> user.login).collect(Collectors.toSet());
      }

      @Override
      public Collection<String> labels() {
        return Stream.concat(
          event.issue.labels.stream().map(label -> label.name),
          target.action.addLabels().stream()
        ).collect(Collectors.toSet());
      }

      @Exclude
      @Override
      public int milestone() {
        return 0;
      }
    });

    sourceIssue.comments().post(() -> Tokens.format(
      source.sourceAction.comment(),
      ImmutableMap.of(
        MoveToken.AUTHOR, event.issue.user.login,
        MoveToken.TARGET, targetRepository.asString(),
        MoveToken.TARGET_ID, targetIssue.number(),
        MoveToken.TARGET_URL, targetIssue.html_url()
      )
    ));

    source.sourceAction.apply(sourceIssue);
    target.action.apply(targetIssue);
  }
}
