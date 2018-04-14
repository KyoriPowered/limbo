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
package net.kyori.limbo.core.web;

import net.kyori.limbo.util.Documents;
import net.kyori.limbo.util.HttpResponse;
import net.kyori.membrane.facet.Enableable;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.jdom2.JDOMException;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public final class WebFacet implements Enableable {
  private final String host;
  private final int port;

  @Inject
  private WebFacet(@Named("config") final Path path) throws IOException, JDOMException, XMLException {
    final Node config = Documents.read(path.resolve("web.xml"));
    this.host = config.requireAttribute("host").value();
    this.port = Integer.parseInt(config.requireAttribute("port").value());
  }

  @Override
  public void enable() {
    Spark.ipAddress(this.host);
    Spark.port(this.port);
    Spark.get("/", (request, response) -> HttpResponse.noContent(response));
    Spark.notFound((request, response) -> HttpResponse.fourOhFour(response));
  }

  @Override
  public void disable() {
    Spark.stop();
  }
}
