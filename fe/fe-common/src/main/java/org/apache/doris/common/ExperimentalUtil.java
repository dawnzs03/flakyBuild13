// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.common;

// Currently, this is for FE config and session variable.
public class ExperimentalUtil {
    public static final String EXPERIMENTAL_PREFIX = "experimental_";

    public enum ExperimentalType {
        // Not an experimental item
        NONE,
        // An experimental item, it will be shown with `experimental_` prefix
        // And user can set it with or without `experimental_` prefix.
        EXPERIMENTAL,
        // A previous experimental item but now it is GA.
        // it will be shown without `experimental_` prefix.
        // But user can set it with or without `experimental_` prefix, for compatibility.
        EXPERIMENTAL_ONLINE
    }
}

