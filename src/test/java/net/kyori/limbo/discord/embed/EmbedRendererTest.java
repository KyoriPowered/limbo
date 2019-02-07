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

import net.kyori.kassel.channel.message.embed.Embed;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbedRendererTest {
  @Test
  void testRenderAll() {
    final Embed embed = EmbedRenderer.render(
      Embed.builder()
        .title("${title}")
        .description("${description}")
        .url("${url}")
        .authorName("${author_name}")
        .authorUrl("${author_url}")
        .authorIcon("${author_icon}")
        .imageUrl("${image_url}")
        .thumbnailUrl("${thumbnail_url}")
        .field("${field_name_1}", "${field_value_1}")
        .field("${field_name_2}", "${field_value_2}")
        .footerText("${footer_text}")
        .footerIcon("${footer_icon}")
        .build(),
      s -> s.replaceAll("\\$\\{.*}", "bar")
    );
    assertEquals("bar", string(embed.title()));
    assertEquals("bar", string(embed.description()));
    assertEquals("bar", string(embed.url()));
    assertEquals("bar", string(embed.author().flatMap(Embed.Author::name)));
    assertEquals("bar", string(embed.author().flatMap(Embed.Author::url)));
    assertEquals("bar", string(embed.author().flatMap(Embed.Author::icon)));
    assertEquals("bar", string(embed.image().flatMap(Embed.Image::url)));
    assertEquals("bar", string(embed.thumbnail().flatMap(Embed.Thumbnail::url)));
    assertEquals("bar", embed.fields().get(0).name());
    assertEquals("bar", embed.fields().get(0).value());
    assertEquals("bar", embed.fields().get(1).name());
    assertEquals("bar", embed.fields().get(1).value());
    assertEquals("bar", string(embed.footer().flatMap(Embed.Footer::text)));
    assertEquals("bar", string(embed.footer().flatMap(Embed.Footer::icon)));
  }

  @Test
  void testRenderOneByOne() {
    final Embed embed = EmbedRenderer.render(
      Embed.builder()
        .title("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .description("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .url("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .authorName("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .authorUrl("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .authorIcon("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .imageUrl("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .thumbnailUrl("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .field("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", "${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .field("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", "${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .footerText("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .footerIcon("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}")
        .build(),
      string -> string.replace("${title}", "title"),
      string -> string.replace("${description}", "description"),
      string -> string.replace("${url}", "url"),
      string -> string.replace("${author_name}", "author_name"),
      string -> string.replace("${author_url}", "author_url"),
      string -> string.replace("${author_icon}", "author_icon"),
      string -> string.replace("${image_url}", "image_url"),
      string -> string.replace("${thumbnail_url}", "thumbnail_url"),
      string -> string.replace("${field_name}", "field_name"),
      string -> string.replace("${field_value}", "field_value"),
      string -> string.replace("${footer_text}", "footer_text"),
      string -> string.replace("${footer_icon}", "footer_icon")
    );
    assertEquals("title ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.title()));
    assertEquals("${title} description ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.description()));
    assertEquals("${title} ${description} url ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.url()));
    assertEquals("${title} ${description} ${url} author_name ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.author().flatMap(Embed.Author::name)));
    assertEquals("${title} ${description} ${url} ${author_name} author_url ${author_icon} ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.author().flatMap(Embed.Author::url)));
    assertEquals("${title} ${description} ${url} ${author_name} ${author_url} author_icon ${image_url} ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.author().flatMap(Embed.Author::icon)));
    assertEquals("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} image_url ${thumbnail_url} ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.image().flatMap(Embed.Image::url)));
    assertEquals("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} thumbnail_url ${field_name} ${field_value} ${footer_text} ${footer_icon}", string(embed.thumbnail().flatMap(Embed.Thumbnail::url)));
    assertEquals("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} field_name ${field_value} ${footer_text} ${footer_icon}", embed.fields().get(0).name());
    assertEquals("${title} ${description} ${url} ${author_name} ${author_url} ${author_icon} ${image_url} ${thumbnail_url} ${field_name} field_value ${footer_text} ${footer_icon}", embed.fields().get(0).value());
  }

  @Test
  void testEdit() {
    final Embed embed = EmbedRenderer.edited(
      Embed.builder()
        .title("title_a")
        .authorName("author_a")
        .footerText("footer_a")
        .build(),
      Embed.builder()
        .title("title_b")
        .authorName("author_b")
        .build()
    );
    assertEquals("title_b", string(embed.title()));
    assertEquals("author_b", string(embed.author().flatMap(Embed.Author::name)));
    assertEquals("footer_a", string(embed.footer().flatMap(Embed.Footer::text)));
  }

  private static String string(final Optional<String> string) {
    return string.orElse("null");
  }
}
