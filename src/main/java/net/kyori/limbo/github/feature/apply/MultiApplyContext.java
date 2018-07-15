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
package net.kyori.limbo.github.feature.apply;

import net.kyori.igloo.v3.Issue;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.BulkActions;

import java.util.Collection;
import java.util.function.Consumer;

/* package */ class MultiApplyContext implements BulkActions.Context {
  private final Builder builder;
  private final BulkActions applicators;

  /* package */ static Builder builder() {
    return new Builder();
  }

  private MultiApplyContext(final Builder builder) {
    this.builder = builder;
    this.applicators = new BulkActions(builder.issue);
  }

  /* package */ ApplyContext.Builder child() {
    return ApplyContext.builder()
      .repository(this.builder.repository)
      .issue(this.builder.issue);
  }

  /* package */ BulkActions applicators() {
    return this.applicators;
  }

  /* package */ void collectApplicators(final Consumer<BulkActions> applicators) {
    applicators.accept(this.applicators);
  }

  @Override
  public Collection<String> existingLabels() {
    return this.builder.labels;
  }

  /* package */ static class Builder {
    private Issue issue;
    private RepositoryId repository;
    private Collection<String> labels;

    /* package */ Builder issue(final Issue issue) {
      this.issue = issue;
      return this;
    }

    /* package */ Builder repository(final RepositoryId repository) {
      this.repository = repository;
      return this;
    }

    /* package */ Builder labels(final Collection<String> labels) {
      this.labels = labels;
      return this;
    }

    /* package */ MultiApplyContext build() {
      return new MultiApplyContext(this);
    }
  }
}
