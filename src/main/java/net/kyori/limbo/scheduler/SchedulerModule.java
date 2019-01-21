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
package net.kyori.limbo.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;

public final class SchedulerModule extends AbstractModule {
  @Provides
  @Singleton
  ExecutorService executorService() {
    final ThreadFactory factory = new ThreadFactoryBuilder()
      .setNameFormat("Limbo Executor - %d")
      .build();
    return Executors.newScheduledThreadPool(processors(), factory);
  }

  @Provides
  @Singleton
  ScheduledExecutorService scheduledExecutorService() {
    final ThreadFactory factory = new ThreadFactoryBuilder()
      .setNameFormat("Limbo Scheduler - %d")
      .build();
    return Executors.newScheduledThreadPool(processors(), factory);
  }

  private static int processors() {
    return Runtime.getRuntime().availableProcessors();
  }
}
