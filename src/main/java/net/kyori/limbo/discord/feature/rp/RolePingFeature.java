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
package net.kyori.limbo.discord.feature.rp;

import com.google.common.collect.ImmutableMap;
import net.kyori.event.Subscribe;
import net.kyori.kassel.channel.TextChannel;
import net.kyori.kassel.channel.message.event.ChannelMessageCreateEvent;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.guild.role.RolePartial;
import net.kyori.kassel.user.User;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.action.Action;
import net.kyori.limbo.discord.embed.EmbedRenderer;
import net.kyori.limbo.discord.filter.RoleQuery;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.util.Tokens;
import net.kyori.lunar.Optionals;
import net.kyori.membrane.facet.Activatable;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class RolePingFeature implements Activatable, Listener {
  private final DiscordConfiguration discord;
  private final Configuration configuration;

  @Inject
  private RolePingFeature(final DiscordConfiguration discord, final Configuration configuration) {
    this.discord = discord;
    this.configuration = configuration;
  }

  @Override
  public boolean active() {
    return this.discord.isEnabled();
  }

  @Subscribe
  public void message(final ChannelMessageCreateEvent event) {
    Optionals.cast(event.channel(), GuildTextChannel.class).ifPresent(channel -> {
      final Guild guild = channel.guild();
      final User author = event.message().author();
      guild.member(author.id()).ifPresent(member -> {
        final String content = event.message().content();
        for(final Role memberRole : member.roles().all().collect(Collectors.toSet())) {
          final Optional<Configuration.SearchResult> searchResult = this.configuration.search((RoleQuery) () -> memberRole, content);
          if(searchResult.isPresent()) {
            final Configuration.Search search = searchResult.get().search;
            guild.role(search.role).ifPresent(role -> this.ping(author, role, channel, search.action, searchResult.get().content));
            return;
          }
        }
      });
    });
  }

  private void ping(final User source, final Role role, final TextChannel channel, final Action action, final String content) {
    action.message().ifPresent(message -> {
      final boolean mentionable = role.mentionable();
      if(!mentionable) {
        role.edit((RolePartial.MentionablePartial) () -> true);
      }

      channel.message(
        Tokens.format(message.content(), ImmutableMap.of(
          RolePingTokens.MESSAGE, content,
          RolePingTokens.SOURCE, source.mention(),
          RolePingTokens.ROLE, role.mention()
        ))
      ).thenRun(() -> {
        if(!mentionable) {
          role.edit((RolePartial.MentionablePartial) () -> false);
        }
      });
    });
  }
}
