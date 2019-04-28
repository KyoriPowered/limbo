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
package net.kyori.limbo.github.action;

import com.google.common.base.Joiner;
import net.kyori.feature.parser.AbstractInjectedFeatureDefinitionParser;
import net.kyori.limbo.xml.Xml;
import net.kyori.mu.Optionals;
import net.kyori.mu.function.ThrowingFunction;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public final class ActionParser extends AbstractInjectedFeatureDefinitionParser<Action> implements Parser<Action> {
  private static final Joiner JOINER = Joiner.on('\n');
  private final Path root;

  @Inject
  private ActionParser(final @Named("config") Path root) {
    this.root = root;
  }

  @Override
  public @NonNull Action realThrowingParse(final @NonNull Node node) {
    final Action./* @Nullable */ State state = pickOne(
      Xml.attrBoolean(node, "open", false),
      Action.State.OPEN,
      Xml.attrBoolean(node, "close", false),
      Action.State.CLOSE
    );
    final Action./* @Nullable */ Comment comment = node.elements("comment")
      .one()
      .map(this::parseComment)
      .optional(null);
    final Set<String> addLabels = node.elements("label")
      .flatMap(label -> label.elements("add"))
      .map(Node::value)
      .collect(Collectors.toSet());
    final Set<String> removeLabels = node.elements("label")
      .flatMap(label -> label.elements("remove"))
      .map(Node::value)
      .collect(Collectors.toSet());
    final Action./* @Nullable */ Lock lock = pickOne(
      Xml.attrBoolean(node, "lock", false),
      Action.Lock.LOCK,
      Xml.attrBoolean(node, "unlock", false),
      Action.Lock.UNLOCK
    );
    return new ActionImpl(state, comment, addLabels, removeLabels, lock);
  }

  private Action.Comment parseComment(final Node node) {
    final Map<String, String> tokens = node.nodes("tokens")
      .flatMap(Node::nodes)
      .named("token")
      .stream()
      .map(ThrowingFunction.of(token -> new AbstractMap.SimpleImmutableEntry<>(token.requireAttribute("name").value(), token.requireAttribute("value").value())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    final /* @Nullable */ String comment = Optionals.first(
      node.nodes("src")
        .one()
        .map(ThrowingFunction.of(src -> readMessage(this.root.resolve("message").resolve(src.value()))))
        .optional(),
      node.nodes("value")
        .one()
        .map(Node::value)
        .optional(),
      tokens.isEmpty()
        ? Optional.of(node.value())
        : Optional.empty()
    ).orElse(null);
    return new Action.Comment(comment, tokens);
  }

  private static <E extends Enum<E>> @Nullable E pickOne(final boolean a, final E aResult, final boolean b, final E bResult) {
    if(!a && !b) {
      return null;
    } else if(a && b) {
      throw new IllegalArgumentException("cannot do both");
    } else if(a) {
      return aResult;
    } else if(b) {
      return bResult;
    } else {
      throw new IllegalArgumentException("what exactly do you want to do?");
    }
  }

  private static String readMessage(final Path path) throws IOException {
    return JOINER.join(Files.readAllLines(path));
  }
}
