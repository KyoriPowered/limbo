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
package net.kyori.limbo.github.action;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import net.kyori.feature.FeatureDefinition;
import net.kyori.igloo.v3.Issue;
import net.kyori.limbo.util.Tokens;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Action extends FeatureDefinition {
  @Nullable State state();

  @Nullable Comment comment();

  @NonNull Set<String> addLabels();

  @NonNull Set<String> removeLabels();

  @Nullable Lock lock();

  default void apply(final Issue issue) throws IOException {
    this.apply(issue, null, null);
  }

  default void apply(final Issue issue, final @Nullable Map<String, Object> comment) throws IOException {
    this.apply(issue, comment, null);
  }

  default void apply(final Issue issue, final net.kyori.limbo.github.api.model.@Nullable Issue source) throws IOException {
    this.apply(issue, null, source);
  }

  void apply(final Issue issue, final @Nullable Map<String, Object> comment, final net.kyori.limbo.github.api.model.@Nullable Issue source) throws IOException;

  class Comment {
    final String comment;
    final Map<String, String> tokens;

    Comment(final String comment, final Map<String, String> tokens) {
      this.comment = comment;
      this.tokens = tokens;
    }

    public String render() {
      return this.render0(this.tokens);
    }

    public String render(final Map<String, Object> tokens) {
      return this.render0(
        ImmutableMap.<String, Object>builder()
        .putAll(this.tokens)
        .putAll(tokens)
        .build()
      );
    }

    private String render0(final Map<String, ?> tokens) {
      return Tokens.format(this.comment, tokens);
    }
  }

  enum State {
    OPEN,
    CLOSE;
  }

  enum Lock {
    LOCK,
    UNLOCK;
  }
}
