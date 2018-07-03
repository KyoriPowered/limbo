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
import net.kyori.limbo.github.api.model.Repository;
import net.kyori.limbo.github.api.model.User;

public final class PullRequestEvent implements Event {
  public Action action;
  public PullRequest pull_request;
  public Repository repository;
  public User sender;

  public enum Action {
    @SerializedName("assigned")
    ASSIGNED,
    @SerializedName("unassigned")
    UNASSIGNED,
    @SerializedName("review_requested")
    REVIEW_REQUESTED,
    @SerializedName("review_request_removed")
    REVIEW_REQUEST_REMOVED,
    @SerializedName("labeled")
    LABELED,
    @SerializedName("unlabeled")
    UNLABELED,
    @SerializedName("opened")
    OPENED,
    @SerializedName("edited")
    EDITED,
    @SerializedName("closed")
    CLOSED,
    @SerializedName("reopened")
    REOPENED;
  }
}
