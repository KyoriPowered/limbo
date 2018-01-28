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
package net.kyori.limbo.feature.github.feature.analyze;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.limbo.feature.github.api.model.Repository;
import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.component.ActionPackage;
import net.kyori.limbo.feature.github.component.action.Action;
import net.kyori.limbo.feature.github.component.action.ActionParser;
import net.kyori.limbo.util.Configurations;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;

final class AnalyzeFeatureConfiguration {
  private final Multimap<Repository, Action> entries = ArrayListMultimap.create();

  @Inject
  AnalyzeFeatureConfiguration(@Named("github_feature") final Path path, final ActionParser parser) throws IOException {
    final ConfigurationNode config = Configurations.readJson(path.resolve("analyze.json"));

    for(final ConfigurationNode entry : config.getChildrenList()) {
      final List<Repository> repositories = new ArrayList<>();
      for(final ConfigurationNode repository : entry.getNode("targets").getChildrenList()) {
        repositories.add(new Repository(new User(repository.getNode("user").getString()), repository.getNode("repo").getString()));
      }
      final List<Action> actions = parser.parseAll(path, entry.getNode("actions"));
      for(final Repository repository : repositories) {
        this.entries.putAll(repository, actions);
      }
    }
  }

  List<ActionPackage> applicators(final Action.On on, final Repository repository, final Function<Action.Who, Boolean> allowed, final String string) {
    final List<ActionPackage> applicators = new ArrayList<>();
    for(final Action action : this.entries.get(repository)) {
      if(!action.on(on) || !allowed.apply(action.who())) {
        continue;
      }
      action.collect(string, applicators);
    }
    return applicators;
  }
}
