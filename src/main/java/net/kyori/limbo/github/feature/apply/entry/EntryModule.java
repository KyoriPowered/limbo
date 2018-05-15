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
package net.kyori.limbo.github.feature.apply.entry;

import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import net.kyori.limbo.github.feature.apply.entry.pattern.PatternEntryParser;
import net.kyori.violet.DuplexModule;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.parser.ParserBinder;

public final class EntryModule extends DuplexModule {
  @Override
  protected void configure() {
    final ParserBinder parsers = new ParserBinder(this.publicBinder());
    parsers.bindParser(Entry.class).to(EntryParser.class);

    this.bindEntry("pattern").to(PatternEntryParser.class);
  }

  private LinkedBindingBuilder<Parser<? extends Entry>> bindEntry(final String id) {
    return MapBinder.newMapBinder(this.publicBinder(), TypeLiteral.get(String.class), new TypeLiteral<Parser<? extends Entry>>() {}).addBinding(id);
  }
}
