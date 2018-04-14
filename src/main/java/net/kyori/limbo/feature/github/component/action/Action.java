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

import net.kyori.limbo.feature.github.component.ActionPackage;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public interface Action {
  boolean on(final On on);

  Set<Who> who();

  void collect(final String string, final List<ActionPackage> applicators);

  interface Parser<A extends Action> {
    A parse(final Path featureRoot, final Node node) throws IOException, XMLException;

    abstract class Impl<A extends Action> implements Parser<A> {
      protected Set<On> on(final Node node) {
        return node.nodes("on")
          .map(on -> on.value().replace(' ', '_').toUpperCase(Locale.ENGLISH))
          .map(On::valueOf)
          .collect(Collectors.toSet());
      }

      protected Set<Who> by(final Node node) {
        return node.nodes("by")
          .map(by -> by.value().replace(' ', '_').toUpperCase(Locale.ENGLISH))
          .map(Who::valueOf)
          .collect(Collectors.toSet());
      }
    }
  }

  abstract class Impl implements Action {
    protected final Set<On> on;
    protected final Set<Who> by;

    public Impl(final Set<On> on, final Set<Who> by) {
      this.on = on;
      this.by = by;
    }

    @Override
    public boolean on(final On on) {
      return this.on.contains(on);
    }

    @Override
    public Set<Who> who() {
      return this.by;
    }
  }

  enum On {
    ISSUE_OPEN,
    ISSUE_COMMENT,
    PULL_REQUEST_OPEN,
    PULL_REQUEST_COMMENT;
  }

  enum Who {
    ANY,
    AUTHOR,
    COLLABORATOR;
  }
}
