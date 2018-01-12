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
import net.kyori.blizzard.Nullable;
import net.kyori.event.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.Repositories;
import net.kyori.igloo.v3.RepositoryId;
import net.kyori.limbo.core.event.Listener;
import net.kyori.limbo.feature.Feature;
import net.kyori.limbo.feature.github.api.event.IssueCommentEvent;
import net.kyori.limbo.feature.github.api.model.Repository;
import net.kyori.limbo.feature.github.cache.RepositoryPermissionCache;
import net.kyori.limbo.util.Tokens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class MoveIssueFeature implements Feature, Listener {
  private static final Logger LOGGER = LogManager.getLogger();
  private final IssueMoveConfiguration configuration;
  private final Repositories repositories;
  private final RepositoryPermissionCache permission;

  @Inject
  private MoveIssueFeature(final IssueMoveConfiguration configuration, final Repositories repositories, final RepositoryPermissionCache permission) {
    this.configuration = configuration;
    this.repositories = repositories;
    this.permission = permission;
  }

  @Subscribe
  public void move(final IssueCommentEvent event) throws IOException {
    final Matcher matcher = this.configuration.matcher(event.comment.body);
    if(!matcher.matches()) {
      return;
    }

    final Repository sourceRepository = event.repository;
    final String tag = matcher.group(1);
    @Nullable final RepositoryId targetRepository = this.configuration.target(sourceRepository, tag);
    if(targetRepository == null) {
      LOGGER.error("Could not find target for '{}' from '{}'", tag, sourceRepository);
      return;
    }

    final net.kyori.limbo.feature.github.api.model.Issue sourceIssue = event.issue;
    LOGGER.info("Trying to move issue '{}#{}' to '{}'", sourceRepository.asString(), sourceIssue.number, targetRepository.asString());
    if(!this.permission.get(sourceRepository, event.comment.user).write() && !this.permission.get(targetRepository, event.comment.user).write()) {
      LOGGER.error("Could not move issue - '{}' is not a collaborator", event.comment.user.login);
      return;
    }

    final net.kyori.igloo.v3.Repository sourceRepo = this.repositories.get(sourceRepository);
    final net.kyori.igloo.v3.Repository targetRepo = this.repositories.get(targetRepository);

    final Issue targetIssue = targetRepo.issues().create(new Issue.Create.Full() {
      @Override
      public String title() {
        return sourceIssue.title;
      }

      @Override
      public String body() {
        return Tokens.format(
          MoveIssueFeature.this.configuration.target.comment,
          ImmutableMap.of(
            IssueMoveToken.AUTHOR, sourceIssue.user.login,
            IssueMoveToken.CONTENT, sourceIssue.body,
            IssueMoveToken.SOURCE, sourceRepository.asString(),
            IssueMoveToken.SOURCE_ID, sourceIssue.number,
            IssueMoveToken.SOURCE_URL, sourceIssue.html_url
          )
        );
      }

      @Override
      public Collection<String> assignees() {
        return sourceIssue.assignees.stream().map(user -> user.login).collect(Collectors.toSet());
      }

      @Override
      public Collection<String> labels() {
        return Stream.concat(
          sourceIssue.labels.stream().map(label -> label.name),
          MoveIssueFeature.this.configuration.target.labels.stream()
        ).collect(Collectors.toSet());
      }

      @Exclude
      @Override
      public int milestone() {
        return 0;
      }
    });

    sourceRepo.issues().get(sourceIssue.number).comments().post(Tokens.format(
      this.configuration.source.comment,
      ImmutableMap.of(
        IssueMoveToken.AUTHOR, sourceIssue.user.login,
        IssueMoveToken.TARGET, targetRepository.asString(),
        IssueMoveToken.TARGET_ID, targetIssue.number(),
        IssueMoveToken.TARGET_URL, targetIssue.html_url()
      )
    ));

    if(sourceIssue.locked) {
      targetIssue.lock();
    } else {
      if(this.configuration.source.lock) {
        sourceRepo.issues().get(sourceIssue.number).lock();
      }

      if(this.configuration.target.lock) {
        targetIssue.lock();
      }
    }

    this.configuration.source.apply(sourceRepo.issues().get(sourceIssue.number));
    this.configuration.target.apply(targetIssue);
  }
}
