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
package net.kyori.limbo.github.api.event;

import com.google.gson.annotations.SerializedName;
import net.kyori.limbo.github.api.model.PullRequest;
import net.kyori.limbo.github.api.model.PullRequestReview;
import net.kyori.limbo.github.api.model.Repository;
import net.kyori.limbo.github.api.model.User;

public final class PullRequestReviewEvent implements Event {
  public Action action;
  public PullRequest pull_request;
  public PullRequestReview review;
  public Repository repository;
  public User sender;

  public enum Action {
    @SerializedName("dismissed")
    DISMISSED {
      @Override
      public net.kyori.limbo.git.event.Event asEvent() {
        return net.kyori.limbo.git.event.Event.PULL_REQUEST_REVIEW_DISMISSED;
      }
    },
    @SerializedName("edited")
    EDITED {
      @Override
      public net.kyori.limbo.git.event.Event asEvent() {
        return net.kyori.limbo.git.event.Event.PULL_REQUEST_REVIEW_EDITED;
      }
    },
    @SerializedName("submitted")
    SUBMITTED {
      @Override
      public net.kyori.limbo.git.event.Event asEvent() {
        return net.kyori.limbo.git.event.Event.PULL_REQUEST_REVIEW_SUBMITTED;
      }
    };

    public abstract net.kyori.limbo.git.event.Event asEvent();
  }
}
