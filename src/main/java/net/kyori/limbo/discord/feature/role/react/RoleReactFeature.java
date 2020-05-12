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

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionAddEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionRemoveEvent;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.snowflake.Snowflaked;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.FunkyTown;
import net.kyori.limbo.discord.feature.AbstractDiscordFeature;
import net.kyori.limbo.discord.filter.RoleQuery;
import net.kyori.limbo.event.Listener;
import net.kyori.membrane.facet.Activatable;
import net.kyori.mu.Composer;
import net.kyori.mu.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RoleReactFeature extends AbstractDiscordFeature implements Activatable, Listener {
  private static final Logger LOGGER = LoggerFactory.getLogger(RoleReactFeature.class);
  // @everyone
  private static final Role EVERYONE = null;
  private static final RoleQuery EVERYONE_QUERY = null;

  private final Configuration configuration;

  @Inject
  private RoleReactFeature(final DiscordConfiguration discord, final Configuration configuration) {
    super(discord);
    this.configuration = configuration;
  }

  @Subscribe
  public void react(final ChannelMessageReactionAddEvent event) {
    Maybe.cast(event.channel(), GuildTextChannel.class).ifJust(channel -> {
      final Guild guild = channel.guild();
      guild.member(event.user().id()).ifJust(member -> this.react(guild, member, event.message(), event.emoji(), (roles, role) -> {
        LOGGER.info("Adding \"{}\" ({}) to role \"{}\" ({})", FunkyTown.globalName(member.user()), member.user().id(), role.name(), role.id());
        roles.add(role);
      }));
    });
  }

  @Subscribe
  public void react(final ChannelMessageReactionRemoveEvent event) {
    Maybe.cast(event.channel(), GuildTextChannel.class).ifJust(channel -> {
      final Guild guild = channel.guild();
      guild.member(event.user().id()).ifJust(member -> this.react(guild, member, event.message(), event.emoji(), (roles, role) -> {
        LOGGER.info("Removing \"{}\" ({}) from role \"{}\" ({})", FunkyTown.globalName(member.user()), member.user().id(), role.name(), role.id());
        roles.remove(role);
      }));
    });
  }

  private void react(final Guild guild, final Member member, final Snowflaked message, final Emoji emoji, final BiConsumer<Member.Roles, Role> consumer) {
    for(final Role memberRole : Composer.<List<Role>>accept(new ArrayList<>(), list -> {
      list.addAll(member.roles().all().collect(Collectors.toSet()));
      list.add(EVERYONE); // @everyone
    })) {
      final LongSet roles = this.configuration.search(queryFor(memberRole), message, emoji);
      if(!roles.isEmpty()) {
        FunkyTown.forEachRole(guild, roles, role -> consumer.accept(member.roles(), role));
        return;
      }
    }
  }

  private static RoleQuery queryFor(final Role role) {
    if(role == EVERYONE) return EVERYONE_QUERY;
    return () -> role;
  }
}
