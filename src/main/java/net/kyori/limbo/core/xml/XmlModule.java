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
package net.kyori.limbo.core.xml;

import com.google.inject.Provides;
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.document.factory.DocumentFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;

import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

public final class XmlModule extends AbstractModule {
  @Provides
  @Singleton
  DocumentFactory documentFactory(final @Named("config") Path path) {
    return DocumentFactory.builder()
      .builder(EvenMoreObjects.make(new SAXBuilder(), builder -> builder.setJDOMFactory(new LocatedJDOMFactory())))
      .includePaths(path.resolve("include"))
      .build();
  }
}
