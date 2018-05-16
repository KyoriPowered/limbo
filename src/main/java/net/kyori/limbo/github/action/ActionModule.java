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
package net.kyori.limbo.github.action;

import net.kyori.fragment.feature.parser.FeatureParserBinder;
import net.kyori.fragment.processor.Processor;
import net.kyori.violet.DuplexModule;
import net.kyori.violet.SetBinder;
import net.kyori.xml.node.parser.ParserBinder;

public final class ActionModule extends DuplexModule {
  @Override
  protected void configure() {
    final ParserBinder parsers = new ParserBinder(this.publicBinder());
    parsers.bindParser(Action.class).to(ActionParser.class);

    final FeatureParserBinder featureParsers = new FeatureParserBinder(this.publicBinder());
    featureParsers.bindFeatureParser(Action.class);

    final SetBinder<Processor> processors = new SetBinder<>(this.publicBinder(), Processor.class);
    processors.addBinding().to(ActionProcessor.class);
  }
}