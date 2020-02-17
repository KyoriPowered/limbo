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

import java.util.Set;
import net.kyori.feature.FeatureDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface RepositoryId extends FeatureDefinition {
  /**
   * Gets the repository source.
   *
   * @return the repository source
   */
  @NonNull Source source();

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  @NonNull String user();

  /**
   * Gets the repository name.
   *
   * @return the repository name
   */
  @NonNull String repo();

  /**
   * Gets a set of "tags" that may be used to identify this repository.
   *
   * @return a set of tags
   */
  @NonNull Set<String> tags();

  /**
   * Gets the repository id as a string.
   *
   * @return string
   */
  default @NonNull String asString() {
    return this.user() + '/' + this.repo();
  }

  enum Source {
    GITHUB;
  }
}
