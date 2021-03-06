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
package net.kyori.limbo.discord.feature.role.auto;

import javax.inject.Inject;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.member.event.GuildMemberAddEvent;
import net.kyori.kassel.user.User;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.FunkyTown;
import net.kyori.limbo.discord.feature.AbstractDiscordFeature;
import net.kyori.limbo.discord.filter.MemberQuery;
import net.kyori.limbo.event.Listener;
import net.kyori.membrane.facet.Activatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoRoleFeature extends AbstractDiscordFeature implements Activatable, Listener {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoRoleFeature.class);
  private final Configuration configuration;

  @Inject
  private AutoRoleFeature(final DiscordConfiguration discord, final Configuration configuration) {
    super(discord);
    this.configuration = configuration;
  }

  @Subscribe
  public void add(final GuildMemberAddEvent event) {
    final Guild guild = event.guild();
    final Member member = event.member();

    this.configuration.search((MemberQuery) () -> member).ifJust(roles -> {
      final User user = member.user();
      FunkyTown.forEachRole(guild, roles, role -> {
        LOGGER.info("Adding \"{}\" ({}) to role \"{}\" ({})", FunkyTown.globalName(user), user.id(), role.name(), role.id());
        member.roles().add(role);
      });
    });
  }
}
