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

import com.google.common.collect.ImmutableMap;
import net.kyori.event.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.PullRequest;
import net.kyori.igloo.v3.Repositories;
import net.kyori.kassel.channel.TextChannel;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.channel.message.event.ChannelMessageCreateEvent;
import net.kyori.limbo.core.event.Listener;
import net.kyori.limbo.feature.discord.action.Action;
import net.kyori.limbo.feature.discord.embed.EmbedRenderer;
import net.kyori.limbo.feature.git.issue.IssueToken;
import net.kyori.limbo.feature.github.repository.GitHubRepositoryIdImpl;
import net.kyori.limbo.util.Tokens;
import net.kyori.lunar.Optionals;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public final class GitHubIssueRefFeature implements Listener {
  private final Configuration configuration;
  private final Repositories repositories;

  @Inject
  private GitHubIssueRefFeature(final Configuration configuration, final Repositories repositories) {
    this.configuration = configuration;
    this.repositories = repositories;
  }

  @Subscribe
  public void message(final ChannelMessageCreateEvent event) {
    Optionals.cast(event.channel(), TextChannel.class).ifPresent(channel -> this.configuration.search(event.message().content()).ifPresent(search -> {
      final Issue issue = this.repositories.get(new GitHubRepositoryIdImpl(search.repository)).issues().get(search.number);
      final @Nullable PullRequest pr = issue.pullRequest().orElse(null);
      final Action action = this.configuration.actionFor(issue, pr);
      action.message().ifPresent(message -> channel.message(
        message.content(),
        message.embed()
          .map(embed -> EmbedRenderer.render(embed, string -> Tokens.format(string, ImmutableMap.of(
            IssueToken.BODY, StringUtils.truncate(issue.body(), Embed.MAX_DESCRIPTION_LENGTH),
            IssueToken.NUMBER, issue.number(),
            Token.TAG, search.tag,
            IssueToken.TITLE, StringUtils.truncate(issue.title(), Embed.MAX_TITLE_LENGTH - search.tag.length() - 2),
            IssueToken.URL, issue.html_url()
          ))))
          .orElse(null)
      ));
    }));
  }
}