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
package net.kyori.limbo.discord.feature.gir;

import net.kyori.feature.parser.FeatureDefinitionParser;
import net.kyori.limbo.discord.action.Action;
import net.kyori.limbo.git.repository.RepositoryId;
import net.kyori.limbo.xml.Processor;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.node.Node;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class GitHubIssueRefProcessor implements Processor {
  private final Configuration configuration;
  private final FeatureDefinitionParser<RepositoryId> repoParser;
  private final FeatureDefinitionParser<Action> actionParser;

  @Inject
  private GitHubIssueRefProcessor(final Configuration configuration, final FeatureDefinitionParser<RepositoryId> repoParser, final FeatureDefinitionParser<Action> actionParser) {
    this.configuration = configuration;
    this.repoParser = repoParser;
    this.actionParser = actionParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("discord")
      .flatMap(Node::elements)
      .named("github-issue-ref")
      .one()
      .ifPresent(Exceptions.rethrowConsumer(entry -> {
        entry.nodes("search").one().ifPresent(search -> {
          final Pattern pattern = search.nodes("pattern").one().map(Node::value).map(Pattern::compile).required();
          final List<RepositoryId> repositories = search.nodes("repositories")
            .flatMap(Node::elements)
            .named("repository")
            .map(this.repoParser::parse)
            .collect(Collectors.toList());
          this.configuration.searches.add(new Configuration.Search(pattern, repositories));
        });
        this.configuration.openAction = this.actionParser.parse(entry.nodes("state").flatMap(Node::elements).named("open").one().required());
        this.configuration.mergedAction = this.actionParser.parse(entry.nodes("state").flatMap(Node::elements).named("merged").one().required());
        this.configuration.closedAction = this.actionParser.parse(entry.nodes("state").flatMap(Node::elements).named("closed").one().required());
      }));
  }
}
