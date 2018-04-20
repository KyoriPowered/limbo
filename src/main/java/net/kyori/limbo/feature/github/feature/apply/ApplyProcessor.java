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
package net.kyori.limbo.feature.github.feature.apply;

import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.processor.Processor;
import net.kyori.limbo.feature.github.feature.apply.entry.Entry;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class ApplyProcessor implements Processor {
  private final ApplyFeatureConfiguration configuration;
  private final Parser<Filter> filterParser;
  private final Parser<Entry> entryParser;

  @Inject
  private ApplyProcessor(final ApplyFeatureConfiguration configuration, final Parser<Filter> filterParser, final Parser<Entry> entryParser) {
    this.configuration = configuration;
    this.filterParser = filterParser;
    this.entryParser = entryParser;
  }

  @Override
  public void process(final Node node) {
    node
      .elements("github")
      .flatMap(Node::elements)
      .named("apply")
      .flatMap(Node::elements)
      .forEach(entry -> {
        final @Nullable Filter filter = this.filterParser.parse(entry.nodes("filter").flatMap(Node::nodes).one().want()).orElse(null);
        final List<Entry> entries = entry.elements("actions").flatMap(Node::elements).map(this.entryParser::parse).collect(Collectors.toList());
        this.configuration.entries.add(new ApplyFeatureConfiguration.Entry(filter, entries));
      });
  }
}
