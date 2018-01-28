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
import net.kyori.limbo.feature.github.component.action.Action;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Set;
import java.util.regex.Pattern;

abstract class PatternAction extends Action.Impl {
  final Pattern pattern;

  PatternAction(final Set<On> on, final Who who, final Pattern pattern) {
    super(on, who);
    this.pattern = pattern;
  }

  static abstract class Parser<A extends Action> extends Action.Parser.Impl<A> {
    private final User identity;

    Parser(final User identity) {
      this.identity = identity;
    }

    Pattern pattern(final ConfigurationNode config) {
      return Pattern.compile(String.format(config.getNode("pattern").getString(), this.identity.login));
    }
  }
}
