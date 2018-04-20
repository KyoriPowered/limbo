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
package net.kyori.limbo.feature.github.repository.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.kyori.igloo.v3.Collaborator;
import net.kyori.igloo.v3.Repositories;
import net.kyori.igloo.v3.RepositoryId;
import net.kyori.igloo.v3.Users;
import net.kyori.limbo.feature.github.api.model.User;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RepositoryPermissionCacheImpl implements RepositoryPermissionCache {
  private final LoadingCache<Key, Collaborator.Permission> cache;

  @Inject
  public RepositoryPermissionCacheImpl(final Repositories repositories, final Users users) {
    this.cache = Caffeine.newBuilder()
      .expireAfterWrite(5, TimeUnit.HOURS)
      .build(key -> repositories.get(key.repository).collaborators().get(users.get(key.user.login)).permission());
  }

  @Override
  public Collaborator.@NonNull Permission get(final @NonNull RepositoryId repository, final @NonNull User user) {
    return this.cache.get(new Key(repository, user));
  }

  private static final class Key {
    private final RepositoryId repository;
    private final User user;

    private Key(final RepositoryId repository, final User user) {
      this.repository = repository;
      this.user = user;
    }

    @Override
    public boolean equals(final Object other) {
      if(this == other) {
        return true;
      }
      if(other == null || this.getClass() != other.getClass()) {
        return false;
      }
      final Key that = (Key) other;
      return Objects.equals(this.repository, that.repository)
        && Objects.equals(this.user, that.user);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.repository, this.user);
    }
  }
}
