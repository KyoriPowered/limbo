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
package net.kyori.limbo.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import net.kyori.fragment.filter.FilterBinder;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.client.Client;
import net.kyori.limbo.discord.action.ActionModule;
import net.kyori.limbo.discord.embed.EmbedParser;
import net.kyori.limbo.discord.feature.gir.GitHubIssueRefFeatureModule;
import net.kyori.limbo.discord.feature.rp.RolePingModule;
import net.kyori.limbo.discord.filter.RoleFilterParser;
import net.kyori.membrane.facet.FacetBinder;
import net.kyori.polar.ForPolar;
import net.kyori.polar.PolarModule;
import net.kyori.polar.util.ColorSerializer;
import net.kyori.polar.util.InstantSerializer;
import net.kyori.violet.DuplexModule;
import net.kyori.xml.node.parser.ParserBinder;

import java.awt.Color;
import java.time.Instant;

public final class DiscordModule extends DuplexModule {
  @Override
  protected void configure() {
    this.install(new PolarModule());

    this.expose(Client.class);

    final FacetBinder facets = new FacetBinder(this.publicBinder());
    facets.addBinding().to(ClientConnector.class);

    final FilterBinder filters = new FilterBinder(this.publicBinder());
    filters.bindFilter("role").to(RoleFilterParser.class);

    final ParserBinder parsers = new ParserBinder(this.publicBinder());
    parsers.bindParser(Embed.class).to(EmbedParser.class);

    this.install(new ActionModule());

    this.install(new GitHubIssueRefFeatureModule());
    this.install(new RolePingModule());
  }

  @ForPolar
  @Provides
  Gson polarGson(final GsonBuilder builder) {
    return builder
      .registerTypeAdapter(Color.class, new ColorSerializer())
      .registerTypeAdapter(Instant.class, new InstantSerializer())
      .create();
  }
}
