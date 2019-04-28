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
package net.kyori.limbo.event;

import net.kyori.event.EventBus;
import net.kyori.event.method.MethodSubscriptionAdapter;
import net.kyori.event.method.SimpleMethodSubscriptionAdapter;
import net.kyori.event.method.asm.ASMEventExecutorFactory;
import net.kyori.membrane.facet.Enableable;
import net.kyori.membrane.facet.internal.Facets;

import javax.inject.Inject;

/* package */ final class EventInstaller implements Enableable {
  private final MethodSubscriptionAdapter<Object> bus;
  private final Facets facets;

  @Inject
  private EventInstaller(final EventBus<Object> bus, final Facets facets) {
    this.bus = new SimpleMethodSubscriptionAdapter<>(bus, new ASMEventExecutorFactory<>());
    this.facets = facets;
  }

  @Override
  public void enable() {
    this.facets.of(Listener.class).forEach(this.bus::register);
  }

  @Override
  public void disable() {
    this.facets.of(Listener.class).forEach(this.bus::unregister);
  }
}
