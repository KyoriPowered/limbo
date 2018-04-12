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
import ninja.leaping.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

public final class ActionParserImpl implements ActionParser {
  private final Map<String, Action.Parser<?>> parsers = new HashMap<>();

  @Inject
  private ActionParserImpl(@Named("identity") final User identity) {
    this.parsers.put("apply when found", new FindAction.Parser(identity));
    this.parsers.put("apply where", new MatchAction.Parser(identity));
  }

  @Override
  public List<Action> parseAll(final Path featureRoot, final ConfigurationNode config) throws IOException {
    final List<Action> actions = new ArrayList<>();

    for(final ConfigurationNode entry : config.getChildrenList()) {
      this.parseOne(featureRoot, entry).ifPresent(actions::add);
    }

    return actions;
  }

  @Override
  public Optional<Action> parseOne(final Path featureRoot, final ConfigurationNode config) throws IOException {
    final Action.@Nullable Parser<?> parser = this.parsers.get(config.getNode("type").getString());
    if(parser != null) {
      return Optional.ofNullable(parser.parse(featureRoot, config));
    }
    return Optional.empty();
  }
}
