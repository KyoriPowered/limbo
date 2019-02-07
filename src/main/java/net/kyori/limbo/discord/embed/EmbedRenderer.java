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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface EmbedRenderer {
  static Embed render(final Embed.Builder builder, final Embed embed, final UnaryOperator<String> renderer) {
    return render(builder, embed, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer);
  }

  static Embed render(final Embed embed, final UnaryOperator<String> renderer) {
    return render(embed, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer, renderer);
  }

  static Embed render(final Embed embed, final UnaryOperator<String> title, final UnaryOperator<String> description, final UnaryOperator<String> url, final UnaryOperator<String> authorName, final UnaryOperator<String> authorUrl, final UnaryOperator<String> authorIcon, final UnaryOperator<String> imageUrl, final UnaryOperator<String> thumbnailUrl, final UnaryOperator<String> fieldName, final UnaryOperator<String> fieldValue, final UnaryOperator<String> footerText, final UnaryOperator<String> footerIcon) {
    final Embed.Builder builder = Embed.builder();
    return render(builder, embed, title, description, url, authorName, authorUrl, authorIcon, imageUrl, thumbnailUrl, fieldName, fieldValue, footerText, footerIcon);
  }

  static Embed render(final Embed.Builder builder, final Embed embed, final UnaryOperator<String> title, final UnaryOperator<String> description, final UnaryOperator<String> url, final UnaryOperator<String> authorName, final UnaryOperator<String> authorUrl, final UnaryOperator<String> authorIcon, final UnaryOperator<String> imageUrl, final UnaryOperator<String> thumbnailUrl, final UnaryOperator<String> fieldName, final UnaryOperator<String> fieldValue, final UnaryOperator<String> footerText, final UnaryOperator<String> footerIcon) {
    apply(embed.title(), title, builder::title);
    apply(embed.description(), description, builder::description);
    apply(embed.url(), url, builder::url);
    embed.color().ifPresent(builder::color);
    embed.timestamp().ifPresent(builder::timestamp);
    embed.author().ifPresent(author -> {
      apply(author.name(), authorName, builder::authorName);
      apply(author.url(), authorUrl, builder::authorUrl);
      apply(author.icon(), authorIcon, builder::authorIcon);
    });
    embed.image().ifPresent(image -> apply(image.url(), imageUrl, builder::imageUrl));
    embed.thumbnail().ifPresent(thumbnail -> apply(thumbnail.url(), thumbnailUrl, builder::thumbnailUrl));
    embed.fields().forEach(field -> builder.field(fieldName.apply(field.name()), fieldValue.apply(field.value())));
    embed.footer().ifPresent(footer -> {
      apply(footer.text(), footerText, builder::footerText);
      apply(footer.icon(), footerIcon, builder::footerIcon);
    });
    return builder.build();
  }

  static Embed edited(final Embed original, final Embed source) {
    return edited(original, source, UnaryOperator.identity());
  }

  static Embed edited(final Embed original, final Embed source, final UnaryOperator<String> renderer) {
    final Embed.Builder builder = original.toBuilder();
    return render(builder, source, renderer);
  }

  static void apply(final Optional<String> oldValue, final UnaryOperator<String> newValue, final Consumer<String> setter) {
    oldValue.ifPresent(value -> setter.accept(newValue.apply(value)));
  }
}
