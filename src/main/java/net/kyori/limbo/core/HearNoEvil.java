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
package net.kyori.limbo.core;

import javax.inject.Inject;
import net.kyori.event.method.annotation.PostOrder;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.limbo.event.Listener;
import net.kyori.limbo.event.ShutdownEvent;
import net.kyori.membrane.facet.internal.Facets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearNoEvil implements Listener {
  private static final Logger LOGGER = LoggerFactory.getLogger(HearNoEvil.class);
  private final Facets facets;

  @Inject
  private HearNoEvil(final Facets facets) {
    this.facets = facets;
  }

  @Subscribe
  @PostOrder(Integer.MAX_VALUE)
  public void shutdown(final ShutdownEvent event) {
    LOGGER.info("Shutdown requested by {}", event.source());
    this.facets.disable();
    System.exit(1);
  }
}
