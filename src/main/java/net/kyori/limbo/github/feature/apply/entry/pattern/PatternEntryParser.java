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
package net.kyori.limbo.github.feature.apply.entry.pattern;

import com.google.common.collect.MoreCollectors;
import net.kyori.fragment.filter.Filter;
import net.kyori.lambda.function.ThrowingFunction;
import net.kyori.limbo.github.action.Action;
import net.kyori.limbo.github.api.model.User;
import net.kyori.limbo.github.feature.apply.SearchScope;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.EnumParser;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

public final class PatternEntryParser implements Parser<PatternEntry> {
  private final Parser<Filter> filterParser;
  private final EnumParser<Type> typeParser;
  private final EnumParser<SearchScope> scopeParser;
  private final Parser<Action> actionParser;
  private final User identity;

  @Inject
  private PatternEntryParser(final Parser<Filter> filterParser, final EnumParser<Type> typeParser, final EnumParser<SearchScope> scopeParser, final Parser<Action> actionParser, final @Named("identity") User identity) {
    this.filterParser = filterParser;
    this.typeParser = typeParser;
    this.scopeParser = scopeParser;
    this.actionParser = actionParser;
    this.identity = identity;
  }

  @Override
  public @NonNull PatternEntry throwingParse(final @NonNull Node node) throws XMLException {
    final Type type = this.typeParser.parse(node.nodes("type").one().required());
    final Filter filter = this.filterParser.parse(node.nodes("filter").flatMap(Node::nodes).one().required());
    final SearchScope scope = this.scopeParser.parse(node.nodes("scope").one().optional()).orElse(SearchScope.DESCRIPTION);
    final Pattern pattern = this.pattern(node);
    if(type == Type.FIND) {
      final Action action = this.actionParser.parse(node.elements("action").collect(MoreCollectors.onlyElement()));
      return new FindPatternEntry(filter, scope, pattern, action);
    } else if(type == Type.WHERE) {
      final List<WherePatternEntry.Where> where = node.elements("where")
        .map(ThrowingFunction.of(group -> {
          final int id = Integer.parseInt(group.requireAttribute("group").value());
          final Map<String, Action> actions = new HashMap<>();
          group.elements("match")
            .forEach(match -> {
              final Action action = this.actionParser.parse(match.nodes("apply").one().required());
              match.nodes("value").forEach(value -> actions.put(value.value(), action));
            });
          return new WherePatternEntry.Where(id, actions);
        }))
        .collect(Collectors.toList());
      return new WherePatternEntry(filter, scope, pattern, where);
    } else {
      throw new IllegalArgumentException(type.name());
    }
  }

  private Pattern pattern(final Node node) throws XMLException {
    return Pattern.compile(String.format(node.requireAttribute("pattern").value(), this.identity.login));
  }

  private enum Type {
    FIND,
    WHERE;
  }
}
