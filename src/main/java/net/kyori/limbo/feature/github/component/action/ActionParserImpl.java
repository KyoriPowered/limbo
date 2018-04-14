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
package net.kyori.limbo.feature.github.component.action;

import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.component.action.type.FindAction;
import net.kyori.limbo.feature.github.component.action.type.MatchAction;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.stream.NodeStream;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

public final class ActionParserImpl implements ActionParser {
  private final Map<String, Action.Parser<?>> parsers = new HashMap<>();

  @Inject
  private ActionParserImpl(@Named("identity") final User identity) {
    this.parsers.put("find", new FindAction.Parser(identity));
    this.parsers.put("match", new MatchAction.Parser(identity));
  }

  @Override
  public List<Action> parseAll(final Path featureRoot, final NodeStream nodes) {
    return nodes.map(Exceptions.rethrowFunction(node -> this.parseOne(featureRoot, node)))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Action> parseOne(final Path featureRoot, final Node node) throws IOException, XMLException {
    final Action.@Nullable Parser<?> parser = this.parsers.get(node.name());
    if(parser != null) {
      return Optional.ofNullable(parser.parse(featureRoot, node));
    }
    return Optional.empty();
  }
}
