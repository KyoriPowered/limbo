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

import javax.inject.Inject;
import javax.inject.Provider;
import net.kyori.feature.FeatureDefinitionContext;
import net.kyori.limbo.xml.Processor;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.flattener.BranchLeafNodeFlattener;
import net.kyori.xml.node.parser.Parser;

public final class ActionProcessor implements Processor {
  private final Provider<FeatureDefinitionContext> context;
  private final Parser<Action> actionParser;

  @Inject
  private ActionProcessor(final Provider<FeatureDefinitionContext> context, final Parser<Action> actionParser) {
    this.context = context;
    this.actionParser = actionParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("github")
      .flatMap(Node::elements)
      .flatMap(new BranchLeafNodeFlattener("actions", "action"))
      .forEach(entry -> this.context.get().define(Action.class, entry, this.actionParser.parse(entry)));
  }
}
