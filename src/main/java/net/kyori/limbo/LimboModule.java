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
package net.kyori.limbo;

import com.google.inject.Module;
import com.google.inject.Provides;
import net.kyori.limbo.discord.DiscordModule;
import net.kyori.limbo.event.EventModule;
import net.kyori.limbo.git.GitModule;
import net.kyori.limbo.github.GitHubModule;
import net.kyori.limbo.http.HttpModule;
import net.kyori.limbo.scheduler.SchedulerModule;
import net.kyori.limbo.xml.XmlModule;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.DuplexBinder;
import net.kyori.xml.XMLException;
import net.kyori.xml.document.factory.DocumentFactory;
import net.kyori.xml.node.Node;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Named;
import javax.inject.Singleton;

public final class LimboModule extends AbstractModule {
  @Override
  protected void configure() {
    this.install(new EventModule());
    this.install(new GitModule());
    this.install(new HttpModule());
    this.install(new SchedulerModule());
    this.install(new XmlModule());

    this.installDuplex(new DiscordModule());
    this.installDuplex(new GitHubModule());
  }

  private void installDuplex(final Module module) {
    final DuplexBinder binder = DuplexBinder.create(this.binder());
    binder.install(module);
  }

  @Named("root")
  @Provides
  @Singleton
  Path root() {
    return Paths.get(".");
  }

  @Named("config")
  @Provides
  @Singleton
  Path config(final @Named("root") Path path) {
    return path.resolve("config");
  }

  @Named("env")
  @Provides
  @Singleton
  Node environmentConfiguration(final DocumentFactory factory, final @Named("config") Path path) throws XMLException {
    return Node.of(factory.read(path.resolve("environment.xml")).getRootElement());
  }
}
