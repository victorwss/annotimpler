# Annotimpler

Annotation-driven framework for implementing Java interfaces at runtime (proxy-based "getters/setters/DAOs from annotations"), split into small Java Platform Module System (JPMS) modules.

## Build / test / lint

No Gradle wrapper is checked in — use a system `gradle` install. Java 25 or newer is required (`versionJavaCompiler` in `build.gradle`).

- Full build (compile + test + delombok + javadoc + all quality tools + publish to `.maven-repo`):
  `gradle clean fullBuild --continue` (this is what `build.bat` runs, with `--warning-mode all --stacktrace` and Windows UTF-8 codepage).
- Compile only: `gradle compileJava` (or `gradle :sql:compileJava` for one module).
- Run all tests for one module: `gradle :sql:test` (module names: `magicfactory`, `datetime`, `typeser`, `convert`, `core`, `transaction`, `limited`, `sql`, `jdbc`, `jpa`).
- Run a single test class: `gradle :jdbc:test --tests "ninja.javahacker.test.annotimpler.jdbc.stmt.SqlWorkerTest"`.
- Run a single test method: `gradle :jdbc:test --tests "ninja.javahacker.test.annotimpler.jdbc.stmt.SqlWorkerTest.methodName"`.
- `test.ignoreFailures = true` is set project-wide, so `gradle test` always exits 0 — check the printed test summary / XML reports under `<module>/build/test-results`, not the exit code.
- Checkstyle, PMD and SpotBugs also run with `ignoreFailures = true`; inspect reports under `<module>/build/reports/` rather than relying on the gradle exit code.
  Configs live in `config/checkstyle/{main,test}.xml`, `config/pmd-ruleset.xml`, `config/spotbugs-exclude.xml`.
- `gradle javadoc` (per module) or `gradle allJavadoc` (unified, module-graph-aware Javadoc across all modules) depend on a `delombok`/`prepareJavadocSource` step — don't hand-edit anything under `build/`.

## Gradle script layout

Root `build.gradle` auto-includes every top-level directory as a subproject (see `settings.gradle`), except `config`, `libs`, `build`, `test-files`, `gradle`, and dotted dirs.
The root `build.gradle` itself is now a slim dispatcher: it only declares `plugins{}`, sets `group`/`version`, exposes a few SpotBugs plugin classes as `ext` properties
(needed because `apply from:` scripts cannot resolve plugin-classpath classes directly), and applies every script under `gradle/` via `apply from:`.
Each concern lives in its own focused file under `gradle/`:

- `gradle/versions.gradle` — dependency/tool versions as `ext` properties.
- `gradle/repositories.gradle` — repositories shared by all projects.
- `gradle/subprojects.gradle` — applies the common plugins and all the scripts below to every subproject.
- `gradle/subproject-dependencies.gradle`, `gradle/delombok.gradle`, `gradle/compile-and-test.gradle`, `gradle/code-quality.gradle`, `gradle/packaging-and-publishing.gradle` —
  per-subproject task configuration (dependencies common to all modules, delombok, compile/test, Checkstyle/PMD/SpotBugs/JaCoCo, jar/publish).
- `gradle/project-task-ordering.gradle` — **automatically** wires `compileJava`/`delombok` task ordering between modules (see below), replacing manually-repeated
  `dependsOn` chains that used to live inline in each `project(":name") { ... }` block.
- `gradle/all-javadoc.gradle`, `gradle/aggregate-html-report.gradle`, `gradle/aggregate-reports.gradle` — unified/aggregate Javadoc and quality-report tasks across modules.
- `gradle/full-build.gradle` — defines the `fullBuild` umbrella task.
- `gradle/future-modules-template.txt` — commented-out templates for backend modules that don't exist yet (e.g. Hibernate/EclipseLink/OpenJPA `jpa` providers).

Each module now has its **own `build.gradle`** file (e.g. `sql/build.gradle`, `jdbc/build.gradle`) containing only what is specific to that module: `description`,
`jar.archiveBaseName`, its `dependencies {}` block, and any module-specific bits (e.g. `test.jvmArgs` for SQLite native access). Cross-cutting behavior (delombok, quality
tools, publishing, task ordering) is *not* repeated per module — it comes from the `gradle/*.gradle` scripts applied to every subproject.

Task ordering between modules is derived **automatically**: `gradle/project-task-ordering.gradle` inspects each subproject's own declared `ProjectDependency`
entries (i.e. whatever you put in that module's `dependencies { api(project(":x")) }`) in an `afterEvaluate` block and wires
`compileJava.dependsOn(":x:delombok")`, `compileJava.dependsOn(":x:compileJava")` and `delombok.dependsOn(":x:jar")` for every such `x`. This means adding a new
inter-module dependency only requires adding the `api(project(":x"))` line in that module's `build.gradle` — there is no separate, manually-maintained
`compileJava.dependsOn`/`delombok.dependsOn` chain to keep in sync anymore (a source of stale-jar bugs in the old inline layout).

## Module layout & dependency graph

Dependency graph (each module is also a JPMS module, their names are below):

```
typeser      - ninja.javahacker.typeser                   (no deps) – handles serialization of the Type interface.
datetime     - ninja.javahacker.datetime                  (no deps) – flexible java.time parsing/formatting (MultiFormatters).
magicfactory - ninja.javahacker.annotimpler.magicfactory  (no deps) – reflective instance creation ("MagicFactory").
limited      - ninja.javahacker.annotimpler.limited       (no deps) – byte/char-limited stream wrappers.
core         - ninja.javahacker.annotimpler.core          -> magicfactory – annotation-driven interface proxy framework.
convert      - ninja.javahacker.annotimpler.convert       -> magicfactory, datetime – typed value converters.
transaction  - ninja.javahacker.transaction               -> typeser, magicfactory – transaction/`Transactor` abstraction, backend-agnostic.
sql          - ninja.javahacker.annotimpler.sql           -> typeser, magicfactory, transaction, datetime, convert, core – backend-agnostic SQL metadata and sources.
jdbc         - ninja.javahacker.annotimpler.jdbc          -> typeser, magicfactory, datetime, convert, core, sql, limited – JDBC-specific DAO generation/execution.
jpa          - ninja.javahacker.annotimpler.jpa           -> typeser, magicfactory, datetime, convert, core, sql – JPA-specific adapters/integration (prototype).
```

Modules are named `ninja.javahacker.annotimpler.<name>` when they are part of the same tool that implements Java interfaces based on annotations
(the "Annotimpler" itself). Some modules are backend-specific specializations (such as `jdbc` and `jpa`), while `sql` now holds backend-agnostic SQL abstractions.

However, the `datetime` and `typeser` packages are outside of `annotimpler` because they are also clearly useful as independent tools. `transaction` (the
`Transactor` abstraction) is likewise outside `annotimpler`, since transaction management is not conceptually tied to implementing annotated interfaces either.
`limited` (byte/char-limited stream wrappers, used by `jdbc` to cap how much of a `Blob`/`Clob` gets read) lives inside `annotimpler` since it is currently only
consumed there. Other modules like `magicfactory` or `convert` are being considered to live as independent tools in the future, but not for now, not yet.

### `core` module — the annotation-implementation pattern

This is the central architectural idea, spread across several files in `core`:
- An annotation type is tagged `@ImplementedBy(SomeImplementation.class)`.
- Placing that annotation on an interface method tells `AnnotationsImplementor.implement(...)` to build a JDK dynamic `Proxy` for the interface.
- For each method of the interface being proxied, one of these cases applies:
  - **If the method carries an `@ImplementedBy`-annotated annotation**: the designated `Implementation` class is instantiated via `MagicFactory` (no args).
    A `PropertyBag` — containing configuration typically supplied by the client/user code — is passed as a parameter to `AnnotationsImplementor.implement(...)`.
    `Implementation#prepare` is then called on the freshly-created object, also receiving that same `PropertyBag` as a parameter. Since `prepare` is polymorphic,
    the method that actually runs is the one from the custom implementation named in the `@ImplementedBy` annotation. `prepare` produces a `CallContext`,
    which is what actually handles every subsequent invocation of that method on the proxy.
  - **If the method has no such annotation but has a `default` body**: invocations fall back to that `default` method.
  - **If the method is `equals`/`hashCode`/`toString`/`clone`/`finalize`/private/static**: it is handled specially and is off-limits to annotation-driven dispatch,
    since letting it participate would be risky, buggy, dangerous or simply non-working.
  - **Otherwise** (no `@ImplementedBy`-annotated annotation, no `default` body, not one of the special methods above): the method is inherently underspecified,
    so the whole process aborts with an exception.
- `jdbc` builds its DAO-generation feature (`@ExecuteSql`, `@GenerateSql`, `@QuerySql` in `ninja.javahacker.annotimpler.jdbc.sqlimpl`) on top of this same mechanism.

### `transaction` module — the `Transactor` abstraction
- `ninja.javahacker.transaction` (single package) — `Transactor`, a backend-agnostic abstraction for running work inside a transaction (begin/commit/rollback),
  independent of JDBC/JPA/any specific persistence technology. `sql` depends on it and builds its JDBC/JPA-facing transaction support on top of it.

### `limited` module — capped stream wrappers
- `ninja.javahacker.annotimpler.limited` (single package) — `LimitedInputStream` and `LimitedReader`, thin wrappers around an `InputStream`/`Reader` that throw once
  more than a configured maximum number of bytes/characters has been read. Used by `jdbc` to defensively cap how much of a `Blob`/`Clob` value gets pulled into memory.
  This module was split out of `jdbc` (where it used to live as an internal package) so it could be reused/tested independently; it currently has no dependencies
  of its own and is depended on only by `jdbc`.

### `sql` / `jdbc` / `jpa` module packages
- `ninja.javahacker.annotimpler.sql` (backend-agnostic SQL-source annotations, built on top of `transaction`'s `Transactor` abstraction).
- `ninja.javahacker.annotimpler.sql.sqlfactories` (`SqlFactory` impls for file/URL/string SQL sources).
- `ninja.javahacker.annotimpler.sql.meta` (SQL loading, parameter binding, factory resolution).
- `ninja.javahacker.annotimpler.jdbc` (JDBC public API/annotations).
- `ninja.javahacker.annotimpler.jdbc.conn` (JDBC `Connection` factories).
- `ninja.javahacker.annotimpler.jdbc.sqlimpl` (runtime handlers for JDBC SQL annotations).
- `ninja.javahacker.annotimpler.jdbc.stmt` (named-parameter statements, type-aware result sets, executors).
- `ninja.javahacker.annotimpler.jpa` (JPA-facing wrappers and provider adapters).

## Conventions

### Project structure

- Every module has `src/main/java/module-info.java` and `src/test/java/module-info.java` (JPMS `open module` blocks) — new source/test files must be reachable per these module declarations,
  and new module dependencies need a `requires` line here in addition to the Gradle dependency.
- Test code lives under the `ninja.javahacker.test.*` package tree (not mirroring `ninja.javahacker.annotimpler.*` 1:1),
  e.g. `sql`'s tests are `ninja.javahacker.test.annotimpler.sql.*` plus shared test helpers directly in `ninja.javahacker.test` (`ForTests`, `ControlledMock`, `Sneaky`).
  This is done on purpose because we want to draw a bold line telling apart test code and production code,
  we really hate splitting packages among different modules,
  we hate module-patching hacks,
  we strongly reject the common practice of putting tests in the same packages as production code labeling this as a bad practice
  and we want to be strictly compliant and heavily resilient under the severe restrictions imposed by JPMS modules.
- Shared test helper classes (`ForTests`, `Sneaky`, `ControlledMock`, and the `AssertionInputStream`/`AssertionReader` pair used by `limited`'s and `jdbc`'s tests) are
  intentionally **duplicated verbatim** into every test tree that needs them, rather than shared through a cross-module test dependency — this is consistent with the
  "no split packages" rule above, at the cost of some copy-paste. When updating one of these helper classes, remember to update every copy.
- `limited`'s test code lives under `ninja.javahacker.test.limited` (not `ninja.javahacker.test.annotimpler.limited`), since that is the package name it already had
  back when it lived inside `jdbc`'s test tree; `jdbc`'s own tests that still need `AssertionInputStream`/`AssertionReader` keep their own copy under that same
  package name in `jdbc`'s test tree.
- Lombok is used pervasively (`@NonNull` generates `IllegalArgumentException`, not `NPE` — see `lombok.config`).
  `lombok.val`/`@Cleanup`/`@Helper` are disabled (`flagUsage = error`).
  Fields default to non-final, non-private unless annotated.
- Rely on Checkstyle, PMD and SpotBugs to tell what is good or not, but don't rely too much. There are a lot of cases where they don't tell the best, but most of the times, they do.
- Rely on JUnit and JaCoCo to see what was tested and what was covered in the tests.

### Code style

- Source/Javadoc encoding is UTF-8 everywhere; don't introduce non-UTF-8 file content.
  The exceptions are the very rare and very specific cases of testing how to handle non-UTF-8 data and they should be clearly documented as that.
- In production code, lines are limited to 140 chars in length (not the traditional 80, which is too limited in practice).
  The rationale for that is that 80 is frequently too small for having a legible code, and this becomes worse considering that Java code is notably verbose.
  Also, this number 80 comes from long-time abandoned and forgotten restrictions on screen sizes and printer sizes from the 1970's and 1980's.
  So, 140 is what experience shows that is ok for most users nowadays.
- For test code, line limits are relaxed to be longer than 140 chars sometimes. However, if they fit into the 140 char-limit, that is surely better.
- Indentation is 4 spaces. Always. Everywhere.
- No tabulation characters ever. We hate them. I would like to find a way to turn them into syntax errors.
- No spaces at line endings ever.
- Comments should be indented in the same way as the nearby code is.
- Always add a space after `//`, `///` or `/*` in the code (except if there is a line-break immediately after). Similarly, always add a space before `*/`.

### Java Code conventions

Those conventions apply only to production code, not test code.
However, it is a good idea if test code also follow them when possible and convenient, but that is no requirement and sometimes might even be undesired.
Remember that test code, contrarily to production code, sometimes do weird, dangerous, tricky or smelly stuff on purpose in order to properly exercise the production code.

- Use `var` to declare local variables whenever possible.
- Omit type declarations on lambdas whenever possible.
- The `finalize` method is hateful and the production code should be protected from the possibility of having them injected by client/user code.
- Avoid package-default visibility, which should be used only occasionally when using neither `public` nor `private` is a good idea.
- When a package-default visibility is indeed needed or intended, use Lombok's `@PackagePrivate` annotation.
- Never use `protected` visibility.
  The only exception for this rule so far is to block the usage of the `finalize` method.
  Some rare cases where this would be forced by the usage of 3rd-party package (including those in the JDK) are conceivable, but never happened yet and we like this.
- Never use class inheritance (other than `java.lang.Object` and `Throwable` subclasses).
  The exceptions are the cases where this would be forced by the usage of 3rd-party package (including those in the JDK).
- In order to disallow and discourage inheritance usage (except for `Throwable`s), make the classes unoverridable when possible.
  This includes making them explicitly `final`, but also includes them being `enum`s, `record`s, lambdas, anonymous, method-local or non-public.
- Every field, parameter or method return that is not `void` nor a primitive type **must** be annotated with either `@NonNull` or `@Nullable`.
- We hate `null`s, so everything should be `@NonNull` when possible.
  The exceptions are for those cases where the usage of `null` is required, needed or expected by a 3rd-party package (including those in the JDK).
- When some field or parameter might legitimately be absent or omitted, consider using `Optional` with also `@NonNull`.
- Although interface methods are implicitly `public`, the `public` modifier should be explicitly present there regardless of what.
- Blocks `do`, `while`, `for` and `else` must always use `{` and `}`.
- The `if` blocks do not need to use `{` and `}` when they are one-liner and have no `else`. Otherwise, they also need to use `{` and `}`.
- We don't like to suppress warnings coming from Javac, Javadoc, Checkstyle, PMD or SpotBugs, but sometimes, we need to.
  When this happens, a comment explaining the reason for the suppression might be worth.
  The `"unchecked"` however, is unfortunately far too common and frequently unavoidable, so most of times it doesn't even need that comment.
- Remove useless warnings suppressions. Those are likely leftovers from past refactorings that don't need to be there anymore.

### Code assertions

- Non-public methods (or public methods in non-public classes or interfaces) have their nullarity checked by a method that **must** be called `checkNotNull`.
  This is because Lombok have special handling for the exact name `checkNotNull`, elliding the explicit null-check.
- The `checkNotNull` method throws an `AssertionError` when it detects a nullarity violation because since it should be used only in non-public methods, that means a programming error.
- Code annotated with `@Generated` is excluded from JaCoCo, and this is the justification to add the `@Generated` annotation even in manually-written code.
- In some rare cases, a few methods other than `checkNotNull` are also annotated with `@Generated`.
  They are always either semantically equivalent to an assert or are a poor-man ad-hoc way to perform polymorphism.
  No other manually-written code should bear the `@Generated` annotation.
- The only reason to exclude something from JaCoCo analysis (deserving the manually-placed `@Generated` annotation) is when they contain code branches that should
  be unreachable by design and thus impossible to be reached or can't be reached in reasonable client code.
  Allowing them would ruin the JaCoCo reports about code coverage if not somehow excluded/ignored.

### Javadocs

- Javadoc uses the newer `///` line-comment style (JEP 467) throughout, never `/** ... */` blocks.
- Test code do not need Javadocs and the javadoc tool would never be run on them (and this would make no sense). Javadocs are only for production code.
- However, if some test code have a Javadoc anyway, keep it there and keep it up-to-date as if it were in production code.
- All public classes **must** have class Javadocs.
- All public methods, fields and constructors on public classes **must** have Javadocs.
- Fields and constructors (even private) also deserve Javadocs (as a good practice and to please PMD).
- Overriden methods (even on non-public classes) also deserve Javadocs (as a good practice and to please PMD). Even if it is nothing more than `/// {@inheritDoc}`.
- If PMD says something must have a Javadoc, then PMD is right.
- If the javadoc tool gives a warning or error on missing, malformed or misplaced javadoc, please fix it.
- Public methods with `@NonNull` parameters must document the constraint via `@param` ("must not be null") and `@throws IllegalArgumentException` Javadoc tags instead of
  relying on the `@NonNull` annotation showing up in Javadoc (see the JDK-8175533 workaround under "Workarounds" below).
- Use a single space between phrases in Javadocs. Don't use double spaces.
- Keywords like `this`, `null`, `false` and `true` **must** be presented as code, not as plaintext (i.e. between backticks in markdown).
- Code that is designed to sometimes throw an unchecked exception must document it the same way it would do for a checked exception.
- If you see a Javadoc with a typo, or that is missing something important or that tells something that is incorrect, please, fix it.

### Imports

- Module imports are preferred over traditional-style imports.
- Don't ever use static imports.
- Don't ever use star imports.
- The main exceptions for module imports are when there is an ambiguity in imported classes or when PMD's bug #6867 is triggered or when Lombok's bug #3969 is triggered.

### Dependencies

- Don't ever introduce new dependencies, no matter what.
- Not even to test code or to build scripts. Don't!
- However, bumping up already existing version dependencies is fine.
- Only the human maintainer can eventually introduce dependencies and only after a long, cautious and detailed reasoning.

## Workarounds

- No `@NonNull` annotations are present in the resulting Javadocs as a workaround for JDK-8175533 (see the `prepareJavadocSource` task in `build.gradle`,
  which strips `@NonNull` from the delomboked source before Javadoc generation runs). This is why `@NonNull` constraints must instead be documented via
  `@param`/`@throws IllegalArgumentException` tags (see "Javadocs" above). When there is a reliable way to fix this bug, we would love to see those
  annotations back in the Javadocs.
- PMD's bug #6867 makes PMD crash when it finds module imports containing ambiguous class names and one of those names is used in the code.
  In the cases when this bug is triggered, the affected module imports must be forsaken and replaced by traditional-style imports.
  We would love to see this bug fixed.
- Lombok's version is not an official one. It is a modified version based on Lombok 1.18.46.
  The only modification is including the code on Lombok's Pull Request #3392, recompiling it manually and adding the newly-generated JAR (version 1.18.47) into the `libs` source.
  This hack is needed because delombok goes nuts when seeing multirelease modular JARs on modulepath, but Lombok's developers still did not integrate the already long-time available fix for some reason.
  We would love to see the fix integrated into an official Lombok version.
- Lombok's bug #3969 makes `import module lombok;` not working, because Lombok has to look into the source code to detect its own insertion and this part is not done yet for module imports.
  This becomes even worse if Lombok is imported only via transitive module dependencies lacking any explicit import in the source-code at all.
  Hence, always use traditional-style imports for Lombok (e.g. `import lombok.NonNull;`).