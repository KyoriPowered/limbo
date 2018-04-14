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
package net.kyori.limbo.feature.github.component.action.type;

import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.component.ActionPackage;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MatchAction extends PatternAction {
  private final List<Where> where;

  MatchAction(final Set<On> on, final Set<Who> by, final Pattern pattern, final List<Where> where) {
    super(on, by, pattern);
    this.where = where;
  }

  @Override
  public void collect(final String string, final List<ActionPackage> applicators) {
    final Matcher matcher = this.pattern.matcher(string);
    if(!matcher.find() || this.escaped(matcher, string)) {
      return;
    }
    for(final Where where : this.where) {
      applicators.add(where.applicators.get(matcher.group(where.group)));
    }
    applicators.removeAll(Collections.singleton(null));
  }

  static class Where {
    final int group;
    final Map<String, ActionPackage> applicators;

    Where(final int group, final Map<String, ActionPackage> applicators) {
      this.group = group;
      this.applicators = applicators;
    }
  }

  public static class Parser extends PatternAction.Parser<MatchAction> {
    public Parser(final User identity) {
      super(identity);
    }

    @Override
    public MatchAction parse(final Path featureRoot, final Node node) throws XMLException {
      final Pattern pattern = this.pattern(node);
      final List<MatchAction.Where> where = node.elements("where")
        .map(Exceptions.rethrowFunction(group -> {
          final int id = Integer.parseInt(group.requireAttribute("group").value());
          final Map<String, ActionPackage> applicators = group.elements("match")
            .map(Exceptions.rethrowFunction(match -> new AbstractMap.SimpleImmutableEntry<>(match.requireAttribute("value").value(), ActionPackage.parse(featureRoot, match))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
          return new MatchAction.Where(id, applicators);
        }))
        .collect(Collectors.toList());
      return new MatchAction(this.on(node), this.by(node), pattern, where);
    }
  }
}
