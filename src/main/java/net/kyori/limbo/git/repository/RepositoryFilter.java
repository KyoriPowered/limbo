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
package net.kyori.limbo.git.repository;

import com.google.common.base.MoreObjects;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RepositoryFilter implements TypedFilter<AbstractRepositoryQuery> {
  private final RepositoryId repository;

  public RepositoryFilter(final RepositoryId repository) {
    this.repository = repository;
  }

  @Override
  public boolean queryable(final @NonNull FilterQuery query) {
    return query instanceof AbstractRepositoryQuery;
  }

  @Override
  public @NonNull FilterResponse typedQuery(final @NonNull AbstractRepositoryQuery query) {
    if(query instanceof RepositoryQuery) {
      return FilterResponse.from(this.repository.equals(((RepositoryQuery) query).repository()));
    } else if(query instanceof RepositoryTagQuery) {
      return FilterResponse.from(this.repository.tags().contains(((RepositoryTagQuery) query).repositoryTag()));
    }
    throw new IllegalArgumentException(query.getClass().getName());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .addValue(this.repository)
      .toString();
  }
}
