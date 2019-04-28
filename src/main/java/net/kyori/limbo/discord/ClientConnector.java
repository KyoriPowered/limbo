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
package net.kyori.limbo.discord;

import net.kyori.event.method.annotation.Subscribe;
import net.kyori.kassel.client.Client;
import net.kyori.kassel.client.shard.Shard;
import net.kyori.kassel.client.shard.event.ShardConnectedEvent;
import net.kyori.kassel.client.shard.event.ShardResumedEvent;
import net.kyori.limbo.event.Listener;
import net.kyori.membrane.facet.Activatable;
import net.kyori.membrane.facet.Connectable;

import javax.inject.Inject;

public final class ClientConnector implements Activatable, Connectable, Listener {
  private final DiscordConfiguration discord;
  private final Client client;

  @Inject
  private ClientConnector(final DiscordConfiguration discord, final Client client) {
    this.discord = discord;
    this.client = client;
  }

  @Override
  public boolean active() {
    return this.discord.isEnabled();
  }

  @Override
  public void connect() {
    this.client.connect();
  }

  @Override
  public void disconnect() {
    this.client.disconnect();
  }

  @Subscribe
  public void connected(final ShardConnectedEvent event) {
    this.refreshPresence(event.shard());
  }

  @Subscribe
  public void resumed(final ShardResumedEvent event) {
    this.refreshPresence(event.shard());
  }

  private void refreshPresence(final Shard shard) {
    final DiscordConfiguration.Presence presence = this.discord.getPresence();
    if(presence != null) {
      shard.presence(presence.getStatus(), presence.getActivityType(), presence.getActivityName());
    }
  }
}
