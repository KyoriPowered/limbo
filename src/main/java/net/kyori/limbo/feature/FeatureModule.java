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
package net.kyori.limbo.feature;

import com.google.inject.Module;
import net.kyori.limbo.feature.discord.DiscordModule;
import net.kyori.limbo.feature.git.GitModule;
import net.kyori.limbo.feature.github.GitHubModule;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.DuplexBinder;

public final class FeatureModule extends AbstractModule {
  @Override
  protected void configure() {
    this.install(new FeatureCoreModule());

    this.installFeature(new DiscordModule());
    this.installFeature(new GitModule());
    this.installFeature(new GitHubModule());
  }

  private void installFeature(final Module module) {
    final DuplexBinder binder = DuplexBinder.create(this.binder());
    binder.install(module);
  }
}
