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
package net.kyori.limbo.discord.feature.role.react;

import net.kyori.event.Subscribe;
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionAddEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionRemoveEvent;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.snowflake.Snowflaked;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.filter.RoleQuery;
import net.kyori.limbo.event.Listener;
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.lunar.Optionals;
import net.kyori.membrane.facet.Activatable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class RoleReactFeature implements Activatable, Listener {
  private final DiscordConfiguration discord;
  private final Configuration configuration;

  @Inject
  private RoleReactFeature(final DiscordConfiguration discord, final Configuration configuration) {
    this.discord = discord;
    this.configuration = configuration;
  }

  @Override
  public boolean active() {
    return this.discord.isEnabled();
  }

  @Subscribe
  public void react(final ChannelMessageReactionAddEvent event) {
    Optionals.cast(event.channel(), GuildTextChannel.class).ifPresent(channel -> {
      final Guild guild = channel.guild();
      guild.member(event.user().id()).ifPresent(member -> this.react(guild, member, event.message(), event.emoji(), Member.Roles::add));
    });
  }

  @Subscribe
  public void react(final ChannelMessageReactionRemoveEvent event) {
    Optionals.cast(event.channel(), GuildTextChannel.class).ifPresent(channel -> {
      final Guild guild = channel.guild();
      guild.member(event.user().id()).ifPresent(member -> this.react(guild, member, event.message(), event.emoji(), Member.Roles::remove));
    });
  }

  private void react(final Guild guild, final Member member, final Snowflaked message, final Emoji emoji, final BiConsumer<Member.Roles, Role> consumer) {
    for(final Role memberRole : EvenMoreObjects.<List<Role>>make(new ArrayList<>(), list -> {
      list.addAll(member.roles().all().collect(Collectors.toSet()));
      list.add(null); // @everyone
    })) {
      final OptionalLong maybeRole = this.configuration.search(memberRole != null ? (RoleQuery) () -> memberRole : null, message, emoji);
      if(maybeRole.isPresent()) {
        guild.role(maybeRole.getAsLong()).ifPresent(role -> consumer.accept(member.roles(), role));
        return;
      }
    }
  }
}
