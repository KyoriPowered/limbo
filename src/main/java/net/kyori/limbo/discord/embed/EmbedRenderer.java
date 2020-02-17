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

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.mu.Maybe;

public interface EmbedRenderer {
  static Embed render(final Embed embed, final UnaryOperator<String> renderer) {
    return render(embed, Operators.of(renderer));
  }

  static Embed render(final Embed embed, final Operators operators) {
    final Embed.Builder builder = Embed.builder();
    return render(builder, embed, operators);
  }

  static Embed render(final Embed.Builder builder, final Embed embed, final Operators operators) {
    apply(embed.title(), operators.title, builder::title);
    apply(embed.description(), operators.description, builder::description);
    apply(embed.url(), operators.url, builder::url);
    embed.color().ifJust(builder::color);
    embed.timestamp().ifJust(builder::timestamp);
    embed.author().ifJust(author -> {
      apply(author.name(), operators.authorName, builder::authorName);
      apply(author.url(), operators.authorUrl, builder::authorUrl);
      apply(author.icon(), operators.authorIcon, builder::authorIcon);
    });
    embed.image().ifJust(image -> apply(image.url(), operators.imageUrl, builder::imageUrl));
    embed.thumbnail().ifJust(thumbnail -> apply(thumbnail.url(), operators.thumbnailUrl, builder::thumbnailUrl));
    embed.fields().forEach(field -> builder.field(operators.fieldName.apply(field.name()), operators.fieldValue.apply(field.value())));
    embed.footer().ifJust(footer -> {
      apply(footer.text(), operators.footerText, builder::footerText);
      apply(footer.icon(), operators.footerIcon, builder::footerIcon);
    });
    return builder.build();
  }

  static Embed edited(final Embed original, final Embed source) {
    return edited(original, source, UnaryOperator.identity());
  }

  static Embed edited(final Embed original, final Embed source, final UnaryOperator<String> renderer) {
    final Embed.Builder builder = original.toBuilder();
    return render(builder, source, Operators.of(renderer));
  }

  static void apply(final Maybe<String> oldValue, final UnaryOperator<String> newValue, final Consumer<String> setter) {
    oldValue.ifJust(value -> setter.accept(newValue.apply(value)));
  }

  class Operators {
    UnaryOperator<String> title;
    UnaryOperator<String> description;
    UnaryOperator<String> url;
    UnaryOperator<String> authorName;
    UnaryOperator<String> authorUrl;
    UnaryOperator<String> authorIcon;
    UnaryOperator<String> imageUrl;
    UnaryOperator<String> thumbnailUrl;
    UnaryOperator<String> fieldName;
    UnaryOperator<String> fieldValue;
    UnaryOperator<String> footerText;
    UnaryOperator<String> footerIcon;

    public static Operators create() {
      return new Operators(UnaryOperator.identity());
    }

    public static Operators of(final UnaryOperator<String> operator) {
      return new Operators(operator);
    }

    private Operators(final UnaryOperator<String> operator) {
      this.title = operator;
      this.description = operator;
      this.url = operator;
      this.authorName = operator;
      this.authorUrl = operator;
      this.authorIcon = operator;
      this.imageUrl = operator;
      this.thumbnailUrl = operator;
      this.fieldName = operator;
      this.fieldValue = operator;
      this.footerText = operator;
      this.footerIcon = operator;
    }

    public Operators title(final UnaryOperator<String> title) {
      this.title = title;
      return this;
    }

    public Operators description(final UnaryOperator<String> description) {
      this.description = description;
      return this;
    }

    public Operators url(final UnaryOperator<String> url) {
      this.url = url;
      return this;
    }

    public Operators authorName(final UnaryOperator<String> authorName) {
      this.authorName = authorName;
      return this;
    }

    public Operators authorUrl(final UnaryOperator<String> authorUrl) {
      this.authorUrl = authorUrl;
      return this;
    }

    public Operators authorIcon(final UnaryOperator<String> authorIcon) {
      this.authorIcon = authorIcon;
      return this;
    }

    public Operators imageUrl(final UnaryOperator<String> imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    public Operators thumbnailUrl(final UnaryOperator<String> thumbnailUrl) {
      this.thumbnailUrl = thumbnailUrl;
      return this;
    }

    public Operators fieldName(final UnaryOperator<String> fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Operators fieldValue(final UnaryOperator<String> fieldValue) {
      this.fieldValue = fieldValue;
      return this;
    }

    public Operators footerText(final UnaryOperator<String> footerText) {
      this.footerText = footerText;
      return this;
    }

    public Operators footerIcon(final UnaryOperator<String> footerIcon) {
      this.footerIcon = footerIcon;
      return this;
    }
  }
}
