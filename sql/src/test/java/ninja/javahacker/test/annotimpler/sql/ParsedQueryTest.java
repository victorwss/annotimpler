package ninja.javahacker.test.annotimpler.sql;

import ninja.javahacker.test.ForTests;
import org.junit.jupiter.api.function.Executable;

import module java.base;
import module ninja.javahacker.annotimpler.sql;
import module org.junit.jupiter.api;

public class ParsedQueryTest {

    private static DynamicTest n(String name, Executable ctx) {
        return DynamicTest.dynamicTest(name, ctx);
    }

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

    @TestFactory
    @SuppressWarnings("ObjectEqualsNull")
    public Stream<DynamicTest> testEqualsHashCodeToString() {
        var a = "SELECT * FROM foo WHERE :a = 'banana ? banana' AND :b = \"melon ? melon\"";
        var pq1 = ParsedQuery.parse(a);
        var pq2 = ParsedQuery.parse(a);
        var b = "SELECT * FROM foo WHERE :simple = ?";
        var pq3 = ParsedQuery.parse(b);
        return Stream.of(
                n("copy is equals", () -> Assertions.assertTrue(pq1.equals(pq2))),
                n("copy equals is simmetric", () -> Assertions.assertTrue(pq2.equals(pq1))),
                n("equals hashCode", () -> Assertions.assertEquals(pq1.hashCode(), pq2.hashCode())),
                n("equals toString", () -> Assertions.assertEquals(pq1.toString(), pq2.toString())),
                n("different is not equals", () -> Assertions.assertFalse(pq1.equals(pq3))),
                n("different toString", () -> Assertions.assertNotEquals(pq1.toString(), pq3.toString())),
                n("different hashCode", () -> Assertions.assertNotEquals(pq1.hashCode(), pq3.hashCode())),
                n("not equals null", () -> Assertions.assertFalse(pq1.equals(null))),
                n("not equals unrelated", () -> Assertions.assertFalse(pq1.equals("x"))),
                n("equals reflexive", () -> Assertions.assertTrue(pq1.equals(pq1)))
        );
    }

    @TestFactory
    @SuppressWarnings("ThrowableResultIgnored")
    public Stream<DynamicTest> testParamsUnmodifiable() {
        var a = "SELECT * FROM foo WHERE :a = 'banana ? banana' AND :b = \"melon ? melon\"";
        var pq1 = ParsedQuery.parse(a);
        var pq2 = ParsedQuery.parse(a);
        return Stream.of(
                n("a", () -> {
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> pq1.params().put("foo", List.of(5)));
                    Assertions.assertEquals(pq1, pq2);
                }),
                n("b", () -> {
                    Assertions.assertThrows(UnsupportedOperationException.class, () -> pq1.params().get("a").add(72));
                    Assertions.assertEquals(pq1, pq2);
                })
        );
    }

    @TestFactory
    @SuppressWarnings("null")
    public Stream<DynamicTest> testNulls() {
        return Stream.of(
                n("parse", () -> ForTests.testNull("original", () -> ParsedQuery.parse(null))),
                n("original-new", () -> ForTests.testNull("original", () -> new ParsedQuery(null, "x", Map.of(), 0, false, false, false))),
                n("original-parsed", () -> ForTests.testNull("parsed", () -> new ParsedQuery("x", null, Map.of(), 0, false, false, false))),
                n("original-params", () -> ForTests.testNull("params", () -> new ParsedQuery("x", "x", null, 0, false, false, false)))
        );
    }
}