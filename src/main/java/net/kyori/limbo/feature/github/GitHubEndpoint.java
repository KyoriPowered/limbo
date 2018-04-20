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
import net.kyori.event.EventBus;
import net.kyori.limbo.core.web.SimpleError;
import net.kyori.limbo.feature.github.api.event.Event;
import net.kyori.limbo.feature.github.api.event.Events;
import net.kyori.limbo.feature.github.api.event.IssueCommentEvent;
import net.kyori.limbo.feature.github.api.event.IssuesEvent;
import net.kyori.limbo.feature.github.api.event.PullRequestEvent;
import net.kyori.limbo.util.Crypt;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.ByteArrayInputStream;
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

@Controller
@Singleton
public final class GitHubEndpoint {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final String X_GITHUB_EVENT = "X-GitHub-Event";
  private static final String X_HUB_SIGNATURE = "X-Hub-Signature";
  private final byte[] key;
  private final Gson gson;
  private final EventBus<Object, Object> bus;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Inject
  private GitHubEndpoint(@Named("env") final Node node, final Gson gson, final EventBus<Object, Object> bus) throws XMLException {
    this.key = node.elements("github").one().need().requireAttribute("key").value().getBytes(StandardCharsets.UTF_8);
    this.gson = gson;
    this.bus = bus;
  }

  @PostMapping("/endpoint/github/")
  public ResponseEntity<?> endpoint(
    final @RequestBody String body,
    final @RequestHeader(X_HUB_SIGNATURE) String xHubSignature,
    final @RequestHeader(X_GITHUB_EVENT) String xGitHubEvent
  ) {
    final byte[] payload = body.getBytes(StandardCharsets.UTF_8);
    if(this.verifySignature(xHubSignature, payload)) {
      final @Nullable Class<? extends Event> event = this.event(xGitHubEvent);
      if(event != null) {
        this.bus.post(this.gson.fromJson(new InputStreamReader(new ByteArrayInputStream(payload)), event));
      } else {
        LOGGER.info("Couldn't find an event for '{}'", xGitHubEvent);
      }
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SimpleError("unauthorized.bad_signature", "could not verify signature"));
    }
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
