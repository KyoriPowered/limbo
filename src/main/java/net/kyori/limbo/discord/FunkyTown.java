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

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.user.User;
import net.kyori.mu.Maybe;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.stream.NodeStream;

public interface FunkyTown {
  static String globalName(final User user) {
    return String.format("%s#%s", user.username(), user.discriminator());
  }

  static LongSet longs(final NodeStream nodes, final Parser<Long> parser) {
    final LongSet longs = new LongArraySet();
    nodes.map(parser).mapToLong(Long::longValue).forEach(longs::add);
    return longs;
  }

  static void forEachRole(final Guild guild, final LongSet roles, final Consumer<Role> consumer) {
    LongStream.of(roles.toLongArray())
      .mapToObj(guild::role)
      .filter(Maybe::isJust)
      .map(Maybe::orThrow)
      .forEach(consumer);
  }
}
