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
package net.kyori.limbo.git.repository;

import net.kyori.feature.parser.AbstractInjectedFeatureDefinitionParser;
import net.kyori.feature.reference.ReferenceFinder;
import net.kyori.limbo.github.repository.GitHubRepositoryIdImpl;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.EnumParser;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RepositoryIdParser extends AbstractInjectedFeatureDefinitionParser<RepositoryId> implements Parser<RepositoryId> {
  private static final ReferenceFinder REFERENCE_FINDER = ReferenceFinder.finder().refs("repository");
  private final EnumParser<RepositoryId.Source> source;

  @Inject
  private RepositoryIdParser(final EnumParser<RepositoryId.Source> source) {
    this.source = source;
  }

  @Override
  public @NonNull RepositoryId realThrowingParse(final @NonNull Node node) throws XMLException {
    final RepositoryId.Source source = this.source.parse(node.attribute("source").optional()).orElse(RepositoryId.Source.GITHUB);
    final String user = node.requireAttribute("user").value();
    final String repo = node.requireAttribute("repo").value();
    final Set<String> tags = node.nodes("tag").map(Node::value).collect(Collectors.toSet());
    switch(source) {
      case GITHUB: return new GitHubRepositoryIdImpl(user, repo, tags);
      default: throw new IllegalArgumentException(source.name());
    }
  }

  @Override
  protected @NonNull ReferenceFinder referenceFinder() {
    return REFERENCE_FINDER;
  }
}
