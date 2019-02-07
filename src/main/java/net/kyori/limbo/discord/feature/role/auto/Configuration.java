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

import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.kassel.user.User;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
/* package */ final class Configuration {
  final List<Entry> entries = new ArrayList<>();

  @NonNull Optional<LongSet> search(final Guild guild, final User user) {
    for(final Entry entry : this.entries) {
      if(entry.guild == guild.id() && entry.user == user.id()) {
        return Optional.of(entry.roles);
      }
    }
    return Optional.empty();
  }

  static class Entry {
    final @Snowflake long guild;
    final @Snowflake long user;
    final LongSet roles;

    Entry(final @Snowflake long guild, final @Snowflake long user, final @NonNull LongSet roles) {
      this.guild = guild;
      this.user = user;
      this.roles = roles;
    }
  }
}
