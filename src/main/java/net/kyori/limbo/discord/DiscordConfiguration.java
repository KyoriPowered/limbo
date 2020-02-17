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

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import net.kyori.kassel.user.Activity;
import net.kyori.kassel.user.Status;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.gateway.GatewayIntent;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("discord")
public class DiscordConfiguration implements PolarConfiguration {
  private boolean enabled;
  private int shards;
  private String token;
  private boolean privileged;
  private Presence presence;

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public @NonNegative int shards() {
    return this.shards;
  }

  public int getShards() {
    return this.shards;
  }

  public void setShards(final int shards) {
    this.shards = shards;
  }

  @Override
  public @NonNull String token() {
    return this.token;
  }

  public String getToken() {
    return this.token;
  }

  public void setToken(final String token) {
    this.token = token;
  }

  @Override
  public @NonNull Set<GatewayIntent> intents() {
    if(this.isPrivileged()) {
      return Sets.union(
        GatewayIntent.defaults(),
        EnumSet.of(GatewayIntent.GUILD_MEMBERS)
      );
    }
    return PolarConfiguration.super.intents();
  }

  public boolean isPrivileged() {
    return this.privileged;
  }

  public void setPrivileged(final boolean privileged) {
    this.privileged = privileged;
  }

  public Presence getPresence() {
    return this.presence;
  }

  public void setPresence(final Presence presence) {
    this.presence = presence;
  }

  public static class Presence {
    private Status status = Status.ONLINE;
    private Activity activityType;
    private String activityName;

    public Status getStatus() {
      return this.status;
    }

    public void setStatus(final Status status) {
      this.status = status;
    }

    public Activity getActivityType() {
      return this.activityType;
    }

    public void setActivityType(final Activity activityType) {
      this.activityType = activityType;
    }

    public String getActivityName() {
      return this.activityName;
    }

    public void setActivityName(final String activityName) {
      this.activityName = activityName;
    }
  }
}
