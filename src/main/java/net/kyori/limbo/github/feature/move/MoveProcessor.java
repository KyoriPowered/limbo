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
package net.kyori.limbo.github.feature.move;

import net.kyori.feature.parser.FeatureDefinitionParser;
import net.kyori.lambda.function.ThrowingConsumer;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.github.action.Action;
import net.kyori.limbo.github.api.model.User;
import net.kyori.limbo.xml.Processor;
import net.kyori.xml.element.Elements;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.flattener.BranchLeafNodeFlattener;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

public final class MoveProcessor implements Processor {
  private final User identity;
  private final MoveConfiguration configuration;
  private final FeatureDefinitionParser<RepositoryId> repoParser;
  private final FeatureDefinitionParser<Action> actionParser;

  @Inject
  private MoveProcessor(final @Named("identity") User identity, final MoveConfiguration configuration, final FeatureDefinitionParser<RepositoryId> repoParser, final FeatureDefinitionParser<Action> actionParser) {
    this.identity = identity;
    this.configuration = configuration;
    this.repoParser = repoParser;
    this.actionParser = actionParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("github")
      .flatMap(Node::elements)
      .named("move")
      .flatMap(Node::elements)
      .map(Elements::inherited)
      .forEach(ThrowingConsumer.of(entry -> {
        final Pattern pattern = Pattern.compile(String.format(entry.requireAttribute("pattern").value(), this.identity.login));

        final @Nullable RepositoryId sourceRepository = entry.elements("source").flatMap(source -> source.nodes("repository")).one().map(this.repoParser::parse).required();
        final Action sourceAction = entry.elements("source").flatMap(source -> source.nodes("action")).one().map(this.actionParser::parse).required();

        final List<MoveConfiguration.Target> targets = entry.elements()
          .flatMap(new BranchLeafNodeFlattener("targets", "target"))
          .map(target -> {
            final @Nullable RepositoryId repository = target.nodes("repository").one().map(this.repoParser::parse).required();
            final Action action = target.nodes("action").one().map(this.actionParser::parse).required();
            return new MoveConfiguration.Target(repository, action);
          })
          .collect(Collectors.toList());

        this.configuration.entries.add(new MoveConfiguration.Entry(pattern, sourceRepository, sourceAction, targets));
      }));
  }
}
