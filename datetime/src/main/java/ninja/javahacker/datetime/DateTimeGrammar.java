package ninja.javahacker.datetime;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Generated;
import lombok.NonNull;
import lombok.experimental.PackagePrivate;

import module java.base;

/// [DateTimeFormatter] is too much flexible about parsing and always ends up accepting something that it should not
/// without further validation with tools like regex, which also have their own problems, so we use this class as a custom parser instead.
///
/// @param dateSeparator What is used to separate date components. Might be dot (`.`), slash (`/`) or dash (`-`).
/// @param dateTimeSeparator What separates the date component from the time component.
///        Normally this is an space, but ISO 8601 uses `T` instead.
/// @param type Which order of the date components should be used.
/// @param acceptsZ Enables ISO 8601 special parsing behaviors if set.
@PackagePrivate
final record DateTimeGrammar(char dateSeparator, char dateTimeSeparator, @NonNull DateFormat type, boolean acceptsZ) {

    public DateTimeGrammar {
        checkNotNull(type); // Check recognized by lombok.
    }

    /// Specifies an ordering format for dates to be recognized by a [DateTimeGrammar] instance.
    public static enum DateFormat {
        /// Refers to `dd/MM/yyyy`, `dd-MM-yyyy` or `dd.MM.yyyy`. Year last, day first.
        DMY,

        /// Refers to `MM/dd/yyyy`, `MM-dd-yyyy` or `MM.dd.yyyy`. Year last, month first.
        MDY,

        /// Refers to `yyyy/MM/dd`, `yyyy-MM-dd` or `yyyy.MM.dd`. Year first, day last.
        YMD;
    }

    private record Match<E>(@NonNull E content, int position, int end) {
        public Match {
            checkNotNull(content); // Check recognized by lombok.
        }

        /// Returns a copy of this [Match] with the same [#position] and [#end], but with different content.
        ///
        /// @param <U> The type of the new content.
        /// @param newContent The new content to use.
        /// @return A copy of this [Match] with `newContent` in place of [#content].
        /// @throws IllegalArgumentException If `newContent` is `null`.
        @NonNull
        public <U> Match<U> withContent(@NonNull U newContent) {
            checkNotNull(newContent); // Check recognized by lombok.
            return new Match<>(newContent, position, end);
        }

        /// The length, in characters, of the matched span.
        ///
        /// @return `[#end] - [#position]`.
        public int size() {
            return end - position;
        }
    }

    private record TwoNumbers(int a, int b) {

        /// Widens this pair into a [ThreeNumbers] with [ThreeNumbers#c] set to `0` and [ThreeNumbers#reallyThree] set to `false`.
        ///
        /// @return A [ThreeNumbers] equivalent to this pair, flagged as not really having a third number.
        @NonNull
        public ThreeNumbers asThree() {
            return new ThreeNumbers(a, b, 0, false);
        }

        /// Widens this pair into a [ThreeNumbers] with an actual third number.
        ///
        /// @param c The third number.
        /// @return A [ThreeNumbers] equivalent to this pair plus `c`, flagged as really having a third number.
        @NonNull
        public ThreeNumbers withC(int c) {
            return new ThreeNumbers(a, b, c, true);
        }
    }

    private record ThreeNumbers(int a, int b, int c, boolean reallyThree) {

        /// Widens this triple into a [FourNumbers] with a fourth number.
        ///
        /// @param d The fourth number.
        /// @return A [FourNumbers] equivalent to this triple plus `d`.
        @NonNull
        public FourNumbers withD(int d) {
            return new FourNumbers(a, b, c, d);
        }

        /// Builds the [LocalDate] represented by [#a], [#b] and [#c], interpreting their meaning according to `type`.
        ///
        /// @param type Which of [#a], [#b] and [#c] is the day, the month and the year.
        /// @return The resulting [LocalDate].
        /// @throws IllegalArgumentException If `type` is `null`.
        @NonNull
        public LocalDate date(@NonNull DateFormat type) {
            checkNotNull(type); // Check recognized by lombok.
            return LocalDate.of(type == DateFormat.YMD ? a : c,
                    type == DateFormat.MDY ? a : b,
                    type == DateFormat.YMD ? c : type == DateFormat.MDY ? b : a
            );
        }
    }

    private record FourNumbers(int a, int b, int c, int d) {

        /// Builds the [LocalTime] represented by [#a] (hour), [#b] (minute), [#c] (second) and [#d] (nanosecond).
        ///
        /// @return The resulting [LocalTime].
        @NonNull
        public LocalTime time() {
            return LocalTime.of(a, b, c, d);
        }
    }

    private record Pieces(@NonNull Optional<LocalDate> date, @NonNull Optional<LocalTime> time, @NonNull Optional<ZoneOffset> zone) {
        public Pieces {
            checkNotNull(date); // Check recognized by lombok.
            checkNotNull(time); // Check recognized by lombok.
            checkNotNull(zone); // Check recognized by lombok.
            assertOneTrue(date.isPresent(), time.isPresent());
        }

        /// Combines [#date] and [#time] (defaulting the time to midnight when absent) into a [LocalDateTime].
        ///
        /// @return The resulting [LocalDateTime], wrapped in an [Optional].
        @NonNull
        public Optional<LocalDateTime> dateTime() {
            assertTrue(date.isPresent()); // Only called in a context where dateLike was true and then date was not empty.
            if (time.isEmpty()) return defaultTime().dateTime();
            return Optional.of(date.get().atTime(time.get()));
        }

        /// Combines [#date], [#time] and [#zone] (defaulting the time to midnight and the zone to UTC when absent)
        /// into an [OffsetDateTime].
        ///
        /// @return The resulting [OffsetDateTime], wrapped in an [Optional].
        @NonNull
        public Optional<OffsetDateTime> dateTimeZone() {
            assertTrue(date.isPresent()); // Only called in a context where dateLike was true and then date was not empty.
            if (time.isEmpty()) return defaultTime().dateTimeZone();
            if (zone.isEmpty()) return defaultZone().dateTimeZone();
            return Optional.of(date.get().atTime(time.get()).atOffset(zone.get()));
        }

        /// Combines [#time] and [#zone] (defaulting the zone to UTC when absent) into an [OffsetTime].
        ///
        /// @return The resulting [OffsetTime], wrapped in an [Optional].
        @NonNull
        public Optional<OffsetTime> timeZone() {
            assertTrue(time.isPresent()); // Only called in a context where dateLike was false and then time was not empty.
            if (zone.isEmpty()) return defaultZone().timeZone();
            return Optional.of(time.get().atOffset(zone.get()));
        }

        @NonNull
        private Pieces defaultZone() {
            return new Pieces(date, time, Optional.of(ZoneOffset.UTC));
        }

        @NonNull
        private Pieces defaultTime() {
            return new Pieces(date, Optional.of(LocalTime.MIDNIGHT), zone);
        }
    }

    /// Parses the input into a [LocalDate].
    ///
    /// @param input What should be parsed.
    /// @return The parsed [LocalDate].
    /// @throws DateTimeParseException If the `input` can't be parsed as a [LocalDate].
    @NonNull
    public LocalDate date(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        return wrap(input, "date", () -> new Parser(input, this).parseAll(true, Pieces::date));
    }

    /// Parses the input into a [LocalTime].
    ///
    /// @param input What should be parsed.
    /// @return The parsed [LocalTime].
    /// @throws DateTimeParseException If the `input` can't be parsed as a [LocalTime].
    @NonNull
    public LocalTime time(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        return wrap(input, "time", () -> new Parser(input, this).parseAll(false, Pieces::time));
    }

    /// Parses the input into a [LocalDateTime].
    ///
    /// @param input What should be parsed.
    /// @return The parsed [LocalDateTime].
    /// @throws DateTimeParseException If the `input` can't be parsed as a [LocalDateTime].
    @NonNull
    public LocalDateTime dateTime(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        return wrap(input, "date-time", () -> new Parser(input, this).parseAll(true, Pieces::dateTime));
    }

    /// Parses the input into an [OffsetDateTime].
    ///
    /// @param input What should be parsed.
    /// @return The parsed [OffsetDateTime].
    /// @throws DateTimeParseException If the `input` can't be parsed as an [OffsetDateTime].
    @NonNull
    public OffsetDateTime dateTimeZone(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        return wrap(input, "date-time-zone", () -> new Parser(input, this).parseAll(true, Pieces::dateTimeZone));
    }

    /// Parses the input into an [OffsetTime].
    ///
    /// @param input What should be parsed.
    /// @return The parsed [OffsetDate].
    /// @throws DateTimeParseException If the `input` can't be parsed as an [OffsetTime].
    @NonNull
    public OffsetTime timeZone(@NonNull String input) {
        checkNotNull(input); // Check recognized by lombok.
        return wrap(input, "time-zone", () -> new Parser(input, this).parseAll(false, Pieces::timeZone));
    }

    @NonNull
    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE") // It is on purpose.
    private <E> E wrap(@NonNull String input, @NonNull String name, @NonNull Supplier<Optional<E>> sup) {
        checkNotNull(sup); // Check recognized by lombok.
        checkNotNull(name); // Check recognized by lombok.
        checkNotNull(input); // Check recognized by lombok.
        try {
            return sup.get().orElseThrow(() -> new DateTimeException(""));
        } catch (DateTimeException e) {
            throw new DateTimeParseException("Bad " + name + " (" + this + "): [" + input + "].", input, 0);
        }
    }

    private record Parser(@NonNull String input, @NonNull DateTimeGrammar grammarDetails) {

        Parser {
            checkNotNull(input); // Check recognized by lombok.
            checkNotNull(grammarDetails); // Check recognized by lombok.
        }

        @NonNull
        private <E> IntFunction<Match<Optional<E>>> opt(@NonNull IntFunction<Optional<Match<E>>> func, boolean extraCondition) {
            checkNotNull(func); // Check recognized by lombok.
            if (!extraCondition) return position -> new Match<>(Optional.empty(), position, position);
            return position -> {
                var d = func.apply(position);
                if (d.isEmpty()) return new Match<>(Optional.empty(), position, position);
                var c = d.get();
                return c.withContent(Optional.of(c.content()));
            };
        }

        /// Parses the whole [#input] as a date, a time, or both (depending on `dateLike` and on what is present),
        /// with an optional timezone, and hands the parsed [Pieces] to `func` to build the final result.
        ///
        /// @param <E> The type of the parsed result.
        /// @param dateLike If `true`, a date component is mandatory and a time component is optional;
        ///        if `false`, a time component is mandatory and a date component is not allowed.
        /// @param func Builds the final result from the parsed [Pieces].
        /// @return The result produced by `func`, or [Optional#empty()] if [#input] does not match the expected grammar.
        /// @throws IllegalArgumentException If `func` is `null`.
        @NonNull
        public <E> Optional<E> parseAll(boolean dateLike, @NonNull Function<Pieces, Optional<E>> func) {
            checkNotNull(func); // Check recognized by lombok.
            var d = opt(this::date, true).apply(0);
            var dc = d.content();
            var s1 = opt(this::dtSep, dc.isPresent()).apply(d.end());
            var t = opt(this::time, true).apply(s1.end());
            var tc = t.content();
            var s2 = opt(this::tzSep, tc.isPresent()).apply(t.end());
            var z = opt(this::zone, true).apply(s2.end());
            var zc = z.content();
            var e = eof(z.end());
            if (e.isEmpty()) return Optional.empty();
            if (tc.isEmpty()) {
                if (dc.isEmpty() || zc.isPresent()) return Optional.empty();
            } else {
                if (dc.isPresent() && s1.content().isEmpty()) return Optional.empty();
                if (zc.isPresent() && s2.content().isEmpty()) return Optional.empty();
            }
            if (dateLike && dc.isEmpty()) return Optional.empty();
            if (!dateLike && tc.isEmpty()) return Optional.empty();
            return Optional.of(new Pieces(dc, tc, zc)).flatMap(func);
        }

        @NonNull
        private Optional<Match<Character>> dtSep(int position) {
            return singleChar(grammarDetails.dateTimeSeparator(), position);
        }

        @NonNull
        private Optional<Match<Character>> tzSep(int position) {
            return grammarDetails.acceptsZ()
                    ? Optional.of(new Match<>(' ', position, position))
                    : singleChar(grammarDetails.dateTimeSeparator(), position);
        }

        @NonNull
        private Optional<Match<LocalDate>> date(int position) {
            var odmy = threeNumbers(grammarDetails.type() == DateFormat.YMD ? 4 : 2,
                    2,
                    grammarDetails.type() == DateFormat.YMD ? 2 : 4,
                    grammarDetails.dateSeparator(),
                    position
            );
            if (odmy.isEmpty()) return Optional.empty();
            var dmy = odmy.get();
            var dmyc = dmy.content();

            var ld = dmyc.date(grammarDetails.type());
            return Optional.of(new Match<>(ld, position, dmy.end()));
        }

        @NonNull
        private Optional<Match<LocalTime>> time(int position) {
            var ohms = twoOrThreeNumbers(2, 2, 2, ':', position);
            if (ohms.isEmpty()) return Optional.empty();
            var a = ohms.get();

            var os1 = a.content().reallyThree() ? singleChar('.', a.end()) : Optional.<Match<Character>>empty();
            if (os1.isEmpty()) return Optional.of(new Match<>(a.content().withD(0).time(), position, a.end()));
            var s1 = os1.get();

            var ob = digits(1, 9, s1.end());
            if (ob.isEmpty()) return Optional.empty();
            var b = ob.get();

            for (var i = b.size(); i < 9; i++) {
                b = new Match<>(b.content() * 10, b.position(), b.end());
            }
            var d = b.content();

            var t = a.content().withD(d).time();
            return Optional.of(new Match<>(t, position, b.end()));
        }

        @NonNull
        private Optional<Match<ZoneOffset>> zone(int position) {
            var os1 = singleChar(c -> c == '+' || c == '-' || (grammarDetails.acceptsZ() && c == 'Z'), position);
            if (os1.isEmpty()) return Optional.empty();
            var s1 = os1.get();
            var s1c = s1.content();

            if (s1.content() == 'Z') return Optional.of(new Match<>(ZoneOffset.UTC, position, s1.end()));

            var ohms = twoOrThreeNumbers(2, 2, 2, ':', s1.end());
            if (ohms.isEmpty()) return Optional.empty();
            var a = ohms.get();
            var ac = a.content();

            var sg = s1c == '-' ? -1 : 1;
            var zn = ZoneOffset.ofHoursMinutesSeconds(sg * ac.a(), sg * ac.b(), sg * ac.c());
            return Optional.of(new Match<>(zn, position, a.end()));
        }

        @NonNull
        private Optional<Match<TwoNumbers>> twoNumbers(int ta, int tb, char separator, int position) {
            var oa = digits(ta, ta, position);
            if (oa.isEmpty()) return Optional.empty();
            var a = oa.get();

            var os1 = singleChar(separator, a.end());
            if (os1.isEmpty()) return Optional.empty();
            var s1 = os1.get();

            var ob = digits(tb, tb, s1.end());
            if (ob.isEmpty()) return Optional.empty();
            var b = ob.get();

            return Optional.of(new Match<>(new TwoNumbers(a.content(), b.content()), position, b.end()));
        }

        @NonNull
        private Optional<Match<ThreeNumbers>> threeNumbers(int ta, int tb, int tc, char separator, int position) {
            var oab = twoNumbers(ta, tb, separator, position);
            if (oab.isEmpty()) return Optional.empty();
            var ab = oab.get();

            var os2 = singleChar(separator, ab.end());
            if (os2.isEmpty()) return Optional.empty();
            var s2 = os2.get();

            var oc = digits(tc, tc, s2.end());
            if (oc.isEmpty()) return Optional.empty();
            var c = oc.get();

            return Optional.of(new Match<>(ab.content().withC(c.content()), position, c.end()));
        }

        @NonNull
        private Optional<Match<ThreeNumbers>> twoOrThreeNumbers(int ta, int tb, int tc, char separator, int position) {
            var oab = twoNumbers(ta, tb, separator, position);
            if (oab.isEmpty()) return Optional.empty();
            var ab = oab.get();

            var os2 = singleChar(separator, ab.end());
            if (os2.isEmpty()) return Optional.of(new Match<>(ab.content().asThree(), position, ab.end()));
            var s2 = os2.get();

            var oc = digits(tc, tc, s2.end());
            if (oc.isEmpty()) return Optional.empty();
            var c = oc.get();

            return Optional.of(new Match<>(ab.content().withC(c.content()), position, c.end()));
        }

        @NonNull
        private Optional<Match<Integer>> digits(int min, int max, int position) {
            var value = 0;
            var i = 0;
            for (; i < max; i++) {
                var d = digit(position + i);
                if (d.isEmpty()) break;
                value *= 10;
                value += d.get().content();
            }
            if (i < min) return Optional.empty();
            return Optional.of(new Match<>(value, position, position + i));
        }

        @NonNull
        private Optional<Match<Integer>> digit(int position) {
            for (var i = 0; i <= 9; i++) {
                var d = digit(i, position);
                if (d.isPresent()) return d;
            }
            return Optional.empty();
        }

        @NonNull
        private Optional<Match<Integer>> digit(int which, int position) {
            return anyChar(position).filter(m -> m.content() == '0' + which).map(m -> m.withContent(which));
        }

        @NonNull
        private Optional<Match<Character>> singleChar(char which, int position) {
            return anyChar(position).filter(m -> m.content() == which);
        }

        @NonNull
        private Optional<Match<Character>> singleChar(@NonNull Predicate<Character> tester, int position) {
            checkNotNull(tester); // Check recognized by lombok.
            return anyChar(position).filter(m -> tester.test(m.content()));
        }

        @NonNull
        private Optional<Match<Character>> anyChar(int position) {
            if (position >= input.length()) return Optional.empty();
            var matched = input.charAt(position);
            return Optional.of(new Match<>(matched, position, position + 1));
        }

        @NonNull
        private Optional<Match<Character>> eof(int position) {
            if (position == input.length()) return Optional.of(new Match<>('\0', position, position));
            return Optional.empty();
        }
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }

    @Generated
    private static void assertTrue(boolean what) {
        if (!what) throw new AssertionError();
    }

    @Generated
    private static void assertOneTrue(boolean a, boolean b) {
        if (!a && !b) throw new AssertionError();
    }
}
