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
package net.kyori.limbo.github.repository;

import net.kyori.limbo.git.repository.AbstractRepositoryId;
import net.kyori.limbo.git.repository.RepositoryId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.Set;

public class GitHubRepositoryIdImpl extends AbstractRepositoryId implements GitHubRepositoryId {
  public GitHubRepositoryIdImpl(final RepositoryId id) {
    this(id.user(), id.repo(), id.tags());
  }

  public GitHubRepositoryIdImpl(final String user, final String repo, final Set<String> tags) {
    super(user, repo, tags);
  }

  @Override
  public @NonNull Source source() {
    return Source.GITHUB;
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }
    if(other == null) {
      return false;
    }
    if(other instanceof net.kyori.igloo.v3.RepositoryId) {
      final net.kyori.igloo.v3.RepositoryId that = (net.kyori.igloo.v3.RepositoryId) other;
      return Objects.equals(this.user(), that.user())
        && Objects.equals(this.repo(), that.repo());
    }
    return super.equals(other);
  }
}
