package ninja.javahacker.annotimpler.jdbc.stmt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.PackagePrivate;

import module java.base;
import module ninja.javahacker.annotimpler.convert;

/// A [ResultSet] wrapper that adds type-aware value retrieval, case-insensitive
/// column name lookup, and automatic mapping of result rows to Java `record` types.
///
/// All [ResultSet] methods are delegated to the underlying result set via Lombok
/// `@Delegate`. The additional methods provided by this class are:
///
/// - [#getTypedValue(int)] / [#getTypedValue(String)] — return the value at a column as the
///   most appropriate Java type for the JDBC type code.
/// - [#getTypedValue(int, Class)] / [#getTypedValue(String, Class)] — additionally convert
///   the value to a specific Java type via a [ConverterFactory].
/// - [#getMap()] and variants — collect all (or a subset of) columns into an unmodifiable map.
/// - [#getRecord(Class)] and variants — map one or more columns directly into a Java `record`.
///
/// Column name comparisons are always case-insensitive and locale-aware; the [Locale]
/// passed to the constructor governs locale-specific uppercasing (e.g. Turkish dotted-I).
@SuppressFBWarnings("EI_EXPOSE_REP2")
@PackagePrivate
final class InternalSmartResultSet implements SmartResultSet {

    /// The converter factory used to convert column values to target Java types.
    @NonNull
    private final ConverterFactory factory;

    /// The wrapped [ResultSet] to decorate.
    @NonNull
    @Delegate(types = ResultSet.class)
    private final ResultSet rs;

    /// The metada from the result set, used to retrieve metadata from columns.
    @NonNull
    private final ResultSetMetaData metaData;

    /// Caches data for each column.
    @NonNull
    private final ColumnMapping mappings;

    /// The locale used for case-insensitive column name matching.
    @NonNull
    private final Locale localizer;

    /// Creates a [SmartResultSet] wrapping the given [ResultSet] with the specified
    /// converter factory and locale.
    ///
    /// @param rs The [ResultSet] to wrap.
    /// @param factory The converter factory used to convert column values to target Java types.
    /// @param localizer The locale used for case-insensitive column name matching.
    /// @throws SQLException If a database access error occurs while reading metadata.
    public InternalSmartResultSet(@NonNull ResultSet rs, @NonNull ConverterFactory factory, @NonNull Locale localizer) throws SQLException {
        checkNotNull(rs); // Check recognized by lombok.
        checkNotNull(factory); // Check recognized by lombok.
        checkNotNull(localizer); // Check recognized by lombok.
        this.rs = rs;
        this.factory = factory;
        this.metaData = rs.getMetaData();
        this.localizer = localizer;
        this.mappings = new ColumnMapping(metaData, localizer);
    }

    /// Returns a brief string identifying this wrapper and the underlying result set.
    ///
    /// @return A string of the form `SmartResultSet[<underlying>]`.
    @NonNull
    @Override
    public String toString() {
        return SmartResultSet.class.getSimpleName() + "[" + rs + "]";
    }

    /// Builds and caches, from a [ResultSetMetaData], the mapping between (case-insensitive,
    /// locale-uppercased) column labels and their 1-based column indices.
    private static final class ColumnMapping {
        /// The upper-cased column label for each 1-based column index (0-based here), or
        /// [Optional#empty()] if the column was null-named, empty-named or duplicated.
        @NonNull
        private final List<Optional<String>> columnNames;

        /// Maps each distinct upper-cased column label to its 1-based column index.
        @NonNull
        private final Map<String, Integer> columnIndexes;

        /// The locale used to upper-case column labels for case-insensitive comparisons.
        @NonNull
        private final Locale localizer;

        /// Creates a [ColumnMapping] by reading all column labels from `rsmd`.
        ///
        /// @param rsmd The result set metadata to read column labels from; must not be `null`.
        /// @param localizer The locale used to upper-case column labels; must not be `null`.
        /// @throws SQLException If a database access error occurs while reading metadata.
        /// @throws IllegalArgumentException If `rsmd` or `localizer` is `null`.
        public ColumnMapping(@NonNull ResultSetMetaData rsmd, @NonNull Locale localizer) throws SQLException {
            checkNotNull(rsmd); // Check recognized by lombok.
            checkNotNull(localizer); // Check recognized by lombok.

            var count = rsmd.getColumnCount();
            var keys = new ArrayList<Optional<String>>(count);
            var idx = new HashMap<String, Integer>(count);

            for (int i = 1; i <= count; i++) {
                var columnName = rsmd.getColumnLabel(i);

                // A null column label should never happen in sane JDBC implementations, but we defend against it anyway.
                if (columnName == null) columnName = "";

                // Column names that are duplicated or that vary only by capitalization should not happen either.
                // Use localizer due to the Turkish/Azerbaijani dotted vs dotless I problem (e.g. "i".toUpperCase(TURKISH) = "İ").
                columnName = columnName.toUpperCase(localizer);

                /* Should never fail in sane JDBC implementations, which should not contain columns that are:
                   a) Null-named;
                   b) Empty-named;
                   c) Duplicated;
                   d) Varying only by capitalization;
                   e) Varying in name only due to the use of Turkish/Azerbaijani dotted vs dotless I.
                   When it fails due to any of those things happening, the field is simply ommited.
                */
                if (columnName.isEmpty() || idx.containsKey(columnName)) {
                    keys.add(Optional.empty());
                } else {
                    idx.put(columnName, i);
                    keys.add(Optional.of(columnName));
                }
            }

            this.columnNames = List.copyOf(keys);
            this.columnIndexes = Map.copyOf(idx);
            this.localizer = localizer;
        }

        /// Returns the total number of columns known by this mapping.
        ///
        /// @return The column count.
        public int getColumnCount() {
            return columnNames.size();
        }

        /// Returns the 1-based column index for the column named `columnName` (case-insensitive).
        ///
        /// @param columnName The column label to look up; must not be `null`.
        /// @return The 1-based column index.
        /// @throws IllegalArgumentException If `columnName` is `null` or there is no such column.
        public int indexOf(@NonNull String columnName) {
            checkNotNull(columnName); // Check recognized by lombok.
            var name = columnName.toUpperCase(localizer);
            var v = columnIndexes.get(name);
            if (v == null) {
                throw new IllegalArgumentException("There is no column \"" + columnName + "\".");
            }
            return v;
        }

        /// Returns the upper-cased label of the column at `columnIndex`, if it has a usable one.
        ///
        /// @param columnIndex The 1-based column index to look up.
        /// @return The upper-cased column label, or [Optional#empty()] if the column was
        ///         null-named, empty-named or a duplicate of another column's label.
        /// @throws IllegalArgumentException If `columnIndex` is out of range.
        @NonNull
        public Optional<String> labelOf(int columnIndex) {
            if (columnIndex < 1 || columnIndex > getColumnCount()) {
                throw new IllegalArgumentException("There is no column " + columnIndex + ".");
            }
            return columnNames.get(columnIndex - 1);
        }
    }

    /// {@inheritDoc}
    @Override
    public int indexOf(@NonNull String columnLabel) {
        return mappings.indexOf(columnLabel);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public Optional<String> labelOf(int columnIndex) {
        return mappings.labelOf(columnIndex);
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public <E> Optional<E> getTypedValueOpt(int columnIndex, @NonNull Class<E> target) throws SQLException {
        try {
            var raw = getTypedValue(columnIndex);
            return factory.getOf(target).fromObj(raw);
        } catch (ConvertionException | UnavailableConverterException e) {
            throw new SQLException(e);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public <R extends Record> R getRecord(
            @NonNull Class<R> k,
            @NonNull Function<String, String> remapper,
            @NonNull Map<String, Object> map)
            throws SQLException
    {
        try {
            var remappedMap = map.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(e -> remapper.apply(e.getKey()), Map.Entry::getValue));
            return factory.mapToRecord(remappedMap, k);
        } catch (ConvertionException
                | MagicFactory.CreationException
                | MagicFactory.CreatorSelectionException
                | UnavailableConverterException e)
        {
            throw new SQLException(e);
        }
    }

    /// {@inheritDoc}
    @NonNull
    @Override
    public <R extends Record> Function<String, String> defaultRemapper(@NonNull Class<R> k) {
        var components = k.getRecordComponents();
        var mapping = new HashMap<String, String>(components.length);
        for (var rc : components) {
            mapping.put(rc.getName().toUpperCase(localizer), rc.getName());
        }
        return key -> mapping.getOrDefault(key, key);
    }

    @Generated
    private static void checkNotNull(Object obj) {
        if (obj == null) throw new AssertionError();
    }
}