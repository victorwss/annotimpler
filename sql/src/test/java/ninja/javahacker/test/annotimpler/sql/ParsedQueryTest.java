package ninja.javahacker.test.annotimpler.sql;

import ninja.javahacker.test.ForTests;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ParsedQueryTest {
    @Test
    public void testSimple() {
        var a = "SELECT * FROM foo WHERE :a = :b + :c";
        var b = "SELECT * FROM foo WHERE ? = ? + ?";
        var x = Map.of("a", List.of(1), "b", List.of(2), "c", List.of(3));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(3, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertFalse(pq.hasErrors())
        );
    }

    @Test
    public void testRepeated() {
        var a = "SELECT * FROM foo WHERE :c = :a * :a + :b * :b";
        var b = "SELECT * FROM foo WHERE ? = ? * ? + ? * ?";
        var x = Map.of("a", List.of(2, 3), "b", List.of(4, 5), "c", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(5, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertFalse(pq.hasErrors())
        );
    }

    @Test
    public void testNested() {
        var a = "SELECT * FROM foo WHERE :simple = :complex::foo::bar";
        var b = "SELECT * FROM foo WHERE ? = ?";
        var x = Map.of("simple", List.of(1), "complex::foo::bar", List.of(2));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(2, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertFalse(pq.hasErrors())
        );
    }

    @Test
    public void testBad1() {
        var a = "SELECT * FROM foo WHERE :simple = 'foo";
        var b = "SELECT * FROM foo WHERE ? = 'foo";
        var x = Map.of("simple", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(1, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertTrue(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testBad2() {
        var a = "SELECT * FROM foo WHERE :simple = \"foo";
        var b = "SELECT * FROM foo WHERE ? = \"foo";
        var x = Map.of("simple", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(1, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertTrue(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testBad3() {
        var a = "SELECT * FROM foo WHERE :simple = ?";
        var b = "SELECT * FROM foo WHERE ? = ?";
        var x = Map.of("simple", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(2, pq.size()),
                () -> Assertions.assertTrue(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testBad4() {
        var a = "SELECT * FROM foo WHERE :simple = :";
        var b = "SELECT * FROM foo WHERE ? = :";
        var x = Map.of("simple", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(1, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertTrue(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testBad5() {
        var a = "SELECT * FROM foo WHERE :simple = : AND 1 = 1";
        var b = "SELECT * FROM foo WHERE ? = : AND 1 = 1";
        var x = Map.of("simple", List.of(1));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(1, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertTrue(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testQuotes() {
        var a = "SELECT * FROM foo WHERE :a = 'banana ? :a :b::c : :: banana' AND :b = \"melon ? :a :b::c : :: melon\"";
        var b = "SELECT * FROM foo WHERE ? = 'banana ? :a :b::c : :: banana' AND ? = \"melon ? :a :b::c : :: melon\"";
        var x = Map.of("a", List.of(1), "b", List.of(2));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(2, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertFalse(pq.loneColons()),
                () -> Assertions.assertFalse(pq.hasErrors())
        );
    }

    @Test
    public void testNameBreak() {
        var a = "SELECT * FROM foo WHERE :a:b :c: :d:: :e::f = 1";
        var b = "SELECT * FROM foo WHERE ?? ?: ?:: ? = 1";
        var x = Map.of("a", List.of(1), "b", List.of(2), "c", List.of(3), "d", List.of(4), "e::f", List.of(5));
        var pq = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> Assertions.assertEquals(a, pq.original()),
                () -> Assertions.assertEquals(b, pq.parsed()),
                () -> Assertions.assertEquals(x, pq.params()),
                () -> Assertions.assertEquals(5, pq.size()),
                () -> Assertions.assertFalse(pq.unnamedParameters()),
                () -> Assertions.assertFalse(pq.unclosedQuotes()),
                () -> Assertions.assertTrue(pq.loneColons()),
                () -> Assertions.assertTrue(pq.hasErrors())
        );
    }

    @Test
    public void testEqualsHashCodeToString() {
        var a = "SELECT * FROM foo WHERE :a = 'banana ? banana' AND :b = \"melon ? melon\"";
        var pq1 = ParsedQuery.parse(a);
        var pq2 = ParsedQuery.parse(a);
        var b = "SELECT * FROM foo WHERE :simple = ?";
        var pq3 = ParsedQuery.parse(b);
        Assertions.assertAll(
                () -> Assertions.assertEquals(pq1, pq2),
                () -> Assertions.assertEquals(pq1.hashCode(), pq2.hashCode()),
                () -> Assertions.assertEquals(pq1.toString(), pq2.toString()),
                () -> Assertions.assertNotEquals(pq1, pq3),
                () -> Assertions.assertNotEquals(pq1.toString(), pq3.toString()),
                () -> Assertions.assertFalse(pq1.equals(null)),
                () -> Assertions.assertTrue(pq1.equals(pq1))
        );
    }

    @Test
    public void testParamsUnmodifiable() {
        var a = "SELECT * FROM foo WHERE :a = 'banana ? banana' AND :b = \"melon ? melon\"";
        var pq1 = ParsedQuery.parse(a);
        var pq2 = ParsedQuery.parse(a);
        Assertions.assertAll(
                () -> {
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> pq1.params().put("foo", List.of(5)));
                    Assertions.assertEquals(pq1, pq2);
                },
                () -> {
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> pq1.params().get("a").add(72));
                    Assertions.assertEquals(pq1, pq2);
                }
        );
    }

    @Test
    public void testNulls() {
        Assertions.assertAll(
                () -> ForTests.testNull("original", () -> ParsedQuery.parse(null), "parse"),
                () -> ForTests.testNull("original", () -> new ParsedQuery(null, "x", Map.of(), 0, false, false, false), "original-new"),
                () -> ForTests.testNull("parsed", () -> new ParsedQuery("x", null, Map.of(), 0, false, false, false), "original-parsed"),
                () -> ForTests.testNull("params", () -> new ParsedQuery("x", "x", null, 0, false, false, false), "original-params")
        );
    }
}