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

public interface Events {
  String CHECK_RUN = "check_run";
  String CHECK_SUITE = "check_suite";
  String COMMIT_COMMENT = "commit_comment";
  String CREATE = "create";
  String DELETE = "delete";
  String DEPLOYMENT = "deployment";
  String DEPLOYMENT_STATUS = "deployment_status";
  String FORK = "fork";
  String GOLLUM = "gollum";
  String INSTALLATION = "installation";
  String INSTALLATION_REPOSITORIES = "installation_repositories";
  String ISSUE_COMMENT = "issue_comment";
  String ISSUES = "issues";
  String LABEL = "label";
  String MARKETPLACE_PURCHASE = "marketplace_purchase";
  String MEMBER = "member";
  String MEMBERSHIP = "membership";
  String MILESTONE = "milestone";
  String ORGANIZATION = "organization";
  String ORG_BLOCK = "org_block";
  String PAGE_BUILD = "page_build";
  String PROJECT_CARD = "project_card";
  String PROJECT_COLUMN = "project_column";
  String PROJECT = "project";
  String PUBLIC = "public";
  String PULL_REQUEST = "pull_request";
  String PULL_REQUEST_REVIEW = "pull_request_review";
  String PULL_REQUEST_REVIEW_COMMENT = "pull_request_review_comment";
  String PUSH = "push";
  String RELEASE = "release";
  String REPOSITORY = "repository";
  String REPOSITORY_VULNERABILITY_ALERT = "repository_vulnerability_alert";
  String STATUS = "status";
  String TEAM = "team";
  String TEAM_ADD = "team_add";
  String WATCH = "watch";
}
