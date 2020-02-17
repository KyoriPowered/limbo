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
package net.kyori.limbo.github;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import net.kyori.event.EventBus;
import net.kyori.limbo.github.api.event.Event;
import net.kyori.limbo.github.api.event.Events;
import net.kyori.limbo.github.api.event.IssueCommentEvent;
import net.kyori.limbo.github.api.event.IssuesEvent;
import net.kyori.limbo.github.api.event.PullRequestEvent;
import net.kyori.limbo.github.api.event.PullRequestReviewEvent;
import net.kyori.limbo.util.Hex;
import net.kyori.limbo.web.SimpleError;
import net.kyori.lunar.crypt.Algorithms;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@Singleton
public final class GitHubEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(GitHubEndpoint.class);
  private static final Map<String, Class<? extends Event>> EVENTS = ImmutableMap.<String, Class<? extends Event>>builder()
    .put(Events.ISSUE_COMMENT, IssueCommentEvent.class)
    .put(Events.ISSUES, IssuesEvent.class)
    .put(Events.PULL_REQUEST, PullRequestEvent.class)
    .put(Events.PULL_REQUEST_REVIEW, PullRequestReviewEvent.class)
    .build();
  private static final String X_GITHUB_EVENT = "X-GitHub-Event";
  private static final String X_HUB_SIGNATURE = "X-Hub-Signature";
  private final byte[] key;
  private final Gson gson;
  private final EventBus<Object> bus;

  @Inject
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private GitHubEndpoint(final @Named("env") Node environment, final Gson gson, final EventBus<Object> bus) throws XMLException {
    this.key = environment.elements("github").one().required().requireAttribute("key").value().getBytes(StandardCharsets.UTF_8);
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
      final /* @Nullable */ Class<? extends Event> event = this.event(xGitHubEvent);
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
    return EVENTS.get(type);
  }

  private boolean verifySignature(final @Nullable String signature, final byte[] payload) {
    if(signature == null) {
      return false;
    }
    if(!signature.startsWith("sha1=")) {
      return false;
    }
    try {
      final SecretKeySpec key = new SecretKeySpec(this.key, Algorithms.HMAC_SHA1);
      final Mac mac = Mac.getInstance(Algorithms.HMAC_SHA1);
      mac.init(key);
      return Hex.asHex(mac.doFinal(payload)).equalsIgnoreCase(signature.substring(5));
    } catch(final InvalidKeyException | NoSuchAlgorithmException e) {
      return false;
    }
  }
}
