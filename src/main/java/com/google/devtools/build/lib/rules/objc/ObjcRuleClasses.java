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

package com.google.devtools.build.lib.rules.objc;

import static com.google.devtools.build.lib.packages.Attribute.attr;
import static com.google.devtools.build.lib.packages.BuildType.LABEL;
import static com.google.devtools.build.lib.packages.BuildType.LABEL_LIST;
import static com.google.devtools.build.lib.packages.BuildType.NODEP_LABEL;
import static com.google.devtools.build.lib.packages.Type.BOOLEAN;
import static com.google.devtools.build.lib.packages.Type.STRING;
import static com.google.devtools.build.lib.packages.Type.STRING_LIST;

import com.google.common.collect.ImmutableMap;
import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.analysis.BaseRuleClasses;
import com.google.devtools.build.lib.analysis.RuleConfiguredTargetBuilder;
import com.google.devtools.build.lib.analysis.RuleContext;
import com.google.devtools.build.lib.analysis.RuleDefinition;
import com.google.devtools.build.lib.analysis.RuleDefinitionEnvironment;
import com.google.devtools.build.lib.analysis.Runfiles;
import com.google.devtools.build.lib.analysis.RunfilesProvider;
import com.google.devtools.build.lib.analysis.actions.SpawnAction;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;
import com.google.devtools.build.lib.packages.RuleClass;
import com.google.devtools.build.lib.packages.RuleClass.Builder.RuleClassType;
import com.google.devtools.build.lib.packages.Type;
import com.google.devtools.build.lib.rules.apple.AppleConfiguration;
import com.google.devtools.build.lib.rules.apple.ApplePlatform;
import com.google.devtools.build.lib.rules.apple.XcodeConfigInfo;
import com.google.devtools.build.lib.rules.cpp.CcInfo;
import com.google.devtools.build.lib.rules.cpp.CcToolchainProvider;
import com.google.devtools.build.lib.rules.cpp.CcToolchainRule;
import com.google.devtools.build.lib.rules.cpp.CppRuleClasses;
import com.google.devtools.build.lib.util.FileType;
import com.google.devtools.build.lib.util.FileTypeSet;

/** Shared rule classes and associated utility code for Objective-C rules. */
public class ObjcRuleClasses {
  static final String STRIP = "strip";

  private ObjcRuleClasses() {
    throw new UnsupportedOperationException("static-only");
  }

  /** A string constant for feature to link a bundle. */
  static final String LINK_BUNDLE_FEATURE = "link_bundle";

  /** A string constant for feature to link a dylib. */
  static final String LINK_DYLIB_FEATURE = "link_dylib";

  /** Attribute name for a dummy target in a child configuration. */
  static final String CHILD_CONFIG_ATTR = "$child_configuration_dummy";

  public static Artifact artifactByAppendingToBaseName(RuleContext context, String suffix) {
    return context.getPackageRelativeArtifact(
        context.getLabel().getName() + suffix, context.getBinOrGenfilesDirectory());
  }

  public static ObjcConfiguration objcConfiguration(RuleContext ruleContext) {
    return ruleContext.getFragment(ObjcConfiguration.class);
  }

  /**
   * Returns true if the source file can be instrumented for coverage.
   */
  public static boolean isInstrumentable(Artifact sourceArtifact) {
    return !ASSEMBLY_SOURCES.matches(sourceArtifact.getFilename());
  }

  /**
   * Creates a new spawn action builder with apple environment variables set that are typically
   * needed by the apple toolchain. This should be used to start to build spawn actions that, in
   * order to run, require both a darwin architecture and a collection of environment variables
   * which contain information about the target and exec architectures.
   */
  static SpawnAction.Builder spawnAppleEnvActionBuilder(
      XcodeConfigInfo xcodeConfigInfo, ApplePlatform targetPlatform) {
    return spawnOnDarwinActionBuilder(xcodeConfigInfo)
        .setEnvironment(appleToolchainEnvironment(xcodeConfigInfo, targetPlatform));
  }

  /** Returns apple environment variables that are typically needed by the apple toolchain. */
  private static ImmutableMap<String, String> appleToolchainEnvironment(
      XcodeConfigInfo xcodeConfigInfo, ApplePlatform targetPlatform) {
    return ImmutableMap.<String, String>builder()
        .putAll(
            AppleConfiguration.appleTargetPlatformEnv(
                targetPlatform, xcodeConfigInfo.getSdkVersionForPlatform(targetPlatform)))
        .putAll(AppleConfiguration.getXcodeVersionEnv(xcodeConfigInfo.getXcodeVersion()))
        .buildOrThrow();
  }

  /** Creates a new spawn action builder that requires a darwin architecture to run. */
  private static SpawnAction.Builder spawnOnDarwinActionBuilder(XcodeConfigInfo xcodeConfigInfo) {
    return new SpawnAction.Builder().setExecutionInfo(xcodeConfigInfo.getExecutionRequirements());
  }

  /**
   * Creates a new configured target builder with the given {@code filesToBuild}, which are also
   * used as runfiles.
   *
   * @param ruleContext the current rule context
   * @param filesToBuild files to build for this target. These also become the data runfiles
   */
  static RuleConfiguredTargetBuilder ruleConfiguredTarget(RuleContext ruleContext,
      NestedSet<Artifact> filesToBuild) {
    RunfilesProvider runfilesProvider = RunfilesProvider.withData(
        new Runfiles.Builder(
            ruleContext.getWorkspaceName(),
            ruleContext.getConfiguration().legacyExternalRunfiles())
            .addRunfiles(ruleContext, RunfilesProvider.DEFAULT_RUNFILES).build(),
        new Runfiles.Builder(
            ruleContext.getWorkspaceName(),
            ruleContext.getConfiguration().legacyExternalRunfiles())
            .addTransitiveArtifacts(filesToBuild).build());

    return new RuleConfiguredTargetBuilder(ruleContext)
        .setFilesToBuild(filesToBuild)
        .add(RunfilesProvider.class, runfilesProvider);
  }

  /**
   * Attributes for {@code objc_*} rules that have compiler options.
   */
  public static class CoptsRule implements RuleDefinition {
    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment environment) {
      return builder
          /* <!-- #BLAZE_RULE($objc_opts_rule).ATTRIBUTE(copts) -->
          Extra flags to pass to the compiler.
          Subject to <a href="${link make-variables}">"Make variable"</a> substitution and
          <a href="${link common-definitions#sh-tokenization}">Bourne shell tokenization</a>.
          These flags will only apply to this target, and not those upon which
          it depends, or those which depend on it.
          <p>
          Note that for the generated Xcode project, directory paths specified using "-I" flags in
          copts are parsed out, prepended with "$(WORKSPACE_ROOT)/" if they are relative paths, and
          added to the header search paths for the associated Xcode target.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("copts", STRING_LIST))
          .build();
    }

    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_opts_rule")
          .type(RuleClassType.ABSTRACT)
          .build();
    }
  }

  /**
   * Attributes for {@code objc_*} rules that can link in SDK frameworks.
   */
  public static class SdkFrameworksDependerRule implements RuleDefinition {
    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment environment) {
      return builder
          /* <!-- #BLAZE_RULE($objc_sdk_frameworks_depender_rule).ATTRIBUTE(sdk_frameworks) -->
          Names of SDK frameworks to link with (e.g. "AddressBook", "QuartzCore"). "UIKit" and
          "Foundation" are always included when building for the iOS, tvOS, visionOS,
          and watchOS platforms. For macOS, only "Foundation" is always included.

          <p> When linking a top level Apple binary, all SDK frameworks listed in that binary's
          transitive dependency graph are linked.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("sdk_frameworks", STRING_LIST))
          /* <!-- #BLAZE_RULE($objc_sdk_frameworks_depender_rule).ATTRIBUTE(weak_sdk_frameworks) -->
          Names of SDK frameworks to weakly link with. For instance,
          "MediaAccessibility".

          In difference to regularly linked SDK frameworks, symbols
          from weakly linked frameworks do not cause an error if they
          are not present at runtime.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("weak_sdk_frameworks", STRING_LIST))
          /* <!-- #BLAZE_RULE($objc_sdk_frameworks_depender_rule).ATTRIBUTE(sdk_dylibs) -->
          Names of SDK .dylib libraries to link with. For instance, "libz" or
          "libarchive".

          "libc++" is included automatically if the binary has any C++ or
          Objective-C++ sources in its dependency tree. When linking a binary,
          all libraries named in that binary's transitive dependency graph are
          used.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("sdk_dylibs", STRING_LIST))
          .build();
    }

    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_sdk_frameworks_depender_rule")
          .type(RuleClassType.ABSTRACT)
          .build();
    }
  }

  /** Iff a file matches this type, it is considered to use C++. */
  static final FileType CPP_SOURCES = FileType.of(".cc", ".cpp", ".mm", ".cxx", ".C");

  static final FileType NON_CPP_SOURCES = FileType.of(".m", ".c");

  static final FileType ASSEMBLY_SOURCES = FileType.of(".s", ".S", ".asm");

  static final FileType OBJECT_FILE_SOURCES = FileType.of(".o");

  /**
   * Header files, which are not compiled directly, but may be included/imported from source files.
   */
  static final FileType HEADERS = FileType.of(".h", ".inc", ".hpp", ".hh");

  /**
   * Files allowed in the srcs attribute. This includes private headers.
   */
  static final FileTypeSet SRCS_TYPE =
      FileTypeSet.of(
          NON_CPP_SOURCES,
          CPP_SOURCES,
          ASSEMBLY_SOURCES,
          OBJECT_FILE_SOURCES,
          HEADERS);

  /** Files that should actually be compiled. */
  static final FileTypeSet COMPILABLE_SRCS_TYPE =
      FileTypeSet.of(NON_CPP_SOURCES, CPP_SOURCES, ASSEMBLY_SOURCES);

  /**
   * Files that are already compiled.
   */
  static final FileTypeSet PRECOMPILED_SRCS_TYPE = FileTypeSet.of(OBJECT_FILE_SOURCES);

  /**
   * Coverage note files which contain information to reconstruct the basic block graphs and assign
   * source line numbers to blocks.
   */
  static final FileType COVERAGE_NOTES = FileType.of(".gcno");

  /**
   * Common attributes for {@code objc_*} rules that depend on a crosstool.
   */
  public static class CrosstoolRule implements RuleDefinition {
    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment env) {
      return builder
          .add(
              attr(CcToolchainRule.CC_TOOLCHAIN_DEFAULT_ATTRIBUTE_NAME, LABEL)
                  .mandatoryProviders(CcToolchainProvider.PROVIDER.id())
                  .value(CppRuleClasses.ccToolchainAttribute(env)))
          .add(
              attr(CcToolchainRule.CC_TOOLCHAIN_TYPE_ATTRIBUTE_NAME, NODEP_LABEL)
                  .value(CppRuleClasses.ccToolchainTypeAttribute(env)))
          .addToolchainTypes(CppRuleClasses.ccToolchainTypeRequirement(env))
          .build();
    }

    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_crosstool_rule")
          .ancestors(CppRuleClasses.CcLinkingRule.class)
          .type(RuleClassType.ABSTRACT)
          .build();
    }
  }

  /**
   * Common attributes for {@code objc_*} rules that can be input to compilation (i.e. can be
   * dependencies of other compiling rules).
   */
  public static class CompileDependencyRule implements RuleDefinition {
    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment env) {
      return builder
          /* <!-- #BLAZE_RULE($objc_compile_dependency_rule).ATTRIBUTE(hdrs) -->
          The list of C, C++, Objective-C, and Objective-C++ header files published
          by this library to be included by sources in dependent rules.
          <p>
          These headers describe the public interface for the library and will be
          made available for inclusion by sources in this rule or in dependent
          rules. Headers not meant to be included by a client of this library
          should be listed in the srcs attribute instead.
          <p>
          These will be compiled separately from the source if modules are enabled.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("hdrs", LABEL_LIST).direct_compile_time_input().allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compile_dependency_rule).ATTRIBUTE(textual_hdrs) -->
          The list of C, C++, Objective-C, and Objective-C++ files that are
          included as headers by source files in this rule or by users of this
          library. Unlike hdrs, these will not be compiled separately from the
          sources.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("textual_hdrs", LABEL_LIST).direct_compile_time_input().allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compile_dependency_rule).ATTRIBUTE(includes) -->
          List of <code>#include/#import</code> search paths to add to this target
          and all depending targets.

          This is to support third party and open-sourced libraries that do not
          specify the entire workspace path in their
          <code>#import/#include</code> statements.
          <p>
          The paths are interpreted relative to the package directory, and the
          genfiles and bin roots (e.g. <code>blaze-genfiles/pkg/includedir</code>
          and <code>blaze-out/pkg/includedir</code>) are included in addition to the
          actual client root.
          <p>
          Unlike <a href="${link objc_library.copts}">COPTS</a>, these flags are added for this rule
          and every rule that depends on it. (Note: not the rules it depends upon!) Be
          very careful, since this may have far-reaching effects.  When in doubt, add
          "-iquote" flags to <a href="${link objc_library.copts}">COPTS</a> instead.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("includes", Type.STRING_LIST))
          /* <!-- #BLAZE_RULE($objc_compile_dependency_rule).ATTRIBUTE(sdk_includes) -->
          List of <code>#include/#import</code> search paths to add to this target
          and all depending targets, where each path is relative to
          <code>$(SDKROOT)/usr/include</code>.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("sdk_includes", Type.STRING_LIST))
          .build();
    }
    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_compile_dependency_rule")
          .type(RuleClassType.ABSTRACT)
          .ancestors(CppRuleClasses.CcIncludeScanningRule.class, SdkFrameworksDependerRule.class)
          .build();
    }
  }

  /**
   * Common attributes for {@code objc_*} rules that contain compilable content.
   */
  public static class CompilingRule implements RuleDefinition {

    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment env) {
      return builder
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(srcs) -->
          The list of C, C++, Objective-C, and Objective-C++ source and header
          files, and/or (`.s`, `.S`, or `.asm`) assembly source files, that are processed to create
          the library target.
          These are your checked-in files, plus any generated files.
          Source files are compiled into .o files with Clang. Header files
          may be included/imported by any source or header in the srcs attribute
          of this target, but not by headers in hdrs or any targets that depend
          on this rule.
          Additionally, precompiled .o files may be given as srcs.  Be careful to
          ensure consistency in the architecture of provided .o files and that of the
          build to avoid missing symbol linker errors.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("srcs", LABEL_LIST).direct_compile_time_input().allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(non_arc_srcs) -->
          The list of Objective-C files that are processed to create the
          library target that DO NOT use ARC.
          The files in this attribute are treated very similar to those in the
          srcs attribute, but are compiled without ARC enabled.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("non_arc_srcs", LABEL_LIST).direct_compile_time_input().allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(pch) -->
          Header file to prepend to every source file being compiled (both arc
          and non-arc).
          Use of pch files is actively discouraged in BUILD files, and this should be
          considered deprecated. Since pch files are not actually precompiled this is not
          a build-speed enhancement, and instead is just a global dependency. From a build
          efficiency point of view you are actually better including what you need directly
          in your sources where you need it.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("pch", LABEL).direct_compile_time_input().allowedFileTypes(FileType.of(".pch")))
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(deps) -->
          The list of targets that are linked together to form the final bundle.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .override(
              attr("deps", LABEL_LIST).mandatoryProviders(CcInfo.PROVIDER.id()).allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(implementation_deps) -->
          The list of other libraries that the library target depends on. Unlike with
          <code>deps</code>, the headers and include paths of these libraries (and all their
          transitive deps) are only used for compilation of this library, and not libraries that
          depend on it. Libraries specified with <code>implementation_deps</code> are still linked
          in binary targets that depend on this library.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(
              attr("implementation_deps", LABEL_LIST)
                  .mandatoryProviders(CcInfo.PROVIDER.id())
                  .allowedFileTypes())
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(defines) -->
          Extra <code>-D</code> flags to pass to the compiler. They should be in
          the form <code>KEY=VALUE</code> or simply <code>KEY</code> and are
          passed not only to the compiler for this target (as <code>copts</code>
          are) but also to all <code>objc_</code> dependers of this target.
          Subject to <a href="${link make-variables}">"Make variable"</a> substitution and
          <a href="${link common-definitions#sh-tokenization}">Bourne shell tokenization</a>.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("defines", STRING_LIST))
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(enable_modules) -->
          Enables clang module support (via -fmodules).
          Setting this to 1 will allow you to @import system headers and other targets:
          @import UIKit;
          @import path_to_package_target;
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("enable_modules", BOOLEAN))
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(module_map) -->
          A custom Clang module map for this target. Use of a custom module map is discouraged. Most
          users should use module maps generated by Bazel.
          If specified, Bazel will not generate a module map for this target, but will pass the
          provided module map to the compiler.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("module_map", LABEL).allowedFileTypes(FileType.of(".modulemap")))
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(module_name) -->
          Sets the module name for this target. By default the module name is the target path with
          all special symbols replaced by _, e.g. //foo/baz:bar can be imported as foo_baz_bar.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("module_name", STRING))
          /* <!-- #BLAZE_RULE($objc_compiling_rule).ATTRIBUTE(linkopts) -->
          Extra flags to pass to the linker.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("linkopts", STRING_LIST))
          .build();
    }
    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_compiling_rule")
          .type(RuleClassType.ABSTRACT)
          .ancestors(
              BaseRuleClasses.NativeActionCreatingRule.class,
              CompileDependencyRule.class,
              CoptsRule.class,
              CrosstoolRule.class)
          .build();
    }
  }

  /**
   * Common attributes for {@code objc_*} rules that can optionally be set to {@code alwayslink}.
   */
  public static class AlwaysLinkRule implements RuleDefinition {
    @Override
    public RuleClass build(RuleClass.Builder builder, RuleDefinitionEnvironment env) {
      return builder
          /* <!-- #BLAZE_RULE($objc_alwayslink_rule).ATTRIBUTE(alwayslink) -->
          If 1, any bundle or binary that depends (directly or indirectly) on this
          library will link in all the object files for the files listed in
          <code>srcs</code> and <code>non_arc_srcs</code>, even if some contain no
          symbols referenced by the binary.
          This is useful if your code isn't explicitly called by code in
          the binary, e.g., if your code registers to receive some callback
          provided by some service.
          <!-- #END_BLAZE_RULE.ATTRIBUTE -->*/
          .add(attr("alwayslink", BOOLEAN))
          .build();
    }
    @Override
    public Metadata getMetadata() {
      return RuleDefinition.Metadata.builder()
          .name("$objc_alwayslink_rule")
          .type(RuleClassType.ABSTRACT)
          .ancestors(CompileDependencyRule.class)
          .build();
    }
  }

  /** Attribute name for apple platform type (e.g. ios or watchos). */
  static final String PLATFORM_TYPE_ATTR_NAME = "platform_type";

  /** Attribute name for the minimum OS version (e.g. "7.3"). */
  static final String MINIMUM_OS_VERSION = "minimum_os_version";
}