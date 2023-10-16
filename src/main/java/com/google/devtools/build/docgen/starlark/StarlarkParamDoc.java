// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.docgen.starlark;

import net.starlark.java.annot.Param;
import net.starlark.java.annot.ParamType;

/** A class containing the documentation for a Starlark method parameter. */
public final class StarlarkParamDoc extends StarlarkDoc {
  /** Repesents the param kind, whether it's a normal param or *arg or **kwargs. */
  public static enum Kind {
    NORMAL,
    EXTRA_POSITIONALS,
    EXTRA_KEYWORDS,
  }

  private StarlarkMethodDoc method;
  private Param param;
  private final Kind kind;

  public StarlarkParamDoc(
      StarlarkMethodDoc method, Param param, StarlarkDocExpander expander, Kind kind) {
    super(expander);
    this.method = method;
    this.param = param;
    this.kind = kind;
  }

  /**
   * Returns the string representing the type of this parameter with the link to the documentation
   * for the type if available.
   *
   * <p>If the parameter type is Object, then returns the empty string. If the parameter type is not
   * a generic, then this method returns a string representing the type name with a link to the
   * documentation for the type if available. If the parameter type is a generic, then this method
   * returns a string "CONTAINER of TYPE" (with HTML link markup).
   */
  public String getType() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < param.allowedTypes().length; i++) {
      ParamType paramType = param.allowedTypes()[i];
      // TODO(adonovan): make generic1 an array.
      if (paramType.generic1() == Object.class) {
        sb.append(getTypeAnchor(paramType.type()));
      } else {
        sb.append(getTypeAnchor(paramType.type(), paramType.generic1()));
      }
      if (i < param.allowedTypes().length - 1) {
        sb.append("; or ");
      }
    }
    return sb.toString();
  }

  public Kind getKind() {
    return kind;
  }

  public StarlarkMethodDoc getMethod() {
    return method;
  }

  @Override public String getName() {
    return param.name();
  }

  public String getDefaultValue() {
    return param.defaultValue();
  }

  @Override
  public String getRawDocumentation() {
    String prefixWarning = "";
    if (!param.enableOnlyWithFlag().isEmpty()) {
      prefixWarning =
          "<b>Experimental</b>. This parameter is experimental and may change at any "
              + "time. Please do not depend on it. It may be enabled on an experimental basis by "
              + "setting <code>--"
              + param.enableOnlyWithFlag()
              + "</code> <br>";
    } else if (!param.disableWithFlag().isEmpty()) {
      prefixWarning =
          "<b>Deprecated</b>. This parameter is deprecated and will be removed soon. "
              + "Please do not depend on it. It is <i>disabled</i> with "
              + "<code>--"
              + param.disableWithFlag()
              + "</code>. Use this flag "
              + "to verify your code is compatible with its imminent removal. <br>";
    }
    return prefixWarning + param.doc();
  }
}