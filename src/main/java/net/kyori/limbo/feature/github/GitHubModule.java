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
package net.kyori.limbo.feature.github;

import com.google.gson.Gson;
import com.google.inject.Provides;
import net.kyori.igloo.v3.GitHub;
import net.kyori.igloo.v3.Repositories;
import net.kyori.igloo.v3.Users;
import net.kyori.limbo.feature.github.api.model.User;
import net.kyori.limbo.feature.github.cache.RepositoryPermissionCache;
import net.kyori.limbo.feature.github.cache.RepositoryPermissionCacheImpl;
import net.kyori.limbo.feature.github.component.action.ActionParser;
import net.kyori.limbo.feature.github.component.action.ActionParserImpl;
import net.kyori.limbo.feature.github.feature.apply.ApplyFeature;
import net.kyori.limbo.feature.github.feature.move.MoveIssueFeature;
import net.kyori.limbo.util.Documents;
import net.kyori.membrane.facet.FacetBinder;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Named;
import javax.inject.Singleton;

public final class GitHubModule extends AbstractModule {
  @Override
  protected void configure() {
    this.bind(ActionParser.class).to(ActionParserImpl.class);
    this.bind(RepositoryPermissionCache.class).to(RepositoryPermissionCacheImpl.class);
    FacetBinder.create(this)
      .add(GitHubEndpoint.class)
      .add(ApplyFeature.class)
      .add(MoveIssueFeature.class);
  }

  @Named("github")
  @Provides
  @Singleton
  Path path(@Named("config") final Path path) {
    return path.resolve("github");
  }

  @Named("github_feature")
  @Provides
  @Singleton
  Path featurePath(@Named("github") final Path path) {
    return path.resolve("feature");
  }

  @Named("identity")
  @Provides
  @Singleton
  Node identityConfiguration(@Named("github") final Path path) throws IOException, JDOMException {
    return Documents.read(path.resolve("identity.xml"));
  }

  @Named("identity")
  @Provides
  @Singleton
  User identity(@Named("identity") final Node node) throws XMLException {
    return new User(node.requireAttribute("login").value());
  }

  @Provides
  @Singleton
  GitHub github(final Gson gson, @Named("identity") final Node config) throws XMLException {
    return GitHub.builder()
      .gson(gson)
      .token(config.requireAttribute("token").value())
      .build();
  }

  @Provides
  @Singleton
  Repositories repositories(final GitHub gh) {
    return gh.repositories();
  }

  @Provides
  @Singleton
  Users users(final GitHub gh) {
    return gh.users();
  }
}
