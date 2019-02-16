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
package net.kyori.limbo.discord.feature.role.auto;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.fragment.filter.Filter;
import net.kyori.lambda.function.ThrowingConsumer;
import net.kyori.limbo.xml.Processor;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.flattener.BranchLeafNodeFlattener;
import net.kyori.xml.node.parser.Parser;

import javax.inject.Inject;

public final class AutoRoleProcessor implements Processor {
  private final Configuration configuration;
  private final Parser<Filter> filterParser;
  private final Parser<Long> longParser;

  @Inject
  private AutoRoleProcessor(final Configuration configuration, final Parser<Filter> filterParser, final Parser<Long> longParser) {
    this.configuration = configuration;
    this.filterParser = filterParser;
    this.longParser = longParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("discord")
      .flatMap(Node::elements)
      .flatMap(new BranchLeafNodeFlattener("auto-roles", "auto-role"))
      .forEach(ThrowingConsumer.of(auto -> {
        final /* @Nullable */ Filter filter = this.filterParser.parse(auto.nodes("filter").flatMap(Node::nodes).one().optional()).orElse(null);
        final LongSet roles = new LongArraySet();
        auto.nodes("role").map(this.longParser).forEach(roles::add);
        this.configuration.entries.add(new Configuration.Entry(filter, roles));
      }));
  }
}
