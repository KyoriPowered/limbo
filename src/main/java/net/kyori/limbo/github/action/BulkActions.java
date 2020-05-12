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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.igloo.v3.Issue;
import net.kyori.igloo.v3.IssuePartial;
import net.kyori.igloo.v3.Label;
import net.kyori.mu.function.ThrowingConsumer;
import net.kyori.mu.stream.MuStreams;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BulkActions {
  private final Issue issue;
  private final net.kyori.limbo.github.api.model.@Nullable Issue source;
  private final List<Action> actions = new ArrayList<>();

  public BulkActions(final Issue issue) {
    this(issue, (net.kyori.limbo.github.api.model.Issue) null);
  }

  public BulkActions(final Issue issue, final List<Action> actions) {
    this(issue, (net.kyori.limbo.github.api.model.Issue) null);
    this.actions.addAll(actions);
  }

  public BulkActions(final Issue issue, final net.kyori.limbo.github.api.model.@Nullable Issue source) {
    this.issue = issue;
    this.source = source;
  }

  public void add(final Action action) {
    this.actions.add(action);
  }

  public void addAll(final List<Action> actions) {
    this.actions.addAll(actions);
  }

  public void apply(final Context context, final Map<String, Object> commentTokens) throws IOException {
    this.applyLabels(context.existingLabels());
    this.applyComment(commentTokens);
    this.applyState();
    this.applyLock();
  }

  private void applyLabels(final Collection<String> existing) throws IOException {
    final class Labels {
      private final Set<String> add = BulkActions.this.actions.stream()
        .flatMap(action -> action.addLabels().stream())
        .collect(Collectors.toSet());
      private final Set<String> remove = BulkActions.this.actions.stream()
        .flatMap(action -> action.removeLabels().stream())
        .collect(Collectors.toSet());

      private boolean isEmpty() {
        return this.add.isEmpty() && this.remove.isEmpty();
      }

      private void removeConflicts() {
        final Set<String> add = new HashSet<>(this.add);
        this.add.removeAll(this.remove);
        this.remove.removeAll(add);
      }

      private void removeExisting() {
        this.add.removeAll(existing);
      }
    }

    final Labels labels = new Labels();
    if(!labels.isEmpty()) {
      labels.removeConflicts();
      labels.removeExisting();

      if(!labels.remove.isEmpty()) {
        this.issue.labels().set(
          Stream
            .concat(
              labels.add.stream(),
              MuStreams.of(BulkActions.this.issue.labels().all())
                .map(Label::name)
            )
            .distinct()
            .filter(label -> !labels.remove.contains(label))
            .collect(Collectors.toSet())
        );
      } else {
        this.issue.labels().add(labels.add);
      }
    }
  }

  private void applyComment(final Map<String, Object> tokens) {
    this.actions.forEach(ThrowingConsumer.of(action -> {
      final Action.Comment comment = action.comment();
      if(comment != null) {
        this.issue.comments().post(() -> comment.render(tokens));
      }
    }));
  }

  private void applyState() throws IOException {
    final long close = this.countStates(Action.State.CLOSE);
    final long open = this.countStates(Action.State.OPEN);
    if(close > 0 || open > 0) {
      if(close > open) {
        this.issue.edit((IssuePartial.StatePartial) () -> Issue.State.CLOSED);
      } else if(open > close) {
        this.issue.edit((IssuePartial.StatePartial) () -> Issue.State.OPEN);
      }
    }
  }

  private long countStates(final Action.State state) {
    return this.actions.stream()
      .filter(action -> action.state() == state)
      .count();
  }

  private void applyLock() throws IOException {
    if(this.source != null && this.source.locked) {
      this.issue.lock();
    } else {
      final long lock = this.countLocks(Action.Lock.LOCK);
      final long unlock = this.countLocks(Action.Lock.UNLOCK);
      if(lock > 0 || unlock > 0) {
        if(lock > unlock) {
          this.issue.lock();
        } else if(unlock > lock) {
          this.issue.unlock();
        }
      }
    }
  }

  private long countLocks(final Action.Lock state) {
    return this.actions.stream()
      .filter(action -> action.lock() == state)
      .count();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .addValue(this.actions)
      .toString();
  }

  public interface Context {
    Collection<String> existingLabels();
  }
}
