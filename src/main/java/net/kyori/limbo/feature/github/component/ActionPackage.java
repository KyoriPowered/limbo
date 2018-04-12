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
package net.kyori.limbo.feature.github.component;

import com.google.common.base.Joiner;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.Label;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ActionPackage {
  private static final Joiner JOINER = Joiner.on('\n');
  private final boolean close;
  public final @Nullable String comment;
  public final Set<String> addLabels;
  private final Set<String> removeLabels;
  public final boolean lock;

  private ActionPackage(final boolean close, final @Nullable String comment, final Set<String> addLabels, final Set<String> removeLabels, final boolean lock) {
    this.close = close;
    this.comment = comment;
    this.addLabels = addLabels;
    this.removeLabels = removeLabels;
    this.lock = lock;
  }

  public void apply(final Issue issue) throws IOException {
    if(!this.addLabels.isEmpty() || !this.removeLabels.isEmpty()) {
      if(!this.removeLabels.isEmpty()) {
        issue.labels().set(Stream.concat(
          this.addLabels.stream(),
          !this.removeLabels.isEmpty() ? StreamSupport.stream(issue.labels().all().spliterator(), false).map(Label::name) : Stream.empty()
        ).filter(label -> !this.removeLabels.contains(label)).collect(Collectors.toSet()));
      } else {
        issue.labels().add(this.addLabels);
      }
    }
    if(this.close) {
      issue.edit((Issue.Partial.State) () -> Issue.State.CLOSED);
    }
  }

  public static ActionPackage parse(final Path featureRoot, final ConfigurationNode config) throws IOException {
    final boolean close = config.getNode("close").getBoolean();
    final @Nullable String comment = config.getNode("comment").isVirtual() ? null : readMessage(featureRoot.resolve("message").resolve(config.getNode("comment").getString()));
    final Set<String> addLabels;
    final Set<String> removeLabels;
    if(config.getNode("label").isVirtual()) {
      addLabels = Collections.emptySet();
      removeLabels = Collections.emptySet();
    } else {
      addLabels = new HashSet<>(config.getNode("label", "add").getList(Types::asString));
      removeLabels = new HashSet<>(config.getNode("label", "remove").getList(Types::asString));
    }
    final boolean lock = config.getNode("lock").getBoolean();
    return new ActionPackage(close, comment, addLabels, removeLabels, lock);
  }

  private static String readMessage(final Path path) throws IOException {
    return JOINER.join(Files.readAllLines(path));
  }
}
