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

import com.google.common.base.MoreObjects;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.IssuePartial;
import net.kyori.igloo.v3.Label;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/* package */ final class ActionImpl implements Action {
  private final @Nullable State state;
  private final @Nullable Comment comment;
  private final Set<String> addLabels;
  private final Set<String> removeLabels;
  private final @Nullable Lock lock;

  /* package */ ActionImpl(final @Nullable State state, final @Nullable Comment comment, final Set<String> addLabels, final Set<String> removeLabels, final @Nullable Lock lock) {
    this.state = state;
    this.comment = comment;
    this.addLabels = addLabels;
    this.removeLabels = removeLabels;
    this.lock = lock;
  }

  @Override
  public @Nullable State state() {
    return this.state;
  }

  @Override
  public @Nullable Comment comment() {
    return this.comment;
  }

  @Override
  public @NonNull Set<String> addLabels() {
    return this.addLabels;
  }

  @Override
  public @NonNull Set<String> removeLabels() {
    return this.removeLabels;
  }

  @Override
  public @Nullable Lock lock() {
    return this.lock;
  }

  @Override
  public void apply(final Issue issue, final @Nullable Map<String, Object> comment, final net.kyori.limbo.github.api.model.@Nullable Issue source) throws IOException {
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
    if(comment != null && this.comment != null) {
      issue.comments().post(() -> this.comment.render(comment));
    }
    if(this.state != null) {
      switch(this.state) {
        case CLOSE: issue.edit((IssuePartial.StatePartial) () -> Issue.State.CLOSED); break;
        case OPEN: issue.edit((IssuePartial.StatePartial) () -> Issue.State.OPEN); break;
      }
    }
    if(source != null && source.locked) {
      issue.lock();
    } else {
      if(this.lock != null) {
        switch(this.lock) {
          case LOCK: issue.lock(); break;
          case UNLOCK: issue.unlock(); break;
        }
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("state", this.state)
      .add("comment", this.comment)
      .add("addLabels", this.addLabels)
      .add("removeLabels", this.removeLabels)
      .add("lock", this.lock)
      .toString();
  }
}
