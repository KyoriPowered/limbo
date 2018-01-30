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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FindAction extends PatternAction {
  private final ActionPackage apply;

  FindAction(final Set<On> on, final Who who, final Pattern pattern, final ActionPackage apply) {
    super(on, who, pattern);
    this.apply = apply;
  }

  @Override
  public void collect(final String string, final List<ActionPackage> applicators) {
    final Matcher matcher = this.pattern.matcher(string);
    if(!matcher.find() || this.escaped(matcher, string)) {
      return;
    }
    applicators.add(this.apply);
  }

  public static final class Parser extends PatternAction.Parser<FindAction> {
    public Parser(final User identity) {
      super(identity);
    }

    @Override
    public FindAction parse(final Path featureRoot, final ConfigurationNode config) throws IOException {
      final Pattern pattern = this.pattern(config);
      final ActionPackage apply = ActionPackage.parse(featureRoot, config.getNode("apply"));
      return new FindAction(this.on(config), this.who(config), pattern, apply);
    }
  }
}
