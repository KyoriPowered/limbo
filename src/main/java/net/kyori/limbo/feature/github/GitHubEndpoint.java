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

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.kyori.event.EventBus;
import net.kyori.limbo.core.event.Listener;
import net.kyori.limbo.feature.github.api.event.Event;
import net.kyori.limbo.feature.github.api.event.Events;
import net.kyori.limbo.feature.github.api.event.IssueCommentEvent;
import net.kyori.limbo.feature.github.api.event.IssuesEvent;
import net.kyori.limbo.feature.github.api.event.PullRequestEvent;
import net.kyori.limbo.util.Crypt;
import net.kyori.limbo.util.HttpResponse;
import net.kyori.membrane.facet.Enableable;
import ninja.leaping.configurate.ConfigurationNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import spark.Spark;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;

@Singleton
public final class GitHubEndpoint implements Enableable {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final String X_GITHUB_EVENT = "X-GitHub-Event";
  private static final String X_HUB_SIGNATURE = "X-Hub-Signature";
  private final byte[] key;
  private final Gson gson;
  private final EventBus<Object, Listener> bus;

  @Inject
  private GitHubEndpoint(@Named("identity") final ConfigurationNode config, final Gson gson, final EventBus<Object, Listener> bus) {
    this.key = config.getNode("key").getString().getBytes(StandardCharsets.UTF_8);
    this.gson = gson;
    this.bus = bus;
  }

  @Override
  public void enable() {
    LOGGER.info("Registering endpoint");
    Spark.post("/endpoint/github/", (request, response) -> {
      final String signature = request.headers(X_HUB_SIGNATURE);
      final byte[] payload;
      try(final InputStream is = request.raw().getInputStream()) {
        payload = ByteStreams.toByteArray(is);
      }
      if(this.verifySignature(signature, payload)) {
        final String type = request.headers(X_GITHUB_EVENT);
        final @Nullable Class<? extends Event> event = this.event(request.headers(X_GITHUB_EVENT));
        if(event != null) {
          this.bus.post(this.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(payload)), event));
        } else {
          LOGGER.info("Couldn't find an event for '{}'", type);
        }
        return HttpResponse.noContent(response);
      } else {
        return HttpResponse.unauthorized(response);
      }
    });
  }

  private @Nullable Class<? extends Event> event(final String type) {
    switch(type) {
      case Events.ISSUE_COMMENT:
        return IssueCommentEvent.class;
      case Events.ISSUES:
        return IssuesEvent.class;
      case Events.PULL_REQUEST:
        return PullRequestEvent.class;
    }
    return null;
  }

  @Override
  public void disable() {
  }

  private boolean verifySignature(final @Nullable String signature, final byte[] payload) {
    if(signature == null) {
      return false;
    }
    if(!signature.startsWith("sha1=")) {
      return false;
    }
    try {
      final SecretKeySpec key = new SecretKeySpec(this.key, Crypt.HMAC_SHA1_ALGORITHM);
      final Mac mac = Mac.getInstance(Crypt.HMAC_SHA1_ALGORITHM);
      mac.init(key);
      return DatatypeConverter.printHexBinary(mac.doFinal(payload)).equalsIgnoreCase(signature.substring(5));
    } catch(final InvalidKeyException | NoSuchAlgorithmException e) {
      return false;
    }
  }
}
