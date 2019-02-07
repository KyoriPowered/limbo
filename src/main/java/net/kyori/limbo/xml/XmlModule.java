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
package net.kyori.limbo.xml;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import net.kyori.feature.FeatureDefinitionContext;
import net.kyori.feature.FeatureDefinitionContextImpl;
import net.kyori.fragment.filter.FilterModule;
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.membrane.facet.FacetBinder;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.document.factory.DocumentFactory;
import net.kyori.xml.node.parser.ParserModule;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;

import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

public final class XmlModule extends AbstractModule {
  @Override
  protected void configure() {
    this.bind(FeatureDefinitionContext.class).to(FeatureDefinitionContextImpl.class).in(Scopes.SINGLETON);

    this.install(new ParserModule());

    this.install(new FilterModule());

    final FacetBinder facets = new FacetBinder(this.binder());
    facets.addBinding().to(FeatureProcessor.class);

    final Multibinder<Processor> processors = this.inSet(Key.get(Processor.class));
    processors.addBinding().to(FiltersProcessor.class);
  }

  @Provides
  @Singleton
  DocumentFactory documentFactory(final @Named("config") Path path) {
    return DocumentFactory.builder()
      .builder(EvenMoreObjects.make(new SAXBuilder(), builder -> builder.setJDOMFactory(new LocatedJDOMFactory())))
      .includePaths(path.resolve("include"))
      .build();
  }
}
