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
package net.kyori.limbo.discord.feature.role.react;

import net.kyori.fragment.filter.Filter;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.limbo.xml.Processor;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.flattener.BranchLeafNodeFlattener;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

public final class RoleReactProcessor implements Processor {
  private final Configuration configuration;
  private final Parser<Filter> filterParser;
  private final Parser<Long> longParser;

  @Inject
  private RoleReactProcessor(final Configuration configuration, final Parser<Filter> filterParser, final Parser<Long> longParser) {
    this.configuration = configuration;
    this.filterParser = filterParser;
    this.longParser = longParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("discord")
      .flatMap(Node::elements)
      .flatMap(new BranchLeafNodeFlattener("role-reacts", "role-react"))
      .forEach(Exceptions.rethrowConsumer(ping -> {
        final @Snowflake long message = ping.nodes("message").one().map(this.longParser).required();
        final String emoji = ping.nodes("emoji").one().map(Node::value).required();
        final @Nullable Filter filter = this.filterParser.parse(ping.nodes("filter").flatMap(Node::nodes).one().optional()).orElse(null);
        final @Snowflake long role = ping.nodes("role").one().map(this.longParser).required();
        this.configuration.reactions.add(new Configuration.Reaction(message, emoji, filter, role));
      }));
  }
}
