package ninja.javahacker.test.limited;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;
import module ninja.javahacker.annotimpler.sql;

@DisplayName("LimitedReader Tests")
@SuppressWarnings({"unused", "NestedAssignment", "ThrowableResultIgnored"})
public class LimitedReaderTest {

    private static final String TEST_DATA = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit.
            Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
            """;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid parameters.")
        void shouldCreateInstanceWithValidParameters() {
            var wrapped = new StringReader(TEST_DATA);
            var limited = new LimitedReader(wrapped, 50);
            Assertions.assertAll(
                    () -> Assertions.assertEquals(50, limited.getMaxSize()),
                    () -> Assertions.assertEquals(50, limited.getRemaining()),
                    () -> Assertions.assertEquals(0, limited.getPosition()),
                    () -> Assertions.assertFalse(limited.isMarkSet()),
                    () -> Assertions.assertFalse(limited.isClosed())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when wrapped reader is null.")
        void shouldThrowNullPointerExceptionWhenWrappedIsNull() {
            ForTests.testNull("wrapped", () -> new LimitedReader(null, 50));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when maxChars is negative.")
        void shouldThrowIllegalArgumentExceptionWhenMaxCharsIsNegative() {
            var wrapped = new StringReader(TEST_DATA);
            Assertions.assertThrows(IllegalArgumentException.class, () -> new LimitedReader(wrapped, -1));
        }

        @Test
        @DisplayName("Should allow maxChars = 0")
        void shouldAllowMaxCharsZero() {
            var wrapped = new StringReader(TEST_DATA);
            Assertions.assertDoesNotThrow(() -> new LimitedReader(wrapped, 0));
        }
    }

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationsTests {

        @Test
        @DisplayName("Should read single character correctly within limit.")
        void shouldReadSingleCharacterWithinLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var firstChar = limited.read();
            Assertions.assertEquals('L', firstChar);
            Assertions.assertEquals(1, limited.getPosition());
            Assertions.assertEquals(9, limited.getRemaining());
        }

        @Test
        @DisplayName("Should return -1 when limit is reached.")
        void shouldReturnMinusOneWhenLimitReached() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 5);
            var buffer = new char[10];
            var read = limited.read(buffer);
            Assertions.assertEquals(5, read);

            var nextChar = limited.read();
            Assertions.assertEquals(-1, nextChar);
            Assertions.assertEquals(5, limited.getPosition());
            Assertions.assertEquals(0, limited.getRemaining());
        }

        @Test
        @DisplayName("Should read characters correctly within limit.")
        void shouldReadCharactersWithinLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 20);
            var buffer = new char[15];
            var read = limited.read(buffer);

            Assertions.assertEquals(15, read);
            Assertions.assertEquals(15, limited.getPosition());
            Assertions.assertEquals(5, limited.getRemaining());

            // Verify content.
            var expected = TEST_DATA.substring(0, 15);
            Assertions.assertEquals(expected, new String(buffer, 0, read));
        }

        @Test
        @DisplayName("Should not read more than limit.")
        void shouldNotReadMoreThanLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var buffer = new char[20];
            var read = limited.read(buffer);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, limited.getPosition());

            // Only first 10 characters should be read.
            var expected = TEST_DATA.substring(0, 10);
            Assertions.assertEquals(expected, new String(buffer, 0, read));
        }

        @Test
        @DisplayName("Should handle read with offset correctly.")
        void shouldHandleReadWithOffsetCorrectly() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 15);
            var buffer = new char[20];
            var read = limited.read(buffer, 5, 10);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, limited.getPosition());

            // Verify data was placed at correct offset.
            var expected = TEST_DATA.substring(0, 10);
            Assertions.assertEquals(expected, new String(buffer, 5, read));
        }

        @ParameterizedTest
        @DisplayName("Should read correctly with different buffer sizes.")
        @ValueSource(ints = {1, 5, 10, 20, 50})
        void shouldReadCorrectlyWithDifferentBufferSizes(int bufferSize) throws IOException {
            var limit = 30;
            var limited = new LimitedReader(new StringReader(TEST_DATA), limit);
            var buffer = new char[bufferSize];
            var totalRead = 0;
            int charsRead;

            while ((charsRead = limited.read(buffer)) != -1) {
                totalRead += charsRead;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);
        }
    }

    @Nested
    @DisplayName("Skip Operations Tests")
    class SkipOperationsTests {

        @Test
        @DisplayName("Should skip characters within limit.")
        void shouldSkipCharactersWithinLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 20);
            var skipped = limited.skip(10);

            Assertions.assertEquals(10, skipped);
            Assertions.assertEquals(10, limited.getPosition());
            Assertions.assertEquals(10, limited.getRemaining());

            // Verify position by reading next character.
            var nextChar = limited.read();
            Assertions.assertEquals(TEST_DATA.charAt(10), nextChar);
        }

        @Test
        @DisplayName("Should not skip more than limit.")
        void shouldNotSkipMoreThanLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 15);
            var skipped = limited.skip(20);

            Assertions.assertEquals(15, skipped);
            Assertions.assertEquals(15, limited.getPosition());
            Assertions.assertEquals(0, limited.getRemaining());
        }

        @Test
        @DisplayName("Should skip zero when n is zero.")
        void shouldSkipZeroWhenNIsZero() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var skipped = limited.skip(0);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(0, limited.getPosition());
        }

        @Test
        @DisplayName("Should skip zero when limit is reached.")
        void shouldSkipZeroWhenLimitReached() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 5);
            limited.skip(5);
            var skipped = limited.skip(10);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(5, limited.getPosition());
        }

        @Test
        @DisplayName("Should skip negative amount.")
        void shouldSkipNegativeAmount() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var skipped = limited.skip(-5);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(0, limited.getPosition());
        }
    }

    @Nested
    @DisplayName("Ready Operations Tests")
    class ReadyOperationsTests {

        @Test
        @DisplayName("Should return true when data is available within limit.")
        void shouldReturnTrueWhenDataAvailableWithinLimit() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 30);
            Assertions.assertTrue(limited.ready());
        }

        @Test
        @DisplayName("Should return true when limit is reached.")
        void shouldReturnTrueWhenLimitReached() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            limited.read(new char[10]);
            Assertions.assertTrue(limited.ready()); // Deepseek used assertFalse.
        }

        @Test
        @DisplayName("Should be ready after reading.")
        void shouldBeReadyAfterReading() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 20);
            Assertions.assertTrue(limited.ready());

            limited.read(new char[5]);
            Assertions.assertTrue(limited.ready());

            limited.read(new char[10]);
            Assertions.assertTrue(limited.ready());
        }

        @Test
        @DisplayName("Should return true for empty data.")
        void shouldReturnTrueForEmptyData() throws IOException {
            var limited = new LimitedReader(new StringReader(""), 10);
            Assertions.assertTrue(limited.ready()); // Deepseek used assertFalse.
            Assertions.assertEquals(-1, limited.read());
        }
    }

    @Nested
    @DisplayName("Mark/Reset Tests")
    class MarkResetTests {

        @Test
        @DisplayName("Should support mark when wrapped reader supports it.")
        void shouldSupportMarkWhenWrappedReaderSupportsIt() throws IOException {
            var wrapped = new StringReader(TEST_DATA);
            var limited = new LimitedReader(wrapped, 50);
            Assertions.assertTrue(limited.markSupported());
        }

        @Test
        @DisplayName("Should not support mark when wrapped reader doesn't support it.")
        void shouldNotSupportMarkWhenWrappedReaderDoesntSupportIt() throws IOException {
            var wrapped = new AssertionReader(TEST_DATA, 50, false);
            var limited = new LimitedReader(wrapped, 50);
            Assertions.assertFalse(limited.markSupported());
        }

        @Test
        @DisplayName("Should fail to mark when wrapped reader doesn't support it.")
        void shouldFailToMarkWhenWrappedDoesntSupportIt() throws IOException {
            var wrapped = new AssertionReader(TEST_DATA, 50, false);
            var limited = new LimitedReader(wrapped, 50);
            Assertions.assertThrows(IOException.class, () -> limited.mark(20));
            Assertions.assertFalse(limited.markSupported());
            Assertions.assertFalse(limited.isMarkSet());
        }

        @Test
        @DisplayName("Should mark and reset correctly.")
        void shouldMarkAndResetCorrectly() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 50);

            // Read first 10 characters.
            var firstRead = new char[10];
            limited.read(firstRead);
            Assertions.assertEquals(10, limited.getPosition());

            // Mark position.
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(100);
            Assertions.assertTrue(limited.isMarkSet());
            var markedPosition = limited.getPosition();

            // Read next 15 characters.
            var secondRead = new char[15];
            limited.read(secondRead);
            Assertions.assertEquals(25, limited.getPosition());

            // Reset to mark.
            limited.reset();
            Assertions.assertEquals(markedPosition, limited.getPosition());

            // Read again from mark position.
            var resetRead = new char[15];
            var read = limited.read(resetRead);
            Assertions.assertEquals(15, read);
            Assertions.assertEquals(25, limited.getPosition());

            // Verify data matches.
            Assertions.assertArrayEquals(secondRead, resetRead);
        }

        @Test
        @DisplayName("Should allow reading after reset.")
        void shouldAllowReadingAfterReset() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 50);

            // Read and mark.
            limited.read(new char[5]);
            limited.mark(100);
            limited.read(new char[10]);
            limited.reset();

            // Read after reset.
            var buffer = new char[20];
            var read = limited.read(buffer);

            Assertions.assertEquals(20, read);
            Assertions.assertEquals(25, limited.getPosition());
        }

        @Test
        @DisplayName("Should throw IOException when reset without mark.")
        void shouldThrowIOExceptionWhenResetWithoutMark() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 50);
            Assertions.assertThrows(IOException.class, limited::reset);
        }

        @Test
        @DisplayName("Should allow multiple marks and resets.")
        void shouldAllowMultipleMarksAndResets() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 50);

            // First mark.
            limited.read(new char[5]);
            limited.mark(100);
            var firstMark = limited.getPosition();

            limited.read(new char[10]);
            limited.reset();
            Assertions.assertEquals(firstMark, limited.getPosition());

            // Second mark.
            limited.read(new char[3]);
            limited.mark(100);
            var secondMark = limited.getPosition();

            limited.read(new char[7]);
            limited.reset();
            Assertions.assertEquals(secondMark, limited.getPosition());
        }

        @Test
        @DisplayName("Should handle mark with readlimit parameter.")
        void shouldHandleMarkWithReadlimitParameter() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 50);
            limited.read(new char[5]);
            limited.mark(20); // Allow reading up to 20 chars before reset.
            limited.read(new char[15]);

            // Reset should work since we read within limit.
            Assertions.assertDoesNotThrow(limited::reset);

            // After reset, we should be able to read again.
            var buffer = new char[10];
            var read = limited.read(buffer);
            Assertions.assertEquals(10, read);
        }
    }

    @Nested
    @DisplayName("Close Operations Tests")
    class CloseOperationsTests {

        @Test
        @DisplayName("Should close wrapped reader.")
        void shouldCloseWrappedReader() throws IOException {
            var wrapped = new AssertionReader(TEST_DATA, 50, true);
            var limited = new LimitedReader(wrapped, 50);

            limited.close();
            Assertions.assertTrue(limited.isClosed());
            Assertions.assertTrue(wrapped.isClosed());

            // Trying to do anything after close should throw exception.
            Assertions.assertAll(
                    () -> Assertions.assertThrows(IOException.class, () -> limited.read()),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.read(new char[10])),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.read(new char[10], 0, 5)),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.skip(5)),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.mark(50)),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.reset()),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.getMaxSize()),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.getPosition()),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.getRemaining()),
                    () -> Assertions.assertThrows(IOException.class, () -> limited.isMarkSet())
            );
        }

        @Test
        @DisplayName("Should be idempotent when closing multiple times.")
        void shouldBeIdempotentWhenClosingMultipleTimes() throws IOException {
            var wrapped = new StringReader(TEST_DATA);
            var limited = new LimitedReader(wrapped, 50);

            limited.close();
            Assertions.assertDoesNotThrow(limited::close);
        }

        @Test
        @DisplayName("Should throw IOException when reading after close.")
        void shouldThrowIOExceptionWhenReadingAfterClose() throws IOException {
            var wrapped = new StringReader(TEST_DATA);
            var limited = new LimitedReader(wrapped, 50);

            limited.close();

            Assertions.assertThrows(IOException.class, () -> limited.read());
            Assertions.assertThrows(IOException.class, () -> limited.read(new char[10]));
        }

        @Test
        @DisplayName("Should work in try-with-resources.")
        void shouldWorkInTryWithResources() throws IOException {
            var wrapped = new AssertionReader(TEST_DATA, 50, false);
            LimitedReader whatWas;
            try (var limited = new LimitedReader(wrapped, 50)) {
                whatWas = limited;
                var buffer = new char[5];
                limited.read(buffer);
                for (var k = 0; k < 5; k++) {
                    Assertions.assertEquals(TEST_DATA.charAt(k), buffer[k]);
                }
            }
            Assertions.assertTrue(whatWas.isClosed());
            Assertions.assertTrue(wrapped.isClosed());
            Assertions.assertThrows(IOException.class, () -> whatWas.read());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero max characters correctly.")
        void shouldHandleZeroMaxCharactersCorrectly() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 0);
            Assertions.assertTrue(limited.ready());
            Assertions.assertEquals(-1, limited.read());
            Assertions.assertEquals(0, limited.skip(10));

            var buffer = new char[10];
            Assertions.assertEquals(-1, limited.read(buffer));
            Assertions.assertTrue(limited.ready());
        }

        @Test
        @DisplayName("Should handle reading exactly limit.")
        void shouldHandleReadingExactlyLimit() throws IOException {
            var limit = 25;
            var limited = new LimitedReader(new StringReader(TEST_DATA), limit);
            var buffer = new char[limit];
            var read = limited.read(buffer);

            Assertions.assertEquals(limit, read);
            Assertions.assertEquals(limit, limited.getPosition());
            Assertions.assertEquals(0, limited.getRemaining());
            Assertions.assertEquals(-1, limited.read());
        }

        @ParameterizedTest
        @DisplayName("Should handle partial reads correctly.")
        @CsvSource({
            "10, 5",
            "10, 10",
            "10, 15",
            "25, 30"
        })
        void shouldHandlePartialReadsCorrectly(int limit, int readSize) throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), limit);
            var buffer = new char[readSize];
            var read = limited.read(buffer);

            var expectedRead = Math.min(limit, readSize);
            Assertions.assertEquals(expectedRead, read);
            Assertions.assertEquals(expectedRead, limited.getPosition());
        }

        @Test
        @DisplayName("Should work with empty reader.")
        void shouldWorkWithEmptyReader() throws IOException {
            var limited = new LimitedReader(new StringReader(""), 10);
            Assertions.assertEquals(-1, limited.read());
            Assertions.assertEquals(0, limited.skip(5));
            Assertions.assertTrue(limited.ready()); // Deepseek used assertFalse
        }

        @Test
        @DisplayName("Should handle null buffer in read.")
        void shouldHandleNullBufferInRead() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            Assertions.assertThrows(NullPointerException.class, () -> limited.read(null, 0, 10));
        }

        @Test
        @DisplayName("Should handle invalid offset and length.")
        void shouldHandleInvalidOffsetAndLength() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var buffer = new char[10];

            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, -1, 5));
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, 0, -1));
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, 8, 5));
        }

        @Test
        @DisplayName("Should handle reading zero length buffer.")
        void shouldHandleReadingZeroLengthBuffer() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 10);
            var buffer = new char[10];
            var read = limited.read(buffer, 0, 0);

            Assertions.assertEquals(0, read);
            Assertions.assertEquals(0, limited.getPosition());
        }

        @Test
        @DisplayName("Should handle reading after limit with mark/reset.")
        void shouldHandleReadingAfterLimitWithMarkReset() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 20);

            // Read up to limit.
            limited.read(new char[20]);

            // Mark at limit.
            limited.mark(100);

            // Try to read more - should return -1.
            Assertions.assertEquals(-1, limited.read());

            // Reset should go back to limit.
            limited.reset();
            Assertions.assertEquals(20, limited.getPosition());

            // Still at limit, so reading returns -1.
            Assertions.assertEquals(-1, limited.read());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with BufferedReader.")
        void shouldWorkWithBufferedReader() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 30);
            var buffered = new BufferedReader(limited);
            var line = new char[50];
            var out = buffered.read(line);
            Assertions.assertEquals(30, out);
        }

        @Test
        @DisplayName("Should work with BufferedReader lines.")
        void shouldWorkWithBufferedReaderLines() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 30);
            var buffered = new BufferedReader(limited);
            var line = buffered.readLine();
            Assertions.assertNotNull(line);
            Assertions.assertEquals(30, line.length());
        }

        @Test
        @DisplayName("Should maintain character count correctly with complex operations.")
        void shouldMaintainCharacterCountCorrectlyWithComplexOperations() throws IOException {
            var limited = new LimitedReader(new StringReader(TEST_DATA), 40);

            // Mix of different operations.
            limited.skip(5);
            Assertions.assertEquals(5, limited.getPosition());

            var buffer1 = new char[10];
            limited.read(buffer1);
            Assertions.assertEquals(15, limited.getPosition());

            limited.mark(100);
            var buffer2 = new char[10];
            limited.read(buffer2);
            Assertions.assertEquals(25, limited.getPosition());

            limited.reset();
            Assertions.assertEquals(15, limited.getPosition());

            var buffer3 = new char[15];
            var read = limited.read(buffer3);
            Assertions.assertEquals(15, read);
            Assertions.assertEquals(30, limited.getPosition());
        }
    }

    @Nested
    @DisplayName("Tests with AssertionReader - Verifying No Excess Reads")
    class AssertionVerificationTests {

        @Test
        @DisplayName("Should NOT cause AssertionError when reading exactly up to limit.")
        void shouldNotCauseAssertionErrorWhenReadingExactlyLimit() throws IOException {
            var limit = 15;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[limit];
            var read = limited.read(buffer);

            Assertions.assertEquals(limit, read);
            Assertions.assertEquals(limit, asserting.getPosition());
            Assertions.assertEquals(TEST_DATA.substring(0, 15), new String(buffer));
            // No assertion error should be thrown.
        }

        @Test
        @DisplayName("Should NOT cause AssertionError when reading in chunks up to limit.")
        void shouldNotCauseAssertionErrorWhenReadingInChunksUpToLimit() throws IOException {
            var limit = 25;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);
            var buffer = new char[7];

            for (var i = 1; i <= 3; i++) {
                var reading = limited.read(buffer);
                Assertions.assertEquals(7, reading);
                Assertions.assertEquals(i * 7, asserting.getPosition());
            }

            var lastRead = limited.read(buffer);
            Assertions.assertEquals(4, lastRead);
            Assertions.assertEquals(25, asserting.getPosition());

            var oneMoreRead = limited.read(buffer);
            Assertions.assertEquals(-1, oneMoreRead);
            Assertions.assertEquals(25, asserting.getPosition());
        }

        @Test
        @DisplayName("Should NOT cause AssertionError when reading one char at a time.")
        void shouldNotCauseAssertionErrorWhenReadingOneCharAtATime() throws IOException {
            var limit = 20;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            for (var i = 0; i < limit; i++) {
                var c = limited.read();
                Assertions.assertEquals(TEST_DATA.charAt(i), c);
            }

            var cl = limited.read();
            Assertions.assertEquals(-1, cl);
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should NOT cause AssertionError when skip is used.")
        void shouldNotCauseAssertionErrorWhenSkipIsUsed() throws IOException {
            var limit = 25;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var skipped1 = limited.skip(10);
            Assertions.assertEquals(10, skipped1);

            var buffer = new char[10];
            var read = limited.read(buffer);
            Assertions.assertEquals(10, read);

            var skipped2 = limited.skip(10);
            Assertions.assertEquals(5, skipped2);

            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should handle mark/reset without causing excess reads.")
        void shouldHandleMarkResetWithoutExcessReads() throws IOException {
            var limit = 40;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            var firstRead = new char[15];
            limited.read(firstRead);
            limited.mark(100);

            var secondRead = new char[15];
            limited.read(secondRead);

            limited.reset();
            var resetRead = new char[15];
            var resetChars = limited.read(resetRead);

            Assertions.assertEquals(15, resetChars);
            Assertions.assertEquals(30, asserting.getPosition());
        }

        @Test
        @DisplayName("Should verify that wrapped reader is never read beyond limit.")
        void shouldVerifyWrappedReaderNeverReadBeyondLimit() throws IOException {
            var limit = 30;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            // Try to read more than limit in various ways.
            var largeBuffer = new char[50];
            var read1 = limited.read(largeBuffer);
            Assertions.assertEquals(limit, read1);

            // This should NOT cause AssertionError because the limited reader
            // should prevent the wrapped reader from being called again.
            var read2 = limited.read();
            Assertions.assertEquals(-1, read2);

            // The wrapped reader should still have only read 'limit' chars.
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @ParameterizedTest
        @DisplayName("Should verify read operations never request more than available limit.")
        @ValueSource(ints = {1, 10, 25, 50})
        void shouldVerifyReadOperationsNeverRequestMoreThanAvailableLimit(int limit) throws IOException {
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[limit + 20];
            var totalRead = 0;
            int charsRead;

            while ((charsRead = limited.read(buffer)) != -1) {
                totalRead += charsRead;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);

            // Verify each read operation from the wrapped reader.
            var operations = asserting.getReadOperations();

            // Sum of all actual chars read should equal limit.
            var totalRequested = operations
                    .stream()
                    .filter(op -> op.type == AssertionReader.OperationType.READ)
                    .mapToLong(op -> op.actualLength)
                    .sum();

            Assertions.assertEquals(limit, totalRequested);

            // No operation should have requested more than the remaining limit.
            var runningTotal = 0L;
            for (var op : operations) {
                if (op.type == AssertionReader.OperationType.READ) {
                    Assertions.assertTrue(
                            op.actualLength <= (limit - runningTotal),
                            "Operation requested " + op.actualLength + " chars but only " + (limit - runningTotal) + " remaining"
                    );
                    runningTotal += op.actualLength;
                }
            }
        }

        @Test
        @DisplayName("Should handle mark/reset correctly when near limit.")
        void shouldHandleMarkResetCorrectlyWhenNearLimit() throws IOException {
            var limit = 20;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Read 15 chars.
            var firstRead = new char[15];
            var read1 = limited.read(firstRead);
            Assertions.assertEquals(15, read1);

            // Mark position.
            limited.mark(100);
            var markedPosition = limited.getPosition();
            Assertions.assertEquals(15, markedPosition);

            // Try to read 10 more chars (only 5 available).
            var secondRead = new char[10];
            var read2 = limited.read(secondRead);
            Assertions.assertEquals(5, read2);
            Assertions.assertEquals(20, limited.getPosition());

            // Reset should go back to marked position.
            limited.reset();
            Assertions.assertEquals(15, limited.getPosition());

            // Read again - should read the same 5 chars.
            var resetRead = new char[10];
            var read3 = limited.read(resetRead);
            Assertions.assertEquals(5, read3);
            Assertions.assertEquals(20, limited.getPosition());

            // Verify the content matches.
            Assertions.assertArrayEquals(
                    Arrays.copyOfRange(secondRead, 0, 5),
                    Arrays.copyOfRange(resetRead, 0, 5)
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases - Boundary Conditions")
    class BoundaryConditionsTests {

        @Test
        @DisplayName("Should handle read request exactly at limit boundary.")
        void shouldHandleReadRequestExactlyAtLimitBoundary() throws IOException {
            var limit = 10;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[10];
            var read = limited.read(buffer, 0, 10);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, asserting.getPosition());

            // Verify the wrapped reader received exactly one read of 10 chars.
            var operations = asserting.getReadOperations();
            Assertions.assertEquals(1, operations.size());
            Assertions.assertEquals(10, operations.get(0).actualLength);
            Assertions.assertEquals(AssertionReader.OperationType.READ, operations.get(0).type);

            // Next read should not cause any additional read from wrapped reader.
            asserting.resetReadOperations();
            var nextRead = limited.read();
            Assertions.assertEquals(-1, nextRead);
            Assertions.assertTrue(asserting.getReadOperations().isEmpty());
        }

        @ParameterizedTest
        @DisplayName("Should handle various combinations of read and skip without exceeding limit.")
        @CsvSource({
            "30, 10, 5, 15",
            "25, 5, 10, 10",
            "20, 8, 4, 8",
            "15, 3, 7, 5"
        })
        void shouldHandleReadSkipCombinationsWithoutExceedingLimit(int limit, int initialRead, int skip, int finalRead) throws IOException {
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[Math.max(initialRead, finalRead)];

            var read1 = limited.read(buffer, 0, initialRead);
            Assertions.assertEquals(Math.min(initialRead, limit), read1);

            var skipped = limited.skip(skip);
            var expectedSkip = Math.min(skip, limit - read1);
            Assertions.assertEquals(expectedSkip, skipped);

            var read2 = limited.read(buffer, 0, finalRead);
            var expectedFinalRead = Math.min(finalRead, limit - read1 - expectedSkip);
            Assertions.assertEquals(expectedFinalRead, read2);

            var totalRead = read1 + skipped + read2;
            Assertions.assertEquals(limit, totalRead);
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should handle zero-length reads correctly.")
        void shouldHandleZeroLengthReadsCorrectly() throws IOException {
            var limit = 10;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[10];

            var zeroRead = limited.read(buffer, 0, 0);
            Assertions.assertEquals(0, zeroRead);
            Assertions.assertEquals(0, asserting.getPosition());
            Assertions.assertTrue(asserting.getReadOperations().isEmpty());

            var normalRead = limited.read(buffer);
            Assertions.assertEquals(limit, normalRead);
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should handle ready() correctly at boundaries.")
        void shouldHandleReadyCorrectlyAtBoundaries() throws IOException {
            var limit = 15;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            Assertions.assertTrue(limited.ready());
            limited.read(new char[limit]);
            Assertions.assertTrue(limited.ready());
            Assertions.assertTrue(limited.ready());
        }
    }

    @Nested
    @DisplayName("Stress Tests")
    class StressTests {

        @Test
        @DisplayName("Should handle many small reads without exceeding limit.")
        void shouldHandleManySmallReadsWithoutExceedingLimit() throws IOException {
            var limit = 100;
            var smallReadSize = 3;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[smallReadSize];
            var totalRead = 0;
            var readCount = 0;
            int charsRead;

            while ((charsRead = limited.read(buffer)) != -1) {
                totalRead += charsRead;
                readCount++;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);
            Assertions.assertEquals(limit, asserting.getPosition());

            // Verify that the sum of all reads equals the limit.
            var sumOfReads = asserting
                    .getReadOperations()
                    .stream()
                    .filter(op -> op.type == AssertionReader.OperationType.READ)
                    .mapToLong(op -> op.actualLength)
                    .sum();

            Assertions.assertEquals(limit, sumOfReads);
        }

        @Test
        @DisplayName("Should handle alternating read and skip operations.")
        void shouldHandleAlternatingReadAndSkipOperations() throws IOException {
            var limit = 100;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);
            var buffer = new char[7];

            for (var i = 0; i < 8; i++) {
                var read = limited.read(buffer);
                Assertions.assertEquals(7, read);
                Assertions.assertEquals(12 * i + 7, asserting.getPosition());

                var skipped = limited.skip(5);
                Assertions.assertEquals(5L, skipped);
                Assertions.assertEquals(12 * i + 12, asserting.getPosition());
            }

            var readLast = limited.read(buffer);
            Assertions.assertEquals(4, readLast);
            Assertions.assertEquals(100, asserting.getPosition());

            var skipLast = limited.skip(5);
            Assertions.assertEquals(0L, skipLast);
            Assertions.assertEquals(100, asserting.getPosition());

            var readPass = limited.read(buffer);
            Assertions.assertEquals(-1, readPass);
            Assertions.assertEquals(100, asserting.getPosition());
        }

        @Test
        @DisplayName("Should handle rapid mark/reset cycles without excess reads.")
        void shouldHandleRapidMarkResetCyclesWithoutExcessReads() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            var buffer1 = new char[5];
            var buffer2 = new char[5];

            for (var i = 0; i < 10; i++) {
                limited.mark(20);
                var read = limited.read(buffer1);
                Assertions.assertEquals(5, read);
                Assertions.assertEquals(i * 5 + 5, limited.getPosition());
                for (var k = 0; k < 5; k++) {
                    Assertions.assertEquals(buffer1[k], TEST_DATA.charAt(i * 5 + k));
                }

                limited.reset();

                var readAfterReset = limited.read(buffer2);
                Assertions.assertEquals(5, readAfterReset);
                Assertions.assertEquals(i * 5 + 5, limited.getPosition());
                Assertions.assertArrayEquals(buffer1, buffer2);
            }

            var endRead = limited.read(buffer1);
            Assertions.assertEquals(-1, endRead);
            Assertions.assertEquals(50, limited.getPosition());
        }
    }

    @Nested
    @DisplayName("Read Operation Verification Tests")
    class ReadOperationVerificationTests {

        @Test
        @DisplayName("Should verify that read operations never exceed requested limit.")
        void shouldVerifyReadOperationsNeverExceedRequestedLimit() throws IOException {
            var limit = 37;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            // Perform various read operations.
            var buffer1 = new char[10];
            limited.read(buffer1);  // Should read 10 chars.

            var buffer2 = new char[20];
            limited.read(buffer2);  // Should read 20 chars.

            var buffer3 = new char[15];
            limited.read(buffer3);  // Should read 7 chars (the remainder).

            // Get all read operations from the wrapped reader.
            var operations = asserting.getReadOperations();

            // Filter only read operations (not skip).
            var reads = operations
                    .stream()
                    .filter(op -> op.type == AssertionReader.OperationType.READ)
                    .toList();

            // Should have 3 read operations.
            Assertions.assertEquals(3, reads.size());

            // Verify each read operation's actual length.
            Assertions.assertEquals(10, reads.get(0).actualLength);
            Assertions.assertEquals(20, reads.get(1).actualLength);
            Assertions.assertEquals(7, reads.get(2).actualLength);

            // Verify total.
            var total = reads
                    .stream()
                    .mapToLong(op -> op.actualLength)
                    .sum();

            Assertions.assertEquals(limit, total);
        }

        @Test
        @DisplayName("Should verify that skip operations are properly reduced near limit.")
        void shouldVerifySkipOperationsProperlyReducedNearLimit() throws IOException {
            var limit = 25;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            // Read first 10 chars.
            limited.read(new char[10]);

            // Try to skip 20 chars (only 15 available).
            var skipped = limited.skip(20);
            Assertions.assertEquals(15, skipped);

            // Verify the skip operation in wrapped reader.
            var operations = asserting.getReadOperations();

            // Find the skip operation.
            var skipOp = operations
                    .stream()
                    .filter(op -> op.type == AssertionReader.OperationType.SKIP)
                    .findFirst()
                    .orElseThrow();

            // Verify it requested 15 to the wrapped AssertionReader, instead of 20 tried above.
            Assertions.assertEquals(15, skipOp.requestedLength);
            Assertions.assertEquals(15, skipOp.actualLength);
        }

        @Test
        @DisplayName("Should verify that no read operations occur after limit is reached.")
        void shouldVerifyNoReadOperationsAfterLimitReached() throws IOException {
            var limit = 20;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            // Read exactly the limit.
            var buffer = new char[20];
            var read = limited.read(buffer);
            Assertions.assertEquals(20, read);

            // Clear the operation tracking.
            asserting.resetReadOperations();

            // Attempt multiple reads after limit.
            for (var i = 0; i < 5; i++) {
                var result = limited.read();
                Assertions.assertEquals(-1, result);
            }

            // Verify no new read operations were recorded.
            var operations = asserting.getReadOperations();
            Assertions.assertTrue(operations.isEmpty(), "No read operations should occur after limit is reached. Found: " + operations);
        }

        @Test
        @DisplayName("Should verify that partial reads respect the limit precisely.")
        void shouldVerifyPartialReadsRespectLimitPrecisely() throws IOException {
            var limit = 23;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            // Read in varying sizes that don't evenly divide the limit.
            var buffer1 = new char[7];
            var read1 = limited.read(buffer1);
            Assertions.assertEquals(7, read1);

            var buffer2 = new char[9];
            var read2 = limited.read(buffer2);
            Assertions.assertEquals(9, read2);

            var buffer3 = new char[10];
            var read3 = limited.read(buffer3);
            Assertions.assertEquals(7, read3); // Only 7 remaining.

            // Verify total.
            Assertions.assertEquals(limit, read1 + read2 + read3);

            // Verify each read operation's actual length.
            var reads = asserting
                    .getReadOperations()
                    .stream()
                    .filter(op -> op.type == AssertionReader.OperationType.READ)
                    .toList();

            Assertions.assertEquals(7, reads.get(0).actualLength);
            Assertions.assertEquals(9, reads.get(1).actualLength);
            Assertions.assertEquals(7, reads.get(2).actualLength);
        }

        @Test
        @DisplayName("Should verify that resets re-reads the same data again.")
        void shouldVerifyThatMultipleResetsRereadsTheSameDataAgain() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Initial read.
            var firstRead = new char[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(100);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new char[15];
            limited.read(secondRead);
            Assertions.assertEquals(30, asserting.getPosition());

            // Reset and read again.
            limited.reset();
            Assertions.assertEquals(15, asserting.getPosition());

            var resetBuffer = new char[15];
            var resetRead = limited.read(resetBuffer);
            Assertions.assertEquals(15, resetRead);
            Assertions.assertEquals(30, asserting.getPosition());
            Assertions.assertArrayEquals(secondRead, resetBuffer);
        }

        @Test
        @DisplayName("Should verify that the mark limit boundary is respected from inside.")
        void shouldVerifyMarkLimitBoundaryFromInside() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Initial read.
            var firstRead = new char[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new char[15];
            limited.read(secondRead);
            Assertions.assertEquals(30, asserting.getPosition());

            // Reset and read again.
            limited.reset();
            Assertions.assertEquals(15, asserting.getPosition());

            var resetBuffer = new char[15];
            var resetRead = limited.read(resetBuffer);
            Assertions.assertEquals(15, resetRead);
            Assertions.assertEquals(30, asserting.getPosition());
            Assertions.assertArrayEquals(secondRead, resetBuffer);

            // Reset and skip.
            limited.reset();
            Assertions.assertEquals(15, asserting.getPosition());
            var resetSkip = limited.skip(15);
            Assertions.assertEquals(15, resetSkip);
            Assertions.assertEquals(30, asserting.getPosition());
        }

        @Test
        @DisplayName("Should verify that the mark limit boundary is respected from outside on read.")
        void shouldVerifyMarkLimitBoundaryFromOutsideRead() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Initial read.
            var firstRead = new char[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new char[16];
            limited.read(secondRead);
            Assertions.assertEquals(31, asserting.getPosition());
            Assertions.assertFalse(limited.isMarkSet());

            // Asserts that reset fails.
            Assertions.assertThrows(IOException.class, limited::reset);
            Assertions.assertEquals(31, asserting.getPosition());
        }

        @Test
        @DisplayName("Should verify that the mark limit boundary is respected from outside on skip.")
        void shouldVerifyMarkLimitBoundaryFromOutsideSkip() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Initial read.
            var firstRead = new char[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());

            // Skip more.
            limited.skip(16);
            Assertions.assertEquals(31, asserting.getPosition());
            Assertions.assertFalse(limited.isMarkSet());

            // Asserts that reset fails.
            Assertions.assertThrows(IOException.class, limited::reset);
            Assertions.assertEquals(31, asserting.getPosition());
        }

        @Test
        @DisplayName("Should verify that reset won't work after mark boundary.")
        void shouldVerifyThatResetWontWorkAfterMarkBoundaryTooFarMark() throws IOException {
            var limit = 50;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            limited.skip(10);
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());
            limited.skip(16);
            Assertions.assertFalse(limited.isMarkSet());
            Assertions.assertThrows(IOException.class, limited::reset);
        }
    }

    @Nested
    @DisplayName("Limit Integration Tests")
    class LimitIntegrationTests {

        @Test
        @DisplayName("Should work correctly in general.")
        void shouldWorkCorrectlyInGeneral() throws IOException {
            var limit = 30;
            var asserting = new AssertionReader(TEST_DATA, limit, false);
            var limited = new LimitedReader(asserting, limit);

            var buffer = new char[limit];
            var read = limited.read(buffer);

            Assertions.assertEquals(limit, read);
            Assertions.assertEquals(TEST_DATA.substring(0, limit), new String(buffer));
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should maintain correct position after complex operations.")
        void shouldMaintainCorrectPositionAfterComplexOperations() throws IOException {
            var limit = 45;
            var asserting = new AssertionReader(TEST_DATA, limit, true);
            var limited = new LimitedReader(asserting, limit);

            // Read first 10 chars.
            var first = new char[10];
            var read1 = limited.read(first);
            Assertions.assertEquals(10, read1);
            Assertions.assertEquals(10, asserting.getPosition());

            // Mark position.
            limited.mark(100);

            // Read next 15 chars.
            var second = new char[15];
            var read2 = limited.read(second);
            Assertions.assertEquals(15, read2);
            Assertions.assertEquals(25, asserting.getPosition());

            // Reset.
            limited.reset();
            Assertions.assertEquals(10, asserting.getPosition());

            // Read again.
            var third = new char[15];
            var read3 = limited.read(third);
            Assertions.assertEquals(15, read3);
            Assertions.assertEquals(25, asserting.getPosition());

            // Verify content matches.
            Assertions.assertArrayEquals(second, third);
        }
    }
}