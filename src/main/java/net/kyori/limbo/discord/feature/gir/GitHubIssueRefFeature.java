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
package net.kyori.limbo.discord.feature.gir;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.PullRequest;
import net.kyori.igloo.v3.Repositories;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.channel.message.event.ChannelMessageCreateEvent;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.action.Action;
import net.kyori.limbo.discord.embed.EmbedRenderer;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.git.issue.IssueTokens;
import net.kyori.limbo.github.repository.GitHubRepositoryIdImpl;
import net.kyori.limbo.util.Tokens;
import net.kyori.membrane.facet.Activatable;
import net.kyori.mu.Maybe;
import org.apache.commons.lang3.StringUtils;

public final class GitHubIssueRefFeature implements Activatable, Listener {
  private final DiscordConfiguration discord;
  private final Configuration configuration;
  private final Repositories repositories;

  @Inject
  private GitHubIssueRefFeature(final DiscordConfiguration discord, final Configuration configuration, final Repositories repositories) {
    this.discord = discord;
    this.configuration = configuration;
    this.repositories = repositories;
  }

  @Override
  public boolean active() {
    return this.discord.isEnabled();
  }

  @Subscribe
  public void message(final ChannelMessageCreateEvent event) {
    Maybe.cast(event.channel(), GuildTextChannel.class).ifJust(channel -> this.configuration.search(channel.guild(), event.message().content()).ifJust(search -> {
      final Issue issue = this.repositories.get(new GitHubRepositoryIdImpl(search.repository)).issues().get(search.number);
      final /* @Nullable */ PullRequest pr = issue.pullRequest().orElse(null);
      final Action action = this.configuration.actionFor(issue, pr);
      action.message().ifJust(message -> channel.message(
        message.content(),
        message.embed()
          .map(embed -> {
            final Map<String, Object> tokens = ImmutableMap.<String, Object>builder()
              .put(IssueTokens.AUTHOR_USERNAME, issue.user().login())
              .put(IssueTokens.BODY, body(issue.body()))
              .put(IssueTokens.NUMBER, issue.number())
              .put(IssueTokens.TITLE, StringUtils.truncate(issue.title(), Embed.MAX_TITLE_LENGTH - search.tag.length() - 2))
              .put(IssueTokens.URL, issue.html_url())
              .put(GitHubIssueRefTokens.TAG, search.tag)
              .build();
            return EmbedRenderer.render(embed, EmbedRenderer.Operators.of(string -> Tokens.format(string, tokens)));
          })
          .orDefault(null)
      ));
    }));
  }

  private static String body(final String body) {
    final String truncated = StringUtils.truncate(body, Embed.MAX_DESCRIPTION_LENGTH - "...".length());
    if(!body.equals(truncated)) {
      return truncated + "...";
    }
    return body;
  }
}
