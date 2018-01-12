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
package net.kyori.limbo.feature.github.feature.move;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.kyori.blizzard.Nullable;
import net.kyori.igloo.v3.RepositoryId;
import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.component.ActionPackage;
import net.kyori.limbo.util.Configurations;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

final class IssueMoveConfiguration {
  private final Pattern pattern;
  final ActionPackage source;
  final ActionPackage target;
  private final Table<RepositoryId, String, RepositoryId> map;

  @Inject
  IssueMoveConfiguration(@Named("identity") final User identity, @Named("github_feature") final Path path) throws IOException {
    final ConfigurationNode config = Configurations.readJson(path.resolve("issue_move.json"));
    this.pattern = Pattern.compile(String.format(config.getNode("pattern").getString(), identity.login));
    this.source = ActionPackage.parse(path.resolve("message"), config.getNode("actions", "source"));
    this.target = ActionPackage.parse(path.resolve("message"), config.getNode("actions", "target"));
    this.map = this.readMap(config);
  }

  Matcher matcher(final String string) {
    return this.pattern.matcher(string);
  }

  @Nullable
  RepositoryId target(final RepositoryId source, final String tag) {
    return this.map.get(source, tag);
  }

  private Table<RepositoryId, String, RepositoryId> readMap(final ConfigurationNode config) {
    final Table<RepositoryId, String, RepositoryId> map = HashBasedTable.create();
    final Map<String, Entry> definitions = new HashMap<>();
    for(final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode("definitions").getChildrenMap().entrySet()) {
      definitions.put(String.valueOf(entry.getKey()), new Entry(
        RepositoryId.of(
          entry.getValue().getNode("target", "user").getString(),
          entry.getValue().getNode("target", "repo").getString()
        ),
        new HashSet<>(entry.getValue().getNode("tags").getList(Types::asString))
      ));
    }
    for(final Map.Entry<Object, ? extends ConfigurationNode> entry : config.getNode("map").getChildrenMap().entrySet()) {
      final Entry source = definitions.get(String.valueOf(entry.getKey()));
      if(source == null) {
        throw new IllegalArgumentException("no definition for source '" + entry.getKey() + '\'');
      }
      for(final String target : entry.getValue().getList(Types::asString)) {
        final Entry definition = definitions.get(target);
        if(definition == null) {
          throw new IllegalArgumentException("no definition for target '" + entry.getKey() + '\'');
        }
        for(final String tag : definition.tags) {
          map.put(source.repository, tag, definition.repository);
        }
      }
    }
    return map;
  }

  private static class Entry {
    final RepositoryId repository;
    final Set<String> tags;

    Entry(final RepositoryId repository, final Set<String> tags) {
      this.repository = repository;
      this.tags = tags;
    }
  }
}
