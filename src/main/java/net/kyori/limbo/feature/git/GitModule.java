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
package net.kyori.limbo.feature.git;

import net.kyori.fragment.feature.parser.FeatureParserBinder;
import net.kyori.fragment.filter.FilterBinder;
import net.kyori.fragment.processor.Processor;
import net.kyori.limbo.feature.git.actor.ActorTypeFilterParser;
import net.kyori.limbo.feature.git.event.EventFilterParser;
import net.kyori.limbo.feature.git.label.LabelFilterParser;
import net.kyori.limbo.feature.git.repository.RepositoriesProcessor;
import net.kyori.limbo.feature.git.repository.RepositoryFilterParser;
import net.kyori.limbo.feature.git.repository.RepositoryId;
import net.kyori.limbo.feature.git.repository.RepositoryIdParser;
import net.kyori.violet.DuplexModule;
import net.kyori.violet.SetBinder;
import net.kyori.xml.node.parser.ParserBinder;

public final class GitModule extends DuplexModule {
  @Override
  protected void configure() {
    final FilterBinder filters = new FilterBinder(this.publicBinder());
    filters.bindFilter("actor").to(ActorTypeFilterParser.class);
    filters.bindFilter("event").to(EventFilterParser.class);
    filters.bindFilter("label").to(LabelFilterParser.class);
    filters.bindFilter("repository").to(RepositoryFilterParser.class);

    final ParserBinder parsers = new ParserBinder(this.publicBinder());
    parsers.bindParser(RepositoryId.class).to(RepositoryIdParser.class);

    final FeatureParserBinder featureParsers = new FeatureParserBinder(this.publicBinder());
    featureParsers.bindFeatureParser(RepositoryId.class);

    final SetBinder<Processor> processors = new SetBinder<>(this.publicBinder(), Processor.class);
    processors.addBinding().to(RepositoriesProcessor.class);
  }
}
