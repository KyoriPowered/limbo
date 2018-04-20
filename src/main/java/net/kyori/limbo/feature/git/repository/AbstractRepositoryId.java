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
package net.kyori.limbo.feature.git.repository;

import com.google.common.base.MoreObjects;
import net.kyori.lunar.EvenMoreObjects;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractRepositoryId implements RepositoryId {
  private final String user;
  private final String repo;
  private final Set<String> tags;

  protected AbstractRepositoryId(final String user, final String repo, final Set<String> tags) {
    this.user = user;
    this.repo = repo;
    this.tags = tags;
  }

  @Override
  public @NonNull String user() {
    return this.user;
  }

  @Override
  public @NonNull String repo() {
    return this.repo;
  }

  @Override
  public @NonNull Set<String> tags() {
    return this.tags;
  }

  @Override
  public boolean equals(final Object other) {
    return EvenMoreObjects.equals(RepositoryId.class, this, other, this::equals);
  }

  protected boolean equals(final RepositoryId that) {
    return Objects.equals(this.source(), that.source())
      && Objects.equals(this.user(), that.user())
      && Objects.equals(this.repo(), that.repo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.user(), this.repo());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("source", this.source())
      .add("user", this.user())
      .add("repo", this.repo())
      .add("tags", this.tags())
      .toString();
  }
}
