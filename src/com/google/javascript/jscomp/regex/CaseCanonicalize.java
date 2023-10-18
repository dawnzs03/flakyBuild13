/*
 * Copyright 2011 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.regex;

import com.google.common.collect.ImmutableList;

/**
 * Implements the ECMAScript 5
 * <a href="http://es5.github.com/#Canonicalize">Canonicalize</a> operation
 * used to specify how case-insensitive regular expressions match.
 *
 * <p>
 * From section <a href="http://es5.github.com/#x15.10.2.9">15.10.2.9</a>,
 * <blockquote>
 * The abstract operation Canonicalize takes a character parameter ch and
 * performs the following steps:
 * <ul>
 *   <li>If IgnoreCase is false, return ch.
 *   <li>Let u be ch converted to upper case as if by calling the standard
 *   built-in method {@code String.prototype.toUpperCase} on the one-character
 *   String ch.
 *   <li>If u does not consist of a single character, return ch.
 *   <li>Let cu be u's character.
 *   <li>If ch's code unit value is greater than or equal to decimal 128 and
 *   cu's code unit value is less than decimal 128, then return ch.
 *   <li>Return cu.
 * </ul>
 * </blockquote>
 */
public final class CaseCanonicalize {

  private CaseCanonicalize() {
    // Uninstantiable.
  }

  // Below are tables that implement the Canonicalize operation.
  // We cannot use java.lang.Character.toUpperCase since that is based on
  // a more modern version of Unicode than that required by the ECMAScript spec.

  /**
   * Set of code units that are case-insensitively equivalent to some other
   * code unit according to the EcmaScript
   * <a href="http://es5.github.com/#Canonicalize">Canonicalize</a> operation
   * described in section 15.10.2.8.
   * The case sensitive characters are the ones that canonicalize to a character
   * other than themselves or have a character that canonicalizes to them.
   * Canonicalize is based on the definition of
   * {@code String.prototype.toUpperCase} which is itself based on Unicode 3.0.0
   * as specified at
   * <a href="ftp://ftp.unicode.org/Public/3.0-Update/UnicodeData-3.0.0.txt">
   * UnicodeData-3.0.0
   * </a>
   * and <a href="ftp://ftp.unicode.org/Public/3.0-Update/SpecialCasing-2.txt">
   * SpecialCasings-2.txt
   * </a>.
   *
   * <p>
   * This table was generated by running the below on Chrome:
   * </p>
   * <pre>
   * for (var cc = 0; cc &lt; 0x10000; ++cc) {
   *   var ch = String.fromCharCode(cc);
   *   var u = ch.toUpperCase();
   *   if (ch != u &amp;&amp; u.length === 1) {
   *     var cu = u.charCodeAt(0);
   *     if (cc &lt;= 128 || u.charCodeAt(0) &gt; 128) {
   *       print('0x' + cc.toString(16) + ', 0x' + cu.toString(16) + ',');
   *     }
   *   }
   * }
   * </pre>
   */
  public static final CharRanges CASE_SENSITIVE = CharRanges.withRanges(
      0x41, 0x5b,
      0x61, 0x7b,
      0xb5, 0xb6,
      0xc0, 0xd7,
      0xd8, 0xdf,
      0xe0, 0xf7,
      0xf8, 0x130,
      0x132, 0x138,
      0x139, 0x149,
      0x14a, 0x17f,
      0x180, 0x18d,
      0x18e, 0x19b,
      0x19c, 0x1aa,
      0x1ac, 0x1ba,
      0x1bc, 0x1be,
      0x1bf, 0x1c0,
      0x1c4, 0x1f0,
      0x1f1, 0x221,
      0x222, 0x234,
      0x23a, 0x23f,
      0x241, 0x250,
      0x253, 0x255,
      0x256, 0x258,
      0x259, 0x25a,
      0x25b, 0x25c,
      0x260, 0x261,
      0x263, 0x264,
      0x268, 0x26a,
      0x26b, 0x26c,
      0x26f, 0x270,
      0x272, 0x273,
      0x275, 0x276,
      0x27d, 0x27e,
      0x280, 0x281,
      0x283, 0x284,
      0x288, 0x28d,
      0x292, 0x293,
      0x345, 0x346,
      0x37b, 0x37e,
      0x386, 0x387,
      0x388, 0x38b,
      0x38c, 0x38d,
      0x38e, 0x390,
      0x391, 0x3a2,
      0x3a3, 0x3b0,
      0x3b1, 0x3cf,
      0x3d0, 0x3d2,
      0x3d5, 0x3d7,
      0x3d8, 0x3f3,
      0x3f5, 0x3f6,
      0x3f7, 0x3fc,
      0x3fd, 0x482,
      0x48a, 0x514,
      0x531, 0x557,
      0x561, 0x587,
      0x10a0, 0x10c6,
      0x1d7d, 0x1d7e,
      0x1e00, 0x1e96,
      0x1e9b, 0x1e9c,
      0x1ea0, 0x1efa,
      0x1f00, 0x1f16,
      0x1f18, 0x1f1e,
      0x1f20, 0x1f46,
      0x1f48, 0x1f4e,
      0x1f51, 0x1f52,
      0x1f53, 0x1f54,
      0x1f55, 0x1f56,
      0x1f57, 0x1f58,
      0x1f59, 0x1f5a,
      0x1f5b, 0x1f5c,
      0x1f5d, 0x1f5e,
      0x1f5f, 0x1f7e,
      0x1fb0, 0x1fb2,
      0x1fb8, 0x1fbc,
      0x1fbe, 0x1fbf,
      0x1fc8, 0x1fcc,
      0x1fd0, 0x1fd2,
      0x1fd8, 0x1fdc,
      0x1fe0, 0x1fe2,
      0x1fe5, 0x1fe6,
      0x1fe8, 0x1fed,
      0x1ff8, 0x1ffc,
      0x2132, 0x2133,
      0x214e, 0x214f,
      0x2160, 0x2180,
      0x2183, 0x2185,
      0x24b6, 0x24ea,
      0x2c00, 0x2c2f,
      0x2c30, 0x2c5f,
      0x2c60, 0x2c6d,
      0x2c75, 0x2c77,
      0x2c80, 0x2ce4,
      0x2d00, 0x2d26,
      0xff21, 0xff3b,
      0xff41, 0xff5b
      );


  /**
   * Returns the case canonical version of the given string.
   */
  public static String caseCanonicalize(String s) {
    for (int i = 0, n = s.length(); i < n; ++i) {
      char ch = s.charAt(i);
      char cu = caseCanonicalize(ch);
      if (cu != ch) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(i, cu);
        while (++i < n) {
          sb.setCharAt(i, caseCanonicalize(s.charAt(i)));
        }
        return sb.toString();
      }
    }
    return s;
  }

  /**
   * Returns the case canonical version of the given code-unit.  ECMAScript 5
   * explicitly says that code-units are to be treated as their code-point
   * equivalent, even surrogates.
   */
  public static char caseCanonicalize(char ch) {
    if (ch < 0x80) {  // Normal case.
      return ('A' <= ch && ch <= 'Z') ? (char) (ch | 32) : ch;
    }
    // Non-ASCII case.
    if (CASE_SENSITIVE.contains(ch)) {
      for (DeltaSet ds : CANON_DELTA_SETS) {
        if (ds.codeUnits.contains(ch)) {
          return (char) (ch - ds.delta);
        }
      }
    }
    return ch;
  }

  /**
   * Given a character range that may include case sensitive code-units,
   * such as {@code [0-9B-M]}, returns the character range that includes all
   * the code-units in the input and those that are case-insensitively
   * equivalent to a code-unit in the input.
   */
  public static CharRanges expandToAllMatched(CharRanges ranges) {
    CharRanges caseSensitive = ranges.intersection(CASE_SENSITIVE);
    if (caseSensitive.isEmpty()) { return ranges; }
    CharRanges expanded = CharRanges.EMPTY;
    for (DeltaSet ds : DELTA_SETS) {
      expanded = expanded.union(
          caseSensitive.intersection(ds.codeUnits).shift(-ds.delta));
    }
    return ranges.union(expanded);
  }


  private static final CharRanges UCASE_ASCII_LETTERS
      = CharRanges.inclusive('A', 'Z');

  /**
   * Given a character range that may include case sensitive code-units,
   * such as {@code [0-9B-M]}, returns the character range that includes
   * the minimal set of code units such that for every code unit in the
   * input there is a case-sensitively equivalent canonical code unit in the
   * output.
   */
  public static CharRanges reduceToMinimum(CharRanges ranges) {
    CharRanges caseSensitive = ranges.intersection(CASE_SENSITIVE);
    if (caseSensitive.isEmpty()) { return ranges; }
    CharRanges expanded = CharRanges.EMPTY;
    for (DeltaSet ds : CANON_DELTA_SETS) {
      expanded = expanded.union(
          caseSensitive.intersection(ds.codeUnits).shift(-ds.delta));
    }
    // Letters a-z gzip better than uppercase A-Z since JavaScript keywords
    // are lower-case, so, even though the definition of Canonicalize is
    // based on String.prototype.toUpperCase, we use lowercase ASCII characters
    // in the minimal form.
    expanded = expanded.difference(UCASE_ASCII_LETTERS).union(
        expanded.intersection(UCASE_ASCII_LETTERS).shift(32));
    return ranges.difference(caseSensitive).union(expanded);
  }

  /**
   * Sets of code units broken down by delta that are case-insensitively
   * equivalent to another code unit that differs from the first by that delta.
   */
  private static final ImmutableList<DeltaSet> DELTA_SETS = ImmutableList.of(
      new DeltaSet(-10795, CharRanges.withMembers(0x23a)),
      new DeltaSet(-10792, CharRanges.withMembers(0x23e)),
      new DeltaSet(-10743, CharRanges.withMembers(0x26b)),
      new DeltaSet(-10727, CharRanges.withMembers(0x27d)),
      new DeltaSet(-7264, CharRanges.withRanges(0x10a0, 0x10c6)),
      new DeltaSet(-7205, CharRanges.withMembers(0x399)),
      new DeltaSet(-3814, CharRanges.withMembers(0x1d7d)),
      new DeltaSet(-743, CharRanges.withMembers(0xb5)),
      new DeltaSet(-219, CharRanges.withMembers(0x1b7)),
      new DeltaSet(-218, CharRanges.withMembers(0x1a6, 0x1a9, 0x1ae)),
      new DeltaSet(-217, CharRanges.withRanges(0x1b1, 0x1b3)),
      new DeltaSet(-214, CharRanges.withMembers(0x19f)),
      new DeltaSet(-213, CharRanges.withMembers(0x19d)),
      new DeltaSet(-211, CharRanges.withMembers(0x196, 0x19c)),
      new DeltaSet(-210, CharRanges.withMembers(0x181)),
      new DeltaSet(-209, CharRanges.withMembers(0x197)),
      new DeltaSet(-207, CharRanges.withMembers(0x194)),
      new DeltaSet(-206, CharRanges.withMembers(0x186)),
      new DeltaSet(-205, CharRanges.withRanges(0x189, 0x18b, 0x193, 0x194)),
      new DeltaSet(-203, CharRanges.withMembers(0x190)),
      new DeltaSet(-202, CharRanges.withMembers(0x18f)),
      new DeltaSet(-195, CharRanges.withMembers(0x180)),
      new DeltaSet(-163, CharRanges.withMembers(0x19a)),
      new DeltaSet(-130, CharRanges.withRanges(0x19e, 0x19f, 0x37b, 0x37e)),
      new DeltaSet(-128, CharRanges.withRanges(0x1f78, 0x1f7a)),
      new DeltaSet(-126, CharRanges.withRanges(0x1f7c, 0x1f7e)),
      new DeltaSet(-121, CharRanges.withMembers(0xff)),
      new DeltaSet(-112, CharRanges.withRanges(0x1f7a, 0x1f7c)),
      new DeltaSet(-100, CharRanges.withRanges(0x1f76, 0x1f78)),
      new DeltaSet(-97, CharRanges.withMembers(0x195)),
      new DeltaSet(-96, CharRanges.withMembers(0x395)),
      new DeltaSet(-86, CharRanges.withRanges(0x39a, 0x39b, 0x1f72, 0x1f76)),
      new DeltaSet(-84, CharRanges.withMembers(0x345)),
      new DeltaSet(-80, CharRanges.withRanges(0x3a1, 0x3a2, 0x400, 0x410)),
      new DeltaSet(-79, CharRanges.withMembers(0x18e)),
      new DeltaSet(-74, CharRanges.withRanges(0x1f70, 0x1f72)),
      new DeltaSet(-71, CharRanges.withMembers(0x245)),
      new DeltaSet(-69, CharRanges.withMembers(0x244)),
      new DeltaSet(-64, CharRanges.withMembers(0x38c)),
      new DeltaSet(-63, CharRanges.withRanges(0x38e, 0x390)),
      new DeltaSet(-62, CharRanges.withMembers(0x392)),
      new DeltaSet(-59, CharRanges.withMembers(0x1e60)),
      new DeltaSet(-57, CharRanges.withMembers(0x398)),
      new DeltaSet(-56, CharRanges.withMembers(0x1bf)),
      new DeltaSet(-54, CharRanges.withMembers(0x3a0)),
      new DeltaSet(-48, CharRanges.withRanges(0x531, 0x557, 0x2c00, 0x2c2f)),
      new DeltaSet(-47, CharRanges.withMembers(0x3a6)),
      new DeltaSet(-38, CharRanges.withMembers(0x386)),
      new DeltaSet(-37, CharRanges.withRanges(0x388, 0x38b)),
      new DeltaSet(-32, CharRanges.withRanges(
          0x41, 0x5b, 0xc0, 0xd7, 0xd8, 0xdf, 0x391, 0x3a2, 0x3a3, 0x3ac,
          0x410, 0x430, 0xff21, 0xff3b)),
      new DeltaSet(-31, CharRanges.withMembers(0x3a3)),
      new DeltaSet(-28, CharRanges.withMembers(0x2132)),
      new DeltaSet(-26, CharRanges.withRanges(0x24b6, 0x24d0)),
      new DeltaSet(-16, CharRanges.withRanges(0x2160, 0x2170)),
      new DeltaSet(-15, CharRanges.withMembers(0x4c0)),
      new DeltaSet(-8, CharRanges.withRanges(
          0x1f00, 0x1f08, 0x1f10, 0x1f16, 0x1f20, 0x1f28, 0x1f30, 0x1f38,
          0x1f40, 0x1f46, 0x1f51, 0x1f52, 0x1f53, 0x1f54, 0x1f55, 0x1f56,
          0x1f57, 0x1f58, 0x1f60, 0x1f68, 0x1fb0, 0x1fb2, 0x1fd0, 0x1fd2,
          0x1fe0, 0x1fe2)),
      new DeltaSet(-7, CharRanges.withMembers(0x3f2, 0x1fe5)),
      new DeltaSet(-2, CharRanges.withMembers(0x1c4, 0x1c7, 0x1ca, 0x1f1)),
      new DeltaSet(-1, CharRanges.withMembers(
          0x100, 0x102, 0x104, 0x106, 0x108, 0x10a, 0x10c, 0x10e, 0x110, 0x112,
          0x114, 0x116, 0x118, 0x11a, 0x11c, 0x11e, 0x120, 0x122, 0x124, 0x126,
          0x128, 0x12a, 0x12c, 0x12e, 0x132, 0x134, 0x136, 0x139, 0x13b, 0x13d,
          0x13f, 0x141, 0x143, 0x145, 0x147, 0x14a, 0x14c, 0x14e, 0x150, 0x152,
          0x154, 0x156, 0x158, 0x15a, 0x15c, 0x15e, 0x160, 0x162, 0x164, 0x166,
          0x168, 0x16a, 0x16c, 0x16e, 0x170, 0x172, 0x174, 0x176, 0x179, 0x17b,
          0x17d, 0x182, 0x184, 0x187, 0x18b, 0x191, 0x198, 0x1a0, 0x1a2, 0x1a4,
          0x1a7, 0x1ac, 0x1af, 0x1b3, 0x1b5, 0x1b8, 0x1bc, 0x1c4, 0x1c7, 0x1ca,
          0x1cd, 0x1cf, 0x1d1, 0x1d3, 0x1d5, 0x1d7, 0x1d9, 0x1db, 0x1de, 0x1e0,
          0x1e2, 0x1e4, 0x1e6, 0x1e8, 0x1ea, 0x1ec, 0x1ee, 0x1f1, 0x1f4, 0x1f8,
          0x1fa, 0x1fc, 0x1fe, 0x200, 0x202, 0x204, 0x206, 0x208, 0x20a, 0x20c,
          0x20e, 0x210, 0x212, 0x214, 0x216, 0x218, 0x21a, 0x21c, 0x21e, 0x222,
          0x224, 0x226, 0x228, 0x22a, 0x22c, 0x22e, 0x230, 0x232, 0x23b, 0x241,
          0x246, 0x248, 0x24a, 0x24c, 0x24e, 0x3d8, 0x3da, 0x3dc, 0x3de, 0x3e0,
          0x3e2, 0x3e4, 0x3e6, 0x3e8, 0x3ea, 0x3ec, 0x3ee, 0x3f7, 0x3fa, 0x460,
          0x462, 0x464, 0x466, 0x468, 0x46a, 0x46c, 0x46e, 0x470, 0x472, 0x474,
          0x476, 0x478, 0x47a, 0x47c, 0x47e, 0x480, 0x48a, 0x48c, 0x48e, 0x490,
          0x492, 0x494, 0x496, 0x498, 0x49a, 0x49c, 0x49e, 0x4a0, 0x4a2, 0x4a4,
          0x4a6, 0x4a8, 0x4aa, 0x4ac, 0x4ae, 0x4b0, 0x4b2, 0x4b4, 0x4b6, 0x4b8,
          0x4ba, 0x4bc, 0x4be, 0x4c1, 0x4c3, 0x4c5, 0x4c7, 0x4c9, 0x4cb, 0x4cd,
          0x4d0, 0x4d2, 0x4d4, 0x4d6, 0x4d8, 0x4da, 0x4dc, 0x4de, 0x4e0, 0x4e2,
          0x4e4, 0x4e6, 0x4e8, 0x4ea, 0x4ec, 0x4ee, 0x4f0, 0x4f2, 0x4f4, 0x4f6,
          0x4f8, 0x4fa, 0x4fc, 0x4fe, 0x500, 0x502, 0x504, 0x506, 0x508, 0x50a,
          0x50c, 0x50e, 0x510, 0x512, 0x1e00, 0x1e02, 0x1e04, 0x1e06, 0x1e08,
          0x1e0a, 0x1e0c, 0x1e0e, 0x1e10, 0x1e12, 0x1e14, 0x1e16, 0x1e18,
          0x1e1a, 0x1e1c, 0x1e1e, 0x1e20, 0x1e22, 0x1e24, 0x1e26, 0x1e28,
          0x1e2a, 0x1e2c, 0x1e2e, 0x1e30, 0x1e32, 0x1e34, 0x1e36, 0x1e38,
          0x1e3a, 0x1e3c, 0x1e3e, 0x1e40, 0x1e42, 0x1e44, 0x1e46, 0x1e48,
          0x1e4a, 0x1e4c, 0x1e4e, 0x1e50, 0x1e52, 0x1e54, 0x1e56, 0x1e58,
          0x1e5a, 0x1e5c, 0x1e5e, 0x1e60, 0x1e62, 0x1e64, 0x1e66, 0x1e68,
          0x1e6a, 0x1e6c, 0x1e6e, 0x1e70, 0x1e72, 0x1e74, 0x1e76, 0x1e78,
          0x1e7a, 0x1e7c, 0x1e7e, 0x1e80, 0x1e82, 0x1e84, 0x1e86, 0x1e88,
          0x1e8a, 0x1e8c, 0x1e8e, 0x1e90, 0x1e92, 0x1e94, 0x1ea0, 0x1ea2,
          0x1ea4, 0x1ea6, 0x1ea8, 0x1eaa, 0x1eac, 0x1eae, 0x1eb0, 0x1eb2,
          0x1eb4, 0x1eb6, 0x1eb8, 0x1eba, 0x1ebc, 0x1ebe, 0x1ec0, 0x1ec2,
          0x1ec4, 0x1ec6, 0x1ec8, 0x1eca, 0x1ecc, 0x1ece, 0x1ed0, 0x1ed2,
          0x1ed4, 0x1ed6, 0x1ed8, 0x1eda, 0x1edc, 0x1ede, 0x1ee0, 0x1ee2,
          0x1ee4, 0x1ee6, 0x1ee8, 0x1eea, 0x1eec, 0x1eee, 0x1ef0, 0x1ef2,
          0x1ef4, 0x1ef6, 0x1ef8, 0x2183, 0x2c60, 0x2c67, 0x2c69, 0x2c6b,
          0x2c75, 0x2c80, 0x2c82, 0x2c84, 0x2c86, 0x2c88, 0x2c8a, 0x2c8c,
          0x2c8e, 0x2c90, 0x2c92, 0x2c94, 0x2c96, 0x2c98, 0x2c9a, 0x2c9c,
          0x2c9e, 0x2ca0, 0x2ca2, 0x2ca4, 0x2ca6, 0x2ca8, 0x2caa, 0x2cac,
          0x2cae, 0x2cb0, 0x2cb2, 0x2cb4, 0x2cb6, 0x2cb8, 0x2cba, 0x2cbc,
          0x2cbe, 0x2cc0, 0x2cc2, 0x2cc4, 0x2cc6, 0x2cc8, 0x2cca, 0x2ccc,
          0x2cce, 0x2cd0, 0x2cd2, 0x2cd4, 0x2cd6, 0x2cd8, 0x2cda, 0x2cdc,
          0x2cde, 0x2ce0, 0x2ce2)),
      new DeltaSet(1, CharRanges.withMembers(
          0x101, 0x103, 0x105, 0x107, 0x109, 0x10b, 0x10d, 0x10f, 0x111, 0x113,
          0x115, 0x117, 0x119, 0x11b, 0x11d, 0x11f, 0x121, 0x123, 0x125, 0x127,
          0x129, 0x12b, 0x12d, 0x12f, 0x133, 0x135, 0x137, 0x13a, 0x13c, 0x13e,
          0x140, 0x142, 0x144, 0x146, 0x148, 0x14b, 0x14d, 0x14f, 0x151, 0x153,
          0x155, 0x157, 0x159, 0x15b, 0x15d, 0x15f, 0x161, 0x163, 0x165, 0x167,
          0x169, 0x16b, 0x16d, 0x16f, 0x171, 0x173, 0x175, 0x177, 0x17a, 0x17c,
          0x17e, 0x183, 0x185, 0x188, 0x18c, 0x192, 0x199, 0x1a1, 0x1a3, 0x1a5,
          0x1a8, 0x1ad, 0x1b0, 0x1b4, 0x1b6, 0x1b9, 0x1bd, 0x1c5, 0x1c8, 0x1cb,
          0x1ce, 0x1d0, 0x1d2, 0x1d4, 0x1d6, 0x1d8, 0x1da, 0x1dc, 0x1df, 0x1e1,
          0x1e3, 0x1e5, 0x1e7, 0x1e9, 0x1eb, 0x1ed, 0x1ef, 0x1f2, 0x1f5, 0x1f9,
          0x1fb, 0x1fd, 0x1ff, 0x201, 0x203, 0x205, 0x207, 0x209, 0x20b, 0x20d,
          0x20f, 0x211, 0x213, 0x215, 0x217, 0x219, 0x21b, 0x21d, 0x21f, 0x223,
          0x225, 0x227, 0x229, 0x22b, 0x22d, 0x22f, 0x231, 0x233, 0x23c, 0x242,
          0x247, 0x249, 0x24b, 0x24d, 0x24f, 0x3d9, 0x3db, 0x3dd, 0x3df, 0x3e1,
          0x3e3, 0x3e5, 0x3e7, 0x3e9, 0x3eb, 0x3ed, 0x3ef, 0x3f8, 0x3fb, 0x461,
          0x463, 0x465, 0x467, 0x469, 0x46b, 0x46d, 0x46f, 0x471, 0x473, 0x475,
          0x477, 0x479, 0x47b, 0x47d, 0x47f, 0x481, 0x48b, 0x48d, 0x48f, 0x491,
          0x493, 0x495, 0x497, 0x499, 0x49b, 0x49d, 0x49f, 0x4a1, 0x4a3, 0x4a5,
          0x4a7, 0x4a9, 0x4ab, 0x4ad, 0x4af, 0x4b1, 0x4b3, 0x4b5, 0x4b7, 0x4b9,
          0x4bb, 0x4bd, 0x4bf, 0x4c2, 0x4c4, 0x4c6, 0x4c8, 0x4ca, 0x4cc, 0x4ce,
          0x4d1, 0x4d3, 0x4d5, 0x4d7, 0x4d9, 0x4db, 0x4dd, 0x4df, 0x4e1, 0x4e3,
          0x4e5, 0x4e7, 0x4e9, 0x4eb, 0x4ed, 0x4ef, 0x4f1, 0x4f3, 0x4f5, 0x4f7,
          0x4f9, 0x4fb, 0x4fd, 0x4ff, 0x501, 0x503, 0x505, 0x507, 0x509, 0x50b,
          0x50d, 0x50f, 0x511, 0x513, 0x1e01, 0x1e03, 0x1e05, 0x1e07, 0x1e09,
          0x1e0b, 0x1e0d, 0x1e0f, 0x1e11, 0x1e13, 0x1e15, 0x1e17, 0x1e19,
          0x1e1b, 0x1e1d, 0x1e1f, 0x1e21, 0x1e23, 0x1e25, 0x1e27, 0x1e29,
          0x1e2b, 0x1e2d, 0x1e2f, 0x1e31, 0x1e33, 0x1e35, 0x1e37, 0x1e39,
          0x1e3b, 0x1e3d, 0x1e3f, 0x1e41, 0x1e43, 0x1e45, 0x1e47, 0x1e49,
          0x1e4b, 0x1e4d, 0x1e4f, 0x1e51, 0x1e53, 0x1e55, 0x1e57, 0x1e59,
          0x1e5b, 0x1e5d, 0x1e5f, 0x1e61, 0x1e63, 0x1e65, 0x1e67, 0x1e69,
          0x1e6b, 0x1e6d, 0x1e6f, 0x1e71, 0x1e73, 0x1e75, 0x1e77, 0x1e79,
          0x1e7b, 0x1e7d, 0x1e7f, 0x1e81, 0x1e83, 0x1e85, 0x1e87, 0x1e89,
          0x1e8b, 0x1e8d, 0x1e8f, 0x1e91, 0x1e93, 0x1e95, 0x1ea1, 0x1ea3,
          0x1ea5, 0x1ea7, 0x1ea9, 0x1eab, 0x1ead, 0x1eaf, 0x1eb1, 0x1eb3,
          0x1eb5, 0x1eb7, 0x1eb9, 0x1ebb, 0x1ebd, 0x1ebf, 0x1ec1, 0x1ec3,
          0x1ec5, 0x1ec7, 0x1ec9, 0x1ecb, 0x1ecd, 0x1ecf, 0x1ed1, 0x1ed3,
          0x1ed5, 0x1ed7, 0x1ed9, 0x1edb, 0x1edd, 0x1edf, 0x1ee1, 0x1ee3,
          0x1ee5, 0x1ee7, 0x1ee9, 0x1eeb, 0x1eed, 0x1eef, 0x1ef1, 0x1ef3,
          0x1ef5, 0x1ef7, 0x1ef9, 0x2184, 0x2c61, 0x2c68, 0x2c6a, 0x2c6c,
          0x2c76, 0x2c81, 0x2c83, 0x2c85, 0x2c87, 0x2c89, 0x2c8b, 0x2c8d,
          0x2c8f, 0x2c91, 0x2c93, 0x2c95, 0x2c97, 0x2c99, 0x2c9b, 0x2c9d,
          0x2c9f, 0x2ca1, 0x2ca3, 0x2ca5, 0x2ca7, 0x2ca9, 0x2cab, 0x2cad,
          0x2caf, 0x2cb1, 0x2cb3, 0x2cb5, 0x2cb7, 0x2cb9, 0x2cbb, 0x2cbd,
          0x2cbf, 0x2cc1, 0x2cc3, 0x2cc5, 0x2cc7, 0x2cc9, 0x2ccb, 0x2ccd,
          0x2ccf, 0x2cd1, 0x2cd3, 0x2cd5, 0x2cd7, 0x2cd9, 0x2cdb, 0x2cdd,
          0x2cdf, 0x2ce1, 0x2ce3)),
      new DeltaSet(2, CharRanges.withMembers(0x1c6, 0x1c9, 0x1cc, 0x1f3)),
      new DeltaSet(7, CharRanges.withMembers(0x3f9, 0x1fec)),
      new DeltaSet(8, CharRanges.withRanges(
          0x1f08, 0x1f10, 0x1f18, 0x1f1e, 0x1f28, 0x1f30, 0x1f38, 0x1f40,
          0x1f48, 0x1f4e, 0x1f59, 0x1f5a, 0x1f5b, 0x1f5c, 0x1f5d, 0x1f5e,
          0x1f5f, 0x1f60, 0x1f68, 0x1f70, 0x1fb8, 0x1fba, 0x1fd8, 0x1fda,
          0x1fe8, 0x1fea)),
      new DeltaSet(15, CharRanges.withMembers(0x4cf)),
      new DeltaSet(16, CharRanges.withRanges(0x2170, 0x2180)),
      new DeltaSet(26, CharRanges.withRanges(0x24d0, 0x24ea)),
      new DeltaSet(28, CharRanges.withMembers(0x214e)),
      new DeltaSet(31, CharRanges.withMembers(0x3c2)),
      new DeltaSet(32, CharRanges.withRanges(
          0x61, 0x7b, 0xe0, 0xf7, 0xf8, 0xff, 0x3b1, 0x3c2, 0x3c3, 0x3cc,
          0x430, 0x450, 0xff41, 0xff5b)),
      new DeltaSet(37, CharRanges.withRanges(0x3ad, 0x3b0)),
      new DeltaSet(38, CharRanges.withMembers(0x3ac)),
      new DeltaSet(47, CharRanges.withMembers(0x3d5)),
      new DeltaSet(48, CharRanges.withRanges(0x561, 0x587, 0x2c30, 0x2c5f)),
      new DeltaSet(54, CharRanges.withMembers(0x3d6)),
      new DeltaSet(56, CharRanges.withMembers(0x1f7)),
      new DeltaSet(57, CharRanges.withMembers(0x3d1)),
      new DeltaSet(59, CharRanges.withMembers(0x1e9b)),
      new DeltaSet(62, CharRanges.withMembers(0x3d0)),
      new DeltaSet(63, CharRanges.withRanges(0x3cd, 0x3cf)),
      new DeltaSet(64, CharRanges.withMembers(0x3cc)),
      new DeltaSet(69, CharRanges.withMembers(0x289)),
      new DeltaSet(71, CharRanges.withMembers(0x28c)),
      new DeltaSet(74, CharRanges.withRanges(0x1fba, 0x1fbc)),
      new DeltaSet(79, CharRanges.withMembers(0x1dd)),
      new DeltaSet(80, CharRanges.withRanges(0x3f1, 0x3f2, 0x450, 0x460)),
      new DeltaSet(84, CharRanges.withMembers(0x399)),
      new DeltaSet(86, CharRanges.withRanges(0x3f0, 0x3f1, 0x1fc8, 0x1fcc)),
      new DeltaSet(96, CharRanges.withMembers(0x3f5)),
      new DeltaSet(97, CharRanges.withMembers(0x1f6)),
      new DeltaSet(100, CharRanges.withRanges(0x1fda, 0x1fdc)),
      new DeltaSet(112, CharRanges.withRanges(0x1fea, 0x1fec)),
      new DeltaSet(121, CharRanges.withMembers(0x178)),
      new DeltaSet(126, CharRanges.withRanges(0x1ffa, 0x1ffc)),
      new DeltaSet(128, CharRanges.withRanges(0x1ff8, 0x1ffa)),
      new DeltaSet(130, CharRanges.withRanges(0x220, 0x221, 0x3fd, 0x400)),
      new DeltaSet(163, CharRanges.withMembers(0x23d)),
      new DeltaSet(195, CharRanges.withMembers(0x243)),
      new DeltaSet(202, CharRanges.withMembers(0x259)),
      new DeltaSet(203, CharRanges.withMembers(0x25b)),
      new DeltaSet(205, CharRanges.withRanges(0x256, 0x258, 0x260, 0x261)),
      new DeltaSet(206, CharRanges.withMembers(0x254)),
      new DeltaSet(207, CharRanges.withMembers(0x263)),
      new DeltaSet(209, CharRanges.withMembers(0x268)),
      new DeltaSet(210, CharRanges.withMembers(0x253)),
      new DeltaSet(211, CharRanges.withMembers(0x269, 0x26f)),
      new DeltaSet(213, CharRanges.withMembers(0x272)),
      new DeltaSet(214, CharRanges.withMembers(0x275)),
      new DeltaSet(217, CharRanges.withRanges(0x28a, 0x28c)),
      new DeltaSet(218, CharRanges.withMembers(0x280, 0x283, 0x288)),
      new DeltaSet(219, CharRanges.withMembers(0x292)),
      new DeltaSet(743, CharRanges.withMembers(0x39c)),
      new DeltaSet(3814, CharRanges.withMembers(0x2c63)),
      new DeltaSet(7205, CharRanges.withMembers(0x1fbe)),
      new DeltaSet(7264, CharRanges.withRanges(0x2d00, 0x2d26)),
      new DeltaSet(10727, CharRanges.withMembers(0x2c64)),
      new DeltaSet(10743, CharRanges.withMembers(0x2c62)),
      new DeltaSet(10792, CharRanges.withMembers(0x2c66)),
      new DeltaSet(10795, CharRanges.withMembers(0x2c65))
      );

  private static final ImmutableList<DeltaSet> CANON_DELTA_SETS
      = ImmutableList.of(
      new DeltaSet(-10743, CharRanges.withMembers(0x26b)),
      new DeltaSet(-10727, CharRanges.withMembers(0x27d)),
      new DeltaSet(-3814, CharRanges.withMembers(0x1d7d)),
      new DeltaSet(-743, CharRanges.withMembers(0xb5)),
      new DeltaSet(-195, CharRanges.withMembers(0x180)),
      new DeltaSet(-163, CharRanges.withMembers(0x19a)),
      new DeltaSet(-130, CharRanges.withRanges(0x19e, 0x19f, 0x37b, 0x37e)),
      new DeltaSet(-128, CharRanges.withRanges(0x1f78, 0x1f7a)),
      new DeltaSet(-126, CharRanges.withRanges(0x1f7c, 0x1f7e)),
      new DeltaSet(-121, CharRanges.withMembers(0xff)),
      new DeltaSet(-112, CharRanges.withRanges(0x1f7a, 0x1f7c)),
      new DeltaSet(-100, CharRanges.withRanges(0x1f76, 0x1f78)),
      new DeltaSet(-97, CharRanges.withMembers(0x195)),
      new DeltaSet(-86, CharRanges.withRanges(0x1f72, 0x1f76)),
      new DeltaSet(-84, CharRanges.withMembers(0x345)),
      new DeltaSet(-74, CharRanges.withRanges(0x1f70, 0x1f72)),
      new DeltaSet(-56, CharRanges.withMembers(0x1bf)),
      new DeltaSet(-8, CharRanges.withRanges(
          0x1f00, 0x1f08, 0x1f10, 0x1f16, 0x1f20, 0x1f28, 0x1f30, 0x1f38,
          0x1f40, 0x1f46, 0x1f51, 0x1f52, 0x1f53, 0x1f54, 0x1f55, 0x1f56,
          0x1f57, 0x1f58, 0x1f60, 0x1f68, 0x1fb0, 0x1fb2, 0x1fd0, 0x1fd2,
          0x1fe0, 0x1fe2)),
      new DeltaSet(-7, CharRanges.withMembers(0x3f2, 0x1fe5)),
      new DeltaSet(1, CharRanges.withMembers(
          0x101, 0x103, 0x105, 0x107, 0x109, 0x10b, 0x10d, 0x10f, 0x111, 0x113,
          0x115, 0x117, 0x119, 0x11b, 0x11d, 0x11f, 0x121, 0x123, 0x125, 0x127,
          0x129, 0x12b, 0x12d, 0x12f, 0x133, 0x135, 0x137, 0x13a, 0x13c, 0x13e,
          0x140, 0x142, 0x144, 0x146, 0x148, 0x14b, 0x14d, 0x14f, 0x151, 0x153,
          0x155, 0x157, 0x159, 0x15b, 0x15d, 0x15f, 0x161, 0x163, 0x165, 0x167,
          0x169, 0x16b, 0x16d, 0x16f, 0x171, 0x173, 0x175, 0x177, 0x17a, 0x17c,
          0x17e, 0x183, 0x185, 0x188, 0x18c, 0x192, 0x199, 0x1a1, 0x1a3, 0x1a5,
          0x1a8, 0x1ad, 0x1b0, 0x1b4, 0x1b6, 0x1b9, 0x1bd, 0x1c5, 0x1c8, 0x1cb,
          0x1ce, 0x1d0, 0x1d2, 0x1d4, 0x1d6, 0x1d8, 0x1da, 0x1dc, 0x1df, 0x1e1,
          0x1e3, 0x1e5, 0x1e7, 0x1e9, 0x1eb, 0x1ed, 0x1ef, 0x1f2, 0x1f5, 0x1f9,
          0x1fb, 0x1fd, 0x1ff, 0x201, 0x203, 0x205, 0x207, 0x209, 0x20b, 0x20d,
          0x20f, 0x211, 0x213, 0x215, 0x217, 0x219, 0x21b, 0x21d, 0x21f, 0x223,
          0x225, 0x227, 0x229, 0x22b, 0x22d, 0x22f, 0x231, 0x233, 0x23c, 0x242,
          0x247, 0x249, 0x24b, 0x24d, 0x24f, 0x3d9, 0x3db, 0x3dd, 0x3df, 0x3e1,
          0x3e3, 0x3e5, 0x3e7, 0x3e9, 0x3eb, 0x3ed, 0x3ef, 0x3f8, 0x3fb, 0x461,
          0x463, 0x465, 0x467, 0x469, 0x46b, 0x46d, 0x46f, 0x471, 0x473, 0x475,
          0x477, 0x479, 0x47b, 0x47d, 0x47f, 0x481, 0x48b, 0x48d, 0x48f, 0x491,
          0x493, 0x495, 0x497, 0x499, 0x49b, 0x49d, 0x49f, 0x4a1, 0x4a3, 0x4a5,
          0x4a7, 0x4a9, 0x4ab, 0x4ad, 0x4af, 0x4b1, 0x4b3, 0x4b5, 0x4b7, 0x4b9,
          0x4bb, 0x4bd, 0x4bf, 0x4c2, 0x4c4, 0x4c6, 0x4c8, 0x4ca, 0x4cc, 0x4ce,
          0x4d1, 0x4d3, 0x4d5, 0x4d7, 0x4d9, 0x4db, 0x4dd, 0x4df, 0x4e1, 0x4e3,
          0x4e5, 0x4e7, 0x4e9, 0x4eb, 0x4ed, 0x4ef, 0x4f1, 0x4f3, 0x4f5, 0x4f7,
          0x4f9, 0x4fb, 0x4fd, 0x4ff, 0x501, 0x503, 0x505, 0x507, 0x509, 0x50b,
          0x50d, 0x50f, 0x511, 0x513, 0x1e01, 0x1e03, 0x1e05, 0x1e07, 0x1e09,
          0x1e0b, 0x1e0d, 0x1e0f, 0x1e11, 0x1e13, 0x1e15, 0x1e17, 0x1e19,
          0x1e1b, 0x1e1d, 0x1e1f, 0x1e21, 0x1e23, 0x1e25, 0x1e27, 0x1e29,
          0x1e2b, 0x1e2d, 0x1e2f, 0x1e31, 0x1e33, 0x1e35, 0x1e37, 0x1e39,
          0x1e3b, 0x1e3d, 0x1e3f, 0x1e41, 0x1e43, 0x1e45, 0x1e47, 0x1e49,
          0x1e4b, 0x1e4d, 0x1e4f, 0x1e51, 0x1e53, 0x1e55, 0x1e57, 0x1e59,
          0x1e5b, 0x1e5d, 0x1e5f, 0x1e61, 0x1e63, 0x1e65, 0x1e67, 0x1e69,
          0x1e6b, 0x1e6d, 0x1e6f, 0x1e71, 0x1e73, 0x1e75, 0x1e77, 0x1e79,
          0x1e7b, 0x1e7d, 0x1e7f, 0x1e81, 0x1e83, 0x1e85, 0x1e87, 0x1e89,
          0x1e8b, 0x1e8d, 0x1e8f, 0x1e91, 0x1e93, 0x1e95, 0x1ea1, 0x1ea3,
          0x1ea5, 0x1ea7, 0x1ea9, 0x1eab, 0x1ead, 0x1eaf, 0x1eb1, 0x1eb3,
          0x1eb5, 0x1eb7, 0x1eb9, 0x1ebb, 0x1ebd, 0x1ebf, 0x1ec1, 0x1ec3,
          0x1ec5, 0x1ec7, 0x1ec9, 0x1ecb, 0x1ecd, 0x1ecf, 0x1ed1, 0x1ed3,
          0x1ed5, 0x1ed7, 0x1ed9, 0x1edb, 0x1edd, 0x1edf, 0x1ee1, 0x1ee3,
          0x1ee5, 0x1ee7, 0x1ee9, 0x1eeb, 0x1eed, 0x1eef, 0x1ef1, 0x1ef3,
          0x1ef5, 0x1ef7, 0x1ef9, 0x2184, 0x2c61, 0x2c68, 0x2c6a, 0x2c6c,
          0x2c76, 0x2c81, 0x2c83, 0x2c85, 0x2c87, 0x2c89, 0x2c8b, 0x2c8d,
          0x2c8f, 0x2c91, 0x2c93, 0x2c95, 0x2c97, 0x2c99, 0x2c9b, 0x2c9d,
          0x2c9f, 0x2ca1, 0x2ca3, 0x2ca5, 0x2ca7, 0x2ca9, 0x2cab, 0x2cad,
          0x2caf, 0x2cb1, 0x2cb3, 0x2cb5, 0x2cb7, 0x2cb9, 0x2cbb, 0x2cbd,
          0x2cbf, 0x2cc1, 0x2cc3, 0x2cc5, 0x2cc7, 0x2cc9, 0x2ccb, 0x2ccd,
          0x2ccf, 0x2cd1, 0x2cd3, 0x2cd5, 0x2cd7, 0x2cd9, 0x2cdb, 0x2cdd,
          0x2cdf, 0x2ce1, 0x2ce3)),
      new DeltaSet(2, CharRanges.withMembers(0x1c6, 0x1c9, 0x1cc, 0x1f3)),
      new DeltaSet(15, CharRanges.withMembers(0x4cf)),
      new DeltaSet(16, CharRanges.withRanges(0x2170, 0x2180)),
      new DeltaSet(26, CharRanges.withRanges(0x24d0, 0x24ea)),
      new DeltaSet(28, CharRanges.withMembers(0x214e)),
      new DeltaSet(31, CharRanges.withMembers(0x3c2)),
      new DeltaSet(32, CharRanges.withRanges(
          0x61, 0x7b, 0xe0, 0xf7, 0xf8, 0xff, 0x3b1, 0x3c2, 0x3c3, 0x3cc, 0x430,
          0x450, 0xff41, 0xff5b)),
      new DeltaSet(37, CharRanges.withRanges(0x3ad, 0x3b0)),
      new DeltaSet(38, CharRanges.withMembers(0x3ac)),
      new DeltaSet(47, CharRanges.withMembers(0x3d5)),
      new DeltaSet(48, CharRanges.withRanges(0x561, 0x587, 0x2c30, 0x2c5f)),
      new DeltaSet(54, CharRanges.withMembers(0x3d6)),
      new DeltaSet(57, CharRanges.withMembers(0x3d1)),
      new DeltaSet(59, CharRanges.withMembers(0x1e9b)),
      new DeltaSet(62, CharRanges.withMembers(0x3d0)),
      new DeltaSet(63, CharRanges.withRanges(0x3cd, 0x3cf)),
      new DeltaSet(64, CharRanges.withMembers(0x3cc)),
      new DeltaSet(69, CharRanges.withMembers(0x289)),
      new DeltaSet(71, CharRanges.withMembers(0x28c)),
      new DeltaSet(79, CharRanges.withMembers(0x1dd)),
      new DeltaSet(80, CharRanges.withRanges(0x3f1, 0x3f2, 0x450, 0x460)),
      new DeltaSet(86, CharRanges.withMembers(0x3f0)),
      new DeltaSet(96, CharRanges.withMembers(0x3f5)),
      new DeltaSet(202, CharRanges.withMembers(0x259)),
      new DeltaSet(203, CharRanges.withMembers(0x25b)),
      new DeltaSet(205, CharRanges.withRanges(0x256, 0x258, 0x260, 0x261)),
      new DeltaSet(206, CharRanges.withMembers(0x254)),
      new DeltaSet(207, CharRanges.withMembers(0x263)),
      new DeltaSet(209, CharRanges.withMembers(0x268)),
      new DeltaSet(210, CharRanges.withMembers(0x253)),
      new DeltaSet(211, CharRanges.withMembers(0x269, 0x26f)),
      new DeltaSet(213, CharRanges.withMembers(0x272)),
      new DeltaSet(214, CharRanges.withMembers(0x275)),
      new DeltaSet(217, CharRanges.withRanges(0x28a, 0x28c)),
      new DeltaSet(218, CharRanges.withMembers(0x280, 0x283, 0x288)),
      new DeltaSet(219, CharRanges.withMembers(0x292)),
      new DeltaSet(7205, CharRanges.withMembers(0x1fbe)),
      new DeltaSet(7264, CharRanges.withRanges(0x2d00, 0x2d26)),
      new DeltaSet(10792, CharRanges.withMembers(0x2c66)),
      new DeltaSet(10795, CharRanges.withMembers(0x2c65))
      );


  /**
   * A group of code units such that for all cu in codeUnits, cu is equivalent,
   * case-insensitively, to cu + delta.
   */
  private static final class DeltaSet {
    final int delta;
    final CharRanges codeUnits;

    DeltaSet(int delta, CharRanges codeUnits) {
      this.delta = delta;
      this.codeUnits = codeUnits;
    }
  }

}
