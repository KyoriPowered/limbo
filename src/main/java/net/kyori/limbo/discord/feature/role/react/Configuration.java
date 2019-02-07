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

import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.kassel.channel.message.emoji.CustomEmoji;
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.kassel.snowflake.Snowflaked;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import javax.inject.Singleton;

@Singleton
/* package */ final class Configuration {
  final List<Reaction> reactions = new ArrayList<>();

  @NonNull OptionalLong search(final FilterQuery query, final Snowflaked message, final Emoji emoji) {
    final String id = emoji instanceof CustomEmoji ? String.valueOf(((CustomEmoji) emoji).id()) : emoji.name();
    for(final Reaction reaction : this.reactions) {
      if(reaction.message == message.id() && reaction.emoji.equals(id)) {
        if(reaction.filter == null || reaction.filter.allowed(query)) {
          return OptionalLong.of(reaction.role);
        }
      }
    }
    return OptionalLong.empty();
  }

  static class Reaction {
    final @Snowflake long message;
    final String emoji;
    final @Nullable Filter filter;
    final @Snowflake long role;

    Reaction(final @Snowflake long message, final String emoji, final @Nullable Filter filter, final @Snowflake long role) {
      this.message = message;
      this.emoji = emoji;
      this.filter = filter;
      this.role = role;
    }
  }
}
