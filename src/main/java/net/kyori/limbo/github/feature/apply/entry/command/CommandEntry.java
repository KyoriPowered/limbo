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
package net.kyori.limbo.github.feature.apply.entry.command;

import java.util.List;
import net.kyori.fragment.filter.Filter;
import net.kyori.limbo.github.action.Action;
import net.kyori.limbo.github.feature.apply.CollectContext;
import net.kyori.limbo.github.feature.apply.entry.Entry;

public final class CommandEntry extends Entry.Impl {
  private final List<String> commands;
  private final Action action;

  /* package */ CommandEntry(final Filter filter, final List<String> commands, final Action action) {
    super(filter);
    this.commands = commands;
    this.action = action;
  }

  @Override
  public void collect(final CollectContext context, final List<Action> actions) {
    for(final String command : this.commands) {
      for(final String string : context.strings) {
        if(string.trim().startsWith(command)) {
          actions.add(this.action);
          return;
        }
      }
    }
  }
}
