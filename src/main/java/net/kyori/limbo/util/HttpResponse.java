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
package net.kyori.limbo.util;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.JsonObject;
import net.kyori.lunar.EvenMoreObjects;
import spark.Response;

public final class HttpResponse {
  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final JsonObject FOUR_OH_FOUR = EvenMoreObjects.make(new JsonObject(), object -> {
    object.addProperty("success", false);
    object.addProperty("error", "four_oh_four");
  });
  private static final JsonObject ERROR_UNAUTHORIZED = EvenMoreObjects.make(new JsonObject(), object -> {
    object.addProperty("success", false);
    object.addProperty("error", "unauthorized");
  });

  private HttpResponse() {
  }

  public static String noContent(final Response response) {
    response.status(HttpStatusCodes.STATUS_CODE_NO_CONTENT);
    return "";
  }

  public static JsonObject fourOhFour(final Response response) {
    response.type(JSON_CONTENT_TYPE);
    response.status(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    return FOUR_OH_FOUR;
  }

  public static JsonObject unauthorized(final Response response) {
    response.type(JSON_CONTENT_TYPE);
    response.status(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
    return ERROR_UNAUTHORIZED;
  }
}
