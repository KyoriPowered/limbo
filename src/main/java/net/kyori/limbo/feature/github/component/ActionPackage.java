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
import net.kyori.blizzard.Nullable;
import net.kyori.igloo.v3.Issue;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ActionPackage {
  private static final Joiner JOINER = Joiner.on('\n');
  private final boolean close;
  @Nullable public final String comment;
  public final Set<String> labels;
  public final boolean lock;

  private ActionPackage(final boolean close, @Nullable final String comment, final Set<String> labels, final boolean lock) {
    this.close = close;
    this.comment = comment;
    this.labels = labels;
    this.lock = lock;
  }

  public void apply(final Issue issue) throws IOException {
    if(!this.labels.isEmpty()) {
      issue.labels().add(this.labels);
    }
    if(this.close) {
      issue.edit((Issue.Partial.State) () -> Issue.State.CLOSED);
    }
  }

  public static ActionPackage parse(final Path path, final ConfigurationNode config) throws IOException {
    final boolean close = config.getNode("close").getBoolean();
    @Nullable final String comment = config.getNode("comment").isVirtual() ? null : readMessage(path.resolve(config.getNode("comment").getString()));
    final Set<String> labels = config.getNode("label").isVirtual() ? Collections.emptySet() : new HashSet<>(config.getNode("label").getList(Types::asString));
    final boolean lock = config.getNode("lock").getBoolean();
    return new ActionPackage(close, comment, labels, lock);
  }

  private static String readMessage(final Path path) throws IOException {
    return JOINER.join(Files.readAllLines(path));
  }
}
