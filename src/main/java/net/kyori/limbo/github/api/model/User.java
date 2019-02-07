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
package net.kyori.limbo.github.api.model;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class User {
  public String login;

  public User() {
  }

  public User(final String login) {
    this.login = login;
  }

  @Override
  public boolean equals(final Object other) {
    if(this == other) {
      return true;
    }
    if(other == null || this.getClass() != other.getClass()) {
      return false;
    }
    final User that = (User) other;
    return Objects.equals(this.login, that.login);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.login);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("login", this.login)
      .toString();
  }
}
