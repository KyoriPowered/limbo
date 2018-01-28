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
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MatchAction extends PatternAction {
  private final List<Where> where;

  MatchAction(final Set<On> on, final Who who, final Pattern pattern, final List<Where> where) {
    super(on, who, pattern);
    this.where = where;
  }

  @Override
  public void collect(final String string, final List<ActionPackage> applicators) {
    final Matcher matcher = this.pattern.matcher(string);
    if(!matcher.find()) {
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
    public MatchAction parse(final Path featureRoot, final ConfigurationNode config) throws IOException {
      final Pattern pattern = this.pattern(config);
      final List<MatchAction.Where> where = new ArrayList<>();
      for(final Map.Entry<Object, ? extends ConfigurationNode> group : config.getNode("where").getChildrenMap().entrySet()) {
        final int id = Integer.parseInt(String.valueOf(group.getKey()));
        final Map<String, ActionPackage> applicators = new HashMap<>();
        for(final Map.Entry<Object, ? extends ConfigurationNode> entry : group.getValue().getChildrenMap().entrySet()) {
          applicators.put((String) entry.getKey(), ActionPackage.parse(featureRoot, entry.getValue()));
        }
        where.add(new MatchAction.Where(id, applicators));
      }
      return new MatchAction(this.on(config), this.who(config), pattern, where);
    }
  }
}
