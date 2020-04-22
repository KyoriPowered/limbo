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
package net.kyori.limbo.discord.feature.status;

import java.time.Instant;
import javax.inject.Inject;
import net.kyori.event.EventBus;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.fragment.filter.Filter;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.event.ChannelMessageCreateEvent;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.user.User;
import net.kyori.limbo.StartTime;
import net.kyori.limbo.discord.DiscordConfiguration;
import net.kyori.limbo.discord.feature.AbstractDiscordFeature;
import net.kyori.limbo.discord.filter.UserFilter;
import net.kyori.limbo.discord.filter.UserQuery;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.event.ShutdownEvent;
import net.kyori.limbo.time.Thyme;
import net.kyori.membrane.facet.Activatable;
import net.kyori.mu.Maybe;

public final class StatusFeature extends AbstractDiscordFeature implements Activatable, Listener {
  private static final Filter FILTER = new UserFilter(105923848263753728L);
  private final EventBus<Object> bus;
  private final Instant startTime;

  @Inject
  private StatusFeature(final DiscordConfiguration discord, final EventBus<Object> bus, final @StartTime Instant startTime) {
    super(discord);
    this.bus = bus;
    this.startTime = startTime;
  }

  @Subscribe
  public void message(final ChannelMessageCreateEvent event) {
    Maybe.cast(event.channel(), GuildTextChannel.class).ifJust(channel -> {
      final Message message = event.message();
      final User author = message.author();
      if(FILTER.allowed((UserQuery) () -> author)) {
        final String content = message.content();
        if(content.equals("~limbo uptime")) {
          channel.message("Uptime: " + Thyme.PRETTY.print(Thyme.duration(Thyme.YMWDHMS_METRIC, this.startTime, Instant.now())));
        } else if(content.equals("~limbo harakiri!")) {
          channel.message("ok, bye! x.x").thenRun(() -> this.bus.post(new ShutdownEvent(this)));
        }
      }
    });
  }
}
