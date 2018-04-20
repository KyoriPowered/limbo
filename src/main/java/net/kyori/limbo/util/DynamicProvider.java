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
package net.kyori.limbo.util;

import com.google.inject.TypeLiteral;
import net.kyori.lunar.CheckedAutoCloseable;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;

import javax.inject.Provider;

import static java.util.Objects.requireNonNull;

public class DynamicProvider<T> implements Provider<T> {
  private T value;

  @Override
  public T get() {
    return requireNonNull(this.value, "value");
  }

  public CheckedAutoCloseable set(final T newValue) {
    final T oldValue = this.value;
    this.value = newValue;
    return () -> this.value = oldValue;
  }

  public static class Module<T> extends AbstractModule {
    private final Class<T> type;

    public Module(final Class<T> type) {
      this.type = type;
    }

    @Override
    protected void configure() {
      final TypeLiteral<DynamicProvider<T>> type = new FriendlyTypeLiteral<DynamicProvider<T>>() {}.where(new TypeArgument<T>(this.type) {});
      this.bind(this.type).toProvider(type);
      this.bind(type).toInstance(new DynamicProvider<>());
    }
  }
}
