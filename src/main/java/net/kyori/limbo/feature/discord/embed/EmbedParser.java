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
package net.kyori.limbo.feature.discord.embed;

import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.Color;
import java.util.regex.Pattern;

import javax.inject.Singleton;

@Singleton
public final class EmbedParser implements Parser<Embed> {
  private static final Pattern COLOR_PATTERN = Pattern.compile("[a-fA-F0-9]{6}");

  @Override
  public @NonNull Embed throwingParse(final @NonNull Node node) {
    final Embed.Builder builder = Embed.builder();
    node.nodes("title").one().ifPresent(title -> builder.title(title.value()));
    node.nodes("description").one().ifPresent(description -> builder.description(description.value()));
    node.nodes("url").one().ifPresent(url -> builder.url(url.value()));
    node.nodes("color").one().ifPresent(Exceptions.rethrowConsumer(color -> {
      final String string = color.value();
      if(string.charAt(0) != '#') {
        throw new XMLException(color, "Color must be a hex value");
      }
      if(!COLOR_PATTERN.matcher(string.substring(1)).matches()) {
        throw new XMLException(color, "Invalid hex format");
      }
      builder.color(new Color(Integer.parseInt(string.substring(1), 16)));
    }));
    node.nodes("author").one().ifPresent(author -> {
      author.nodes("name").one().ifPresent(name -> builder.authorName(name.value()));
      author.nodes("url").one().ifPresent(url -> builder.authorUrl(url.value()));
      author.nodes("icon").one().ifPresent(icon -> builder.authorIcon(icon.value()));
    });
    node.nodes("image").one().ifPresent(image -> builder.imageUrl(image.value()));
    node.nodes("thumbnail").one().ifPresent(thumbnail -> builder.thumbnailUrl(thumbnail.value()));
    node.nodes("field").forEach(field -> builder.field(
      field.nodes("name").one().need().value(),
      field.nodes("value").one().need().value()
    ));
    node.nodes("footer").one().ifPresent(footer -> {
      footer.nodes("text").one().ifPresent(text -> builder.footerText(text.value()));
      footer.nodes("icon").one().ifPresent(icon -> builder.footerIcon(icon.value()));
    });
    return builder.build();
  }
}
