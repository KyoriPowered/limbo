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
package net.kyori.limbo.github.api.model;

import com.google.common.base.MoreObjects;
import net.kyori.limbo.github.repository.GitHubRepositoryId;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public final class Repository implements GitHubRepositoryId {
  public User owner;
  public String name;

  public Repository() {
  }

  public Repository(final User owner, final String name) {
    this.owner = owner;
    this.name = name;
  }

  @Override
  public @NonNull Source source() {
    return Source.GITHUB;
  }

  @Override
  public String user() {
    return this.owner.login;
  }

  @Override
  public String repo() {
    return this.name;
  }

  @Override
  public @NonNull Set<String> tags() {
    return Collections.emptySet();
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }
    if(other == null || !(other instanceof GitHubRepositoryId)) {
      return false;
    }
    final GitHubRepositoryId that = (GitHubRepositoryId) other;
    return Objects.equals(this.owner.login, that.user())
      && Objects.equals(this.name, that.repo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.owner.login, this.name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("owner", this.owner.login)
      .add("name", this.name)
      .toString();
  }
}
