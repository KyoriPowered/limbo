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
package net.kyori.limbo.discord.action;

import com.google.common.base.Function;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ActionParser implements Parser<Action> {
  private final Parser<Embed> embedParser;

  @Inject
  private ActionParser(final Parser<Embed> embedParser) {
    this.embedParser = embedParser;
  }

  @Override
  public @NonNull Action throwingParse(final @NonNull Node node) {
    final Action.Message message = node.nodes("message")
      .one()
      .map((Function<Node, Action.Message>) m -> {
        final String content = m.nodes("content").one().map(Node::value).want().orElse("");
        final @Nullable Embed embed = m.nodes("embed").one().map(this.embedParser::parse).want().orElse(null);
        return new Action.Message() {
          @Override
          public @NonNull String content() {
            return content;
          }

          @Override
          public @NonNull Optional<Embed> embed() {
            return Optional.ofNullable(embed);
          }
        };
      }).want().orElse(null);
    return new ActionImpl(message);
  }
}
