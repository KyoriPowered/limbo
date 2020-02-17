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
package net.kyori.limbo.discord.embed;

import java.awt.Color;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.limbo.xml.Xml;
import net.kyori.mu.function.ThrowingConsumer;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class EmbedParser implements Parser<Embed> {
  private static final Pattern COLOR_PATTERN = Pattern.compile("[a-fA-F0-9]{6}");

  @Override
  public @NonNull Embed throwingParse(final @NonNull Node node) {
    final Embed.Builder builder = Embed.builder();
    Xml.acceptOne(node, "title", builder::title);
    Xml.acceptOne(node, "description", builder::description);
    Xml.acceptOne(node, "url", builder::url);
    node.nodes("color").one().ifPresent(ThrowingConsumer.of(color -> {
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
      Xml.acceptOne(author, "name", builder::authorName);
      Xml.acceptOne(author, "url", builder::authorUrl);
      Xml.acceptOne(author, "icon", builder::authorIcon);
    });
    Xml.acceptOne(node, "image", builder::imageUrl);
    Xml.acceptOne(node, "thumbnail", builder::thumbnailUrl);
    node.nodes("field").forEach(field -> builder.field(
      Xml.requireOneString(field, "name"),
      Xml.requireOneString(field, "value")
    ));
    node.nodes("footer").one().ifPresent(footer -> {
      Xml.acceptOne(footer, "text", builder::footerText);
      Xml.acceptOne(footer, "icon", builder::footerIcon);
    });
    return builder.build();
  }
}
