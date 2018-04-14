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
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Table;
import net.kyori.igloo.v3.RepositoryId;
import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.component.ActionPackage;
import net.kyori.limbo.util.Documents;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.XMLException;
import net.kyori.xml.flattener.BranchLeafNodeFlattener;
import net.kyori.xml.node.Node;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

final class IssueMoveConfiguration {
  private final Pattern pattern;
  final ActionPackage source;
  final ActionPackage target;
  private final Table<RepositoryId, String, RepositoryId> map;

  @Inject
  IssueMoveConfiguration(@Named("identity") final User identity, @Named("github_feature") final Path path) throws IOException, JDOMException, XMLException {
    final Node config = Documents.read(path.resolve("issue_move.xml"));
    this.pattern = Pattern.compile(String.format(config.requireAttribute("pattern").value(), identity.login));
    this.source = ActionPackage.parse(path, config.elements("actions").flatMap(actions -> actions.elements("source")).collect(MoreCollectors.onlyElement()));
    this.target = ActionPackage.parse(path, config.elements("actions").flatMap(actions -> actions.elements("target")).collect(MoreCollectors.onlyElement()));
    this.map = this.readMap(config);
  }

  Matcher matcher(final String string) {
    return this.pattern.matcher(string);
  }

  @Nullable RepositoryId target(final RepositoryId source, final String tag) {
    return this.map.get(source, tag);
  }

  private Table<RepositoryId, String, RepositoryId> readMap(final Node node) {
    final Map<String, Entry> definitions = node.elements("definitions")
      .flatMap(n -> n.elements("definition"))
      .map(Exceptions.rethrowFunction(entry -> new AbstractMap.SimpleImmutableEntry<>(
        entry.requireAttribute("id").value(),
        new Entry(
          RepositoryId.of(
            entry.nodes("target").flatMap(target -> target.nodes("user")).collect(MoreCollectors.onlyElement()).value(),
            entry.nodes("target").flatMap(target -> target.nodes("repo")).collect(MoreCollectors.onlyElement()).value()
          ),
          entry.elements().flatMap(new BranchLeafNodeFlattener(Collections.singleton("tags"), Collections.singleton("tag"))).map(Node::value).collect(Collectors.toSet()))
      )))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    final Table<RepositoryId, String, RepositoryId> map = HashBasedTable.create();

    node.elements().flatMap(new BranchLeafNodeFlattener(Collections.singleton("targets"), Collections.singleton("source")))
      .forEach(Exceptions.rethrowConsumer(sourceNode -> {
        final Entry source = definitions.get(String.valueOf(sourceNode.requireAttribute("id").value()));
        if(source == null) {
          throw new IllegalArgumentException("no definition for source '" + sourceNode.requireAttribute("id").value() + '\'');
        }
        sourceNode.elements("target").map(Node::value).forEach(target -> {
          final Entry definition = definitions.get(target);
          if(definition == null) {
            throw new IllegalArgumentException("no definition for target '" + target + '\'');
          }
          for(final String tag : definition.tags) {
            map.put(source.repository, tag, definition.repository);
          }
        });
      }));
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
