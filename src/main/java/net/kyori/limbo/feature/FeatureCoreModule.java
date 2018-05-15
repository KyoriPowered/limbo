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
package net.kyori.limbo.feature;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import net.kyori.fragment.feature.context.FeatureContext;
import net.kyori.fragment.feature.context.FeatureContextImpl;
import net.kyori.fragment.filter.FilterModule;
import net.kyori.fragment.processor.Processor;
import net.kyori.limbo.feature.core.FiltersProcessor;
import net.kyori.limbo.util.DynamicProvider;
import net.kyori.membrane.facet.FacetBinder;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.XMLException;
import net.kyori.xml.document.factory.DocumentFactory;
import net.kyori.xml.node.Node;

import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

public final class FeatureCoreModule extends AbstractModule {
  @Override
  protected void configure() {
    this.install(new DynamicProvider.Module<>(FeatureContext.class, new FeatureContextImpl()));

    final FacetBinder facets = new FacetBinder(this.binder());
    facets.addBinding().to(FeatureProcessor.class);

    this.install(new FilterModule());
    final Multibinder<Processor> processors = this.inSet(Key.get(Processor.class));
    processors.addBinding().to(FiltersProcessor.class);
  }

  @Named("env")
  @Provides
  @Singleton
  Node environmentConfiguration(final DocumentFactory factory, final @Named("config") Path path) throws XMLException {
    return Node.of(factory.read(path.resolve("environment.xml")).getRootElement());
  }
}
