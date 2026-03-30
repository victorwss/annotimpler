package ninja.javahacker.test.limited;

import ninja.javahacker.test.ForTests;

import module java.base;
import module org.junit.jupiter.api;
import module org.junit.jupiter.params;
import module ninja.javahacker.annotimpler.sql;

@DisplayName("LimitedInputStream Tests")
@SuppressWarnings({"unused", "NestedAssignment", "ThrowableResultIgnored"})
public class LimitedInputStreamTest {

    private static final byte[] TEST_DATA = new byte[100];

    static {
        for (var i = 0; i < TEST_DATA.length; i++) {
            TEST_DATA[i] = (byte) i;
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @TestFactory
        @DisplayName("Should create instance with valid parameters.")
        Stream<DynamicTest> shouldCreateInstanceWithValidParameters() {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            var limited = new LimitedInputStream(wrapped, 50);
            return Stream.of(
                    DynamicTest.dynamicTest("getMaxSize", () -> Assertions.assertEquals(50, limited.getMaxSize())),
                    DynamicTest.dynamicTest("getRemaining", () -> Assertions.assertEquals(50, limited.getRemaining())),
                    DynamicTest.dynamicTest("getPosition", () -> Assertions.assertEquals(0, limited.getPosition())),
                    DynamicTest.dynamicTest("isMarkSet", () -> Assertions.assertFalse(limited.isMarkSet())),
                    DynamicTest.dynamicTest("isClosed", () -> Assertions.assertFalse(limited.isClosed()))
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when wrapped stream is null.")
        void shouldThrowNullPointerExceptionWhenWrappedIsNull() {
            ForTests.testNull("wrapped", () -> new LimitedInputStream(null, 50));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when maxBytes is negative.")
        void shouldThrowIllegalArgumentExceptionWhenMaxBytesIsNegative() {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            Assertions.assertThrows(IllegalArgumentException.class, () -> new LimitedInputStream(wrapped, -1));
        }

        @Test
        @DisplayName("Should allow maxBytes = 0.")
        void shouldAllowMaxBytesZero() {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            Assertions.assertDoesNotThrow(() -> new LimitedInputStream(wrapped, 0));
        }
    }

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationsTests {

        @Test
        @DisplayName("Should read single byte correctly within limit.")
        void shouldReadSingleByteWithinLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var firstByte = limited.read();
            Assertions.assertEquals(0, firstByte);
            Assertions.assertEquals(1, limited.getPosition());
            Assertions.assertEquals(9, limited.getRemaining());
        }

        @Test
        @DisplayName("Should return -1 when limit is reached.")
        void shouldReturnMinusOneWhenLimitReached() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 5);
            var buffer = new byte[10];
            var read = limited.read(buffer);
            Assertions.assertEquals(5, read);

            var nextByte = limited.read();
            Assertions.assertEquals(-1, nextByte);
            Assertions.assertEquals(5, limited.getPosition());
            Assertions.assertEquals(0, limited.getRemaining());
        }

        @Test
        @DisplayName("Should read bytes correctly within limit.")
        void shouldReadBytesWithinLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 20);
            var buffer = new byte[15];
            var read = limited.read(buffer);

            Assertions.assertEquals(15, read);
            Assertions.assertEquals(15, limited.getPosition());
            Assertions.assertEquals(5, limited.getRemaining());

            // Verify content.
            for (var i = 0; i < 15; i++) {
                Assertions.assertEquals(TEST_DATA[i], buffer[i]);
            }
        }

        @Test
        @DisplayName("Should not read more than limit.")
        void shouldNotReadMoreThanLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var buffer = new byte[20];
            var read = limited.read(buffer);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, limited.getPosition());

            // Only first 10 bytes should be read.
            for (var i = 0; i < 10; i++) {
                Assertions.assertEquals(TEST_DATA[i], buffer[i]);
            }
        }

        @Test
        @DisplayName("Should handle read with offset correctly.")
        void shouldHandleReadWithOffsetCorrectly() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 15);
            var buffer = new byte[20];
            var read = limited.read(buffer, 5, 10);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, limited.getPosition());

            // Verify data was placed at correct offset.
            for (var i = 0; i < 10; i++) {
                Assertions.assertEquals(TEST_DATA[i], buffer[5 + i]);
            }
        }

        @ParameterizedTest
        @DisplayName("Should read correctly with different buffer sizes.")
        @ValueSource(ints = {1, 5, 10, 20, 50})
        void shouldReadCorrectlyWithDifferentBufferSizes(int bufferSize) throws IOException {
            var limit = 30;
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), limit);
            var buffer = new byte[bufferSize];
            var totalRead = 0;
            int bytesRead;

            while ((bytesRead = limited.read(buffer)) != -1) {
                totalRead += bytesRead;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);
        }
    }

    @Nested
    @DisplayName("Skip Operations Tests")
    class SkipOperationsTests {

        @Test
        @DisplayName("Should skip bytes within limit.")
        void shouldSkipBytesWithinLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 20);
            var skipped = limited.skip(10);

            Assertions.assertEquals(10, skipped);
            Assertions.assertEquals(10, limited.getPosition());
            Assertions.assertEquals(10, limited.getRemaining());

            // Verify position by reading next byte.
            var nextByte = limited.read();
            Assertions.assertEquals(TEST_DATA[10], nextByte);
        }

        @Test
        @DisplayName("Should not skip more than limit.")
        void shouldNotSkipMoreThanLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 15);
            var skipped = limited.skip(20);

            Assertions.assertEquals(15, skipped);
            Assertions.assertEquals(15, limited.getPosition());
            Assertions.assertEquals(0, limited.getRemaining());
        }

        @Test
        @DisplayName("Should skip zero when n is zero.")
        void shouldSkipZeroWhenNIsZero() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var skipped = limited.skip(0);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(0, limited.getPosition());
        }

        @Test
        @DisplayName("Should skip zero when limit is reached.")
        void shouldSkipZeroWhenLimitReached() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 5);
            limited.skip(5);
            var skipped = limited.skip(10);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(5, limited.getPosition());
        }

        @Test
        @DisplayName("Should skip negative amount.")
        void shouldSkipNegativeAmount() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var skipped = limited.skip(-5);

            Assertions.assertEquals(0, skipped);
            Assertions.assertEquals(0, limited.getPosition());
        }
    }

    @Nested
    @DisplayName("Available Operations Tests")
    class AvailableOperationsTests {

        @Test
        @DisplayName("Should return correct available bytes within limit.")
        void shouldReturnCorrectAvailableBytesWithinLimit() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 30);
            var available = limited.available();
            Assertions.assertEquals(30, available);
        }

        @Test
        @DisplayName("Should return zero available when limit is reached.")
        void shouldReturnZeroAvailableWhenLimitReached() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            limited.read(new byte[10]);
            var available = limited.available();
            Assertions.assertEquals(0, available);
        }

        @Test
        @DisplayName("Should update available after reading.")
        void shouldUpdateAvailableAfterReading() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 20);
            Assertions.assertEquals(20, limited.available());

            limited.read(new byte[5]);
            Assertions.assertEquals(15, limited.available());

            limited.read(new byte[10]);
            Assertions.assertEquals(5, limited.available());

            limited.read(new byte[5]);
            Assertions.assertEquals(0, limited.available());
        }

        @Test
        @DisplayName("Should return zero for empty data.")
        void shouldReturnZeroForEmptyData() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(new byte[0]), 10);
            Assertions.assertEquals(0, limited.available());
            Assertions.assertEquals(-1, limited.read());
        }
    }

    @Nested
    @DisplayName("Mark/Reset Tests")
    class MarkResetTests {

        @Test
        @DisplayName("Should support mark when wrapped stream supports it.")
        void shouldSupportMarkWhenWrappedStreamSupportsIt() throws IOException {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            var limited = new LimitedInputStream(wrapped, 50);
            Assertions.assertTrue(limited.markSupported());
        }

        @Test
        @DisplayName("Should not support mark when wrapped stream doesn't support it.")
        void shouldNotSupportMarkWhenWrappedStreamDoesntSupportIt() throws IOException {
            var wrapped = new AssertionInputStream(TEST_DATA, 50, false);
            var limited = new LimitedInputStream(wrapped, 50);
            Assertions.assertFalse(limited.markSupported());
        }

        @Test
        @DisplayName("Should fail to mark when wrapped stream doesn't support it.")
        void shouldFailToMarkWhenWrappedDoesntSupportIt() throws IOException {
            var wrapped = new AssertionInputStream(TEST_DATA, 50, false);
            var limited = new LimitedInputStream(wrapped, 50);
            limited.mark(20);
            Assertions.assertFalse(limited.markSupported());
            Assertions.assertFalse(limited.isMarkSet());
        }

        @Test
        @DisplayName("Should mark and reset correctly.")
        void shouldMarkAndResetCorrectly() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 50);

            // Read first 10 bytes.
            var firstRead = new byte[10];
            limited.read(firstRead);
            Assertions.assertEquals(10, limited.getPosition());

            // Mark position.
            limited.mark(100);
            var markedPosition = limited.getPosition();

            // Read next 15 bytes.
            var secondRead = new byte[15];
            limited.read(secondRead);
            Assertions.assertEquals(25, limited.getPosition());

            // Reset to mark.
            limited.reset();
            Assertions.assertEquals(markedPosition, limited.getPosition());

            // Read again from mark position.
            var resetRead = new byte[15];
            var read = limited.read(resetRead);
            Assertions.assertEquals(15, read);
            Assertions.assertEquals(25, limited.getPosition());

            // Verify data matches.
            Assertions.assertArrayEquals(secondRead, resetRead);
        }

        @Test
        @DisplayName("Should allow reading after reset.")
        void shouldAllowReadingAfterReset() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 50);

            // Read and mark.
            limited.read(new byte[5]);
            limited.mark(100);
            limited.read(new byte[10]);
            limited.reset();

            // Read after reset.
            var buffer = new byte[20];
            var read = limited.read(buffer);

            Assertions.assertEquals(20, read);
            Assertions.assertEquals(25, limited.getPosition());
        }

        @Test
        @DisplayName("Should throw IOException when reset without mark.")
        void shouldThrowIOExceptionWhenResetWithoutMark() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 50);
            Assertions.assertThrows(IOException.class, limited::reset);
        }

        @Test
        @DisplayName("Should allow multiple marks and resets.")
        void shouldAllowMultipleMarksAndResets() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 50);

            // First mark.
            limited.read(new byte[5]);
            limited.mark(100);
            var firstMark = limited.getPosition();

            limited.read(new byte[10]);
            limited.reset();
            Assertions.assertEquals(firstMark, limited.getPosition());

            // Second mark.
            limited.read(new byte[3]);
            limited.mark(100);
            var secondMark = limited.getPosition();

            limited.read(new byte[7]);
            limited.reset();
            Assertions.assertEquals(secondMark, limited.getPosition());
        }

        @Test
        @DisplayName("Should handle mark with readlimit parameter.")
        void shouldHandleMarkWithReadlimitParameter() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 50);
            limited.read(new byte[5]);
            limited.mark(20); // Allow reading up to 20 bytes before reset.
            limited.read(new byte[15]);

            // Reset should work since we read within limit.
            Assertions.assertDoesNotThrow(limited::reset);

            // After reset, we should be able to read again.
            var buffer = new byte[10];
            var read = limited.read(buffer);
            Assertions.assertEquals(10, read);
        }
    }

    @Nested
    @DisplayName("Close Operations Tests")
    class CloseOperationsTests {

        @TestFactory
        @DisplayName("Should close wrapped stream.")
        Stream<DynamicTest> shouldCloseWrappedStream() throws IOException {
            var wrapped = new AssertionInputStream(TEST_DATA, 50, false);
            var limited = new LimitedInputStream(wrapped, 50);

            limited.close();
            Assertions.assertTrue(limited.isClosed());
            Assertions.assertTrue(wrapped.isClosed());

            // Trying to do anything (other than mark) after close should throw exception.
            return Stream.of(
                    DynamicTest.dynamicTest("read()"      , () -> Assertions.assertThrows(IOException.class, () -> limited.read())),
                    DynamicTest.dynamicTest("read(1)"     , () -> Assertions.assertThrows(IOException.class, () -> limited.read(new byte[10]))),
                    DynamicTest.dynamicTest("read(3)"     , () -> Assertions.assertThrows(IOException.class, () -> limited.read(new byte[10], 0, 5))),
                    DynamicTest.dynamicTest("skip"        , () -> Assertions.assertThrows(IOException.class, () -> limited.skip(5))),
                    DynamicTest.dynamicTest("mark"        , () -> Assertions.assertDoesNotThrow(() -> limited.mark(50))),
                    DynamicTest.dynamicTest("reset"       , () -> Assertions.assertThrows(IOException.class, () -> limited.reset())),
                    DynamicTest.dynamicTest("getMaxSize"  , () -> Assertions.assertThrows(IOException.class, () -> limited.getMaxSize())),
                    DynamicTest.dynamicTest("getPosition" , () -> Assertions.assertThrows(IOException.class, () -> limited.getPosition())),
                    DynamicTest.dynamicTest("getRemaining", () -> Assertions.assertThrows(IOException.class, () -> limited.getRemaining())),
                    DynamicTest.dynamicTest("isMarkSet"   , () -> Assertions.assertThrows(IOException.class, () -> limited.isMarkSet()))
            );
        }

        @Test
        @DisplayName("Should be idempotent when closing multiple times.")
        void shouldBeIdempotentWhenClosingMultipleTimes() throws IOException {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            var limited = new LimitedInputStream(wrapped, 50);

            limited.close();
            Assertions.assertDoesNotThrow(limited::close);
        }

        @Test
        @DisplayName("Should throw IOException when reading after close.")
        void shouldThrowIOExceptionWhenReadingAfterClose() throws IOException {
            var wrapped = new ByteArrayInputStream(TEST_DATA);
            var limited = new LimitedInputStream(wrapped, 50);

            limited.close();

            Assertions.assertThrows(IOException.class, () -> limited.read());
            Assertions.assertThrows(IOException.class, () -> limited.read(new byte[10]));
        }

        @Test
        @DisplayName("Should work in try-with-resources.")
        void shouldWorkInTryWithResources() throws IOException {
            var wrapped = new AssertionInputStream(TEST_DATA, 50, false);
            LimitedInputStream whatWas;
            try (var limited = new LimitedInputStream(wrapped, 50)) {
                whatWas = limited;
                var buffer = new byte[5];
                limited.read(buffer);
                for (var k = 0; k < 5; k++) {
                    Assertions.assertEquals(TEST_DATA[k], buffer[k]);
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
        @DisplayName("Should handle zero max bytes correctly.")
        void shouldHandleZeroMaxBytesCorrectly() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 0);
            Assertions.assertEquals(0, limited.available());
            Assertions.assertEquals(-1, limited.read());
            Assertions.assertEquals(0, limited.skip(10));

            var buffer = new byte[10];
            Assertions.assertEquals(-1, limited.read(buffer));
            Assertions.assertEquals(0, limited.available());
        }

        @Test
        @DisplayName("Should handle reading exactly limit.")
        void shouldHandleReadingExactlyLimit() throws IOException {
            var limit = 25;
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), limit);
            var buffer = new byte[limit];
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
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), limit);
            var buffer = new byte[readSize];
            var read = limited.read(buffer);

            var expectedRead = Math.min(limit, readSize);
            Assertions.assertEquals(expectedRead, read);
            Assertions.assertEquals(expectedRead, limited.getPosition());
        }

        @Test
        @DisplayName("Should work with empty input stream.")
        void shouldWorkWithEmptyInputStream() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(new byte[0]), 10);
            Assertions.assertEquals(-1, limited.read());
            Assertions.assertEquals(0, limited.skip(5));
            Assertions.assertEquals(0, limited.available());
        }

        @Test
        @DisplayName("Should handle null buffer in read.")
        void shouldHandleNullBufferInRead() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            Assertions.assertThrows(NullPointerException.class, () -> limited.read(null, 0, 10));
        }

        @Test
        @DisplayName("Should handle invalid offset and length.")
        void shouldHandleInvalidOffsetAndLength() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var buffer = new byte[10];

            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, -1, 5));
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, 0, -1));
            Assertions.assertThrows(IndexOutOfBoundsException.class, () -> limited.read(buffer, 8, 5));
        }

        @Test
        @DisplayName("Should handle reading zero length buffer.")
        void shouldHandleReadingZeroLengthBuffer() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 10);
            var buffer = new byte[10];
            var read = limited.read(buffer, 0, 0);

            Assertions.assertEquals(0, read);
            Assertions.assertEquals(0, limited.getPosition());
        }

        @Test
        @DisplayName("Should handle reading after limit with mark/reset.")
        void shouldHandleReadingAfterLimitWithMarkReset() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 20);

            // Read up to limit.
            limited.read(new byte[20]);

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
        @DisplayName("Should work with BufferedInputStream.")
        void shouldWorkWithBufferedReader() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 30);
            var buffered = new BufferedInputStream(limited);
            var line = new byte[50];
            var out = buffered.read(line);
            Assertions.assertEquals(30, out);
        }

        @Test
        @DisplayName("Should maintain byte count correctly with complex operations.")
        void shouldMaintainByteCountCorrectlyWithComplexOperations() throws IOException {
            var limited = new LimitedInputStream(new ByteArrayInputStream(TEST_DATA), 40);

            // Mix of different operations.
            limited.skip(5);
            Assertions.assertEquals(5, limited.getPosition());

            var buffer1 = new byte[10];
            limited.read(buffer1);
            Assertions.assertEquals(15, limited.getPosition());

            limited.mark(100);
            var buffer2 = new byte[10];
            limited.read(buffer2);
            Assertions.assertEquals(25, limited.getPosition());

            limited.reset();
            Assertions.assertEquals(15, limited.getPosition());

            var buffer3 = new byte[15];
            var read = limited.read(buffer3);
            Assertions.assertEquals(15, read);
            Assertions.assertEquals(30, limited.getPosition());
        }
    }

    @Nested
    @DisplayName("Tests with AssertionInputStream - Verifying No Excess Reads")
    class AssertionVerificationTests {

        @Test
        @DisplayName("Should NOT cause AssertionError when reading exactly up to limit.")
        void shouldNotCauseAssertionErrorWhenReadingExactlyLimit() throws IOException {
            var limit = 15;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[limit];
            var read = limited.read(buffer);

            Assertions.assertEquals(limit, read);
            Assertions.assertEquals(limit, asserting.getPosition());
            for (var i = 0; i < 15; i++) {
                Assertions.assertEquals(TEST_DATA[i], buffer[i]);
            }
            // No assertion error should be thrown.
        }

        @Test
        @DisplayName("Should NOT cause AssertionError when reading in chunks up to limit.")
        void shouldNotCauseAssertionErrorWhenReadingInChunksUpToLimit() throws IOException {
            var limit = 25;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);
            var buffer = new byte[7];

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
        @DisplayName("Should NOT cause AssertionError when reading one byte at a time.")
        void shouldNotCauseAssertionErrorWhenReadingOneByteAtATime() throws IOException {
            var limit = 20;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            for (var i = 0; i < limit; i++) {
                var c = limited.read();
                Assertions.assertEquals(TEST_DATA[i], c);
            }

            var cl = limited.read();
            Assertions.assertEquals(-1, cl);
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should NOT cause AssertionError when skip is used.")
        void shouldNotCauseAssertionErrorWhenSkipIsUsed() throws IOException {
            var limit = 25;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var skipped1 = limited.skip(10);
            Assertions.assertEquals(10, skipped1);

            var buffer = new byte[10];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            var firstRead = new byte[15];
            limited.read(firstRead);
            limited.mark(100);

            var secondRead = new byte[15];
            limited.read(secondRead);

            limited.reset();
            var resetRead = new byte[15];
            var resetBytes = limited.read(resetRead);

            Assertions.assertEquals(15, resetBytes);
            Assertions.assertEquals(30, asserting.getPosition());
        }

        @Test
        @DisplayName("Should verify that wrapped stream is never read beyond limit.")
        void shouldVerifyWrappedStreamNeverReadBeyondLimit() throws IOException {
            var limit = 30;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            // Try to read more than limit in various ways.
            var largeBuffer = new byte[50];
            var read1 = limited.read(largeBuffer);
            Assertions.assertEquals(limit, read1);

            // This should NOT cause AssertionError because the limited stream
            // should prevent the wrapped stream from being called again.
            var read2 = limited.read();
            Assertions.assertEquals(-1, read2);

            // The wrapped stream should still have only read 'limit' bytes.
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @ParameterizedTest
        @DisplayName("Should verify read operations never request more than available limit.")
        @ValueSource(ints = {1, 10, 25, 50})
        void shouldVerifyReadOperationsNeverRequestMoreThanAvailableLimit(int limit) throws IOException {
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[limit + 20];
            var totalRead = 0;
            int bytesRead;

            while ((bytesRead = limited.read(buffer)) != -1) {
                totalRead += bytesRead;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);

            // Verify each read operation from the wrapped stream.
            var operations = asserting.getReadOperations();

            // Sum of all actual bytes read should equal limit.
            var totalRequested = operations
                    .stream()
                    .filter(op -> op.type == AssertionInputStream.OperationType.READ)
                    .mapToLong(op -> op.actualLength)
                    .sum();

            Assertions.assertEquals(limit, totalRequested);

            // No operation should have requested more than the remaining limit.
            var runningTotal = 0L;
            for (var op : operations) {
                if (op.type == AssertionInputStream.OperationType.READ) {
                    Assertions.assertTrue(
                            op.actualLength <= (limit - runningTotal),
                            "Operation requested " + op.actualLength + " bytes but only " + (limit - runningTotal) + " remaining"
                    );
                    runningTotal += op.actualLength;
                }
            }
        }

        @Test
        @DisplayName("Should handle mark/reset correctly when near limit.")
        void shouldHandleMarkResetCorrectlyWhenNearLimit() throws IOException {
            var limit = 20;
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Read 15 bytes.
            var firstRead = new byte[15];
            var read1 = limited.read(firstRead);
            Assertions.assertEquals(15, read1);

            // Mark position.
            limited.mark(100);
            var markedPosition = limited.getPosition();
            Assertions.assertEquals(15, markedPosition);

            // Try to read 10 more bytes (only 5 available).
            var secondRead = new byte[10];
            var read2 = limited.read(secondRead);
            Assertions.assertEquals(5, read2);
            Assertions.assertEquals(20, limited.getPosition());

            // Reset should go back to marked position.
            limited.reset();
            Assertions.assertEquals(15, limited.getPosition());

            // Read again - should read the same 5 bytes.
            var resetRead = new byte[10];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[10];
            var read = limited.read(buffer, 0, 10);

            Assertions.assertEquals(10, read);
            Assertions.assertEquals(10, asserting.getPosition());

            // Verify the wrapped stream received exactly one read of 10 bytes.
            var operations = asserting.getReadOperations();
            Assertions.assertEquals(1, operations.size());
            Assertions.assertEquals(10, operations.get(0).actualLength);
            Assertions.assertEquals(AssertionInputStream.OperationType.READ, operations.get(0).type);

            // Next read should not cause any additional read from wrapped stream.
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[Math.max(initialRead, finalRead)];

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
        @DisplayName("Should handle zero-byte reads correctly.")
        void shouldHandleZeroByteReadsCorrectly() throws IOException {
            var limit = 10;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[10];

            var zeroRead = limited.read(buffer, 0, 0);
            Assertions.assertEquals(0, zeroRead);
            Assertions.assertEquals(0, asserting.getPosition());
            Assertions.assertTrue(asserting.getReadOperations().isEmpty());

            var normalRead = limited.read(buffer);
            Assertions.assertEquals(limit, normalRead);
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should handle available() correctly at boundaries.")
        void shouldHandleReadyCorrectlyAtBoundaries() throws IOException {
            var limit = 15;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            Assertions.assertEquals(15, limited.available());
            limited.read(new byte[limit]);
            Assertions.assertEquals(0, limited.available());
            Assertions.assertEquals(0, limited.available());
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[smallReadSize];
            var totalRead = 0;
            var readCount = 0;
            int bytesRead;

            while ((bytesRead = limited.read(buffer)) != -1) {
                totalRead += bytesRead;
                readCount++;
                Assertions.assertTrue(totalRead <= limit);
            }

            Assertions.assertEquals(limit, totalRead);
            Assertions.assertEquals(limit, asserting.getPosition());

            // Verify that the sum of all reads equals the limit.
            var sumOfReads = asserting
                    .getReadOperations()
                    .stream()
                    .filter(op -> op.type == AssertionInputStream.OperationType.READ)
                    .mapToLong(op -> op.actualLength)
                    .sum();

            Assertions.assertEquals(limit, sumOfReads);
        }

        @Test
        @DisplayName("Should handle alternating read and skip operations.")
        void shouldHandleAlternatingReadAndSkipOperations() throws IOException {
            var limit = 100;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);
            var buffer = new byte[7];

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
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer1 = new byte[5];
            var buffer2 = new byte[5];

            for (var i = 0; i < 10; i++) {
                limited.mark(20);
                var read = limited.read(buffer1);
                Assertions.assertEquals(5, read);
                Assertions.assertEquals(i * 5 + 5, limited.getPosition());
                for (var k = 0; k < 5; k++) {
                    Assertions.assertEquals(buffer1[k], TEST_DATA[i * 5 + k]);
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            // Perform various read operations.
            var buffer1 = new byte[10];
            limited.read(buffer1);  // Should read 10 bytes.

            var buffer2 = new byte[20];
            limited.read(buffer2);  // Should read 20 bytes.

            var buffer3 = new byte[15];
            limited.read(buffer3);  // Should read 7 bytes (the remainder).

            // Get all read operations from the wrapped stream.
            var operations = asserting.getReadOperations();

            // Filter only read operations (not skip).
            var reads = operations
                    .stream()
                    .filter(op -> op.type == AssertionInputStream.OperationType.READ)
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            // Read first 10 bytes.
            limited.read(new byte[10]);

            // Try to skip 20 bytes (only 15 available).
            var skipped = limited.skip(20);
            Assertions.assertEquals(15, skipped);

            // Verify the skip operation in wrapped stream.
            var operations = asserting.getReadOperations();

            // Find the skip operation.
            var skipOp = operations
                    .stream()
                    .filter(op -> op.type == AssertionInputStream.OperationType.SKIP)
                    .findFirst()
                    .orElseThrow();

            // Verify it requested 15 to the wrapped AssertionInputStream, instead of 20 tried above.
            Assertions.assertEquals(15, skipOp.requestedLength);
            Assertions.assertEquals(15, skipOp.actualLength);
        }

        @Test
        @DisplayName("Should verify that no read operations occur after limit is reached.")
        void shouldVerifyNoReadOperationsAfterLimitReached() throws IOException {
            var limit = 20;
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            // Read exactly the limit.
            var buffer = new byte[20];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            // Read in varying sizes that don't evenly divide the limit.
            var buffer1 = new byte[7];
            var read1 = limited.read(buffer1);
            Assertions.assertEquals(7, read1);

            var buffer2 = new byte[9];
            var read2 = limited.read(buffer2);
            Assertions.assertEquals(9, read2);

            var buffer3 = new byte[10];
            var read3 = limited.read(buffer3);
            Assertions.assertEquals(7, read3); // Only 7 remaining.

            // Verify total.
            Assertions.assertEquals(limit, read1 + read2 + read3);

            // Verify each read operation's actual length.
            var reads = asserting
                    .getReadOperations()
                    .stream()
                    .filter(op -> op.type == AssertionInputStream.OperationType.READ)
                    .toList();

            Assertions.assertEquals(7, reads.get(0).actualLength);
            Assertions.assertEquals(9, reads.get(1).actualLength);
            Assertions.assertEquals(7, reads.get(2).actualLength);
        }
    
        @Test
        @DisplayName("Should verify that resets re-reads the same data again.")
        void shouldVerifyThatMultipleResetsRereadsTheSameDataAgain() throws IOException {
            var limit = 50;
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Initial read.
            var firstRead = new byte[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(100);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new byte[15];
            limited.read(secondRead);
            Assertions.assertEquals(30, asserting.getPosition());

            // Reset and read again.
            limited.reset();
            Assertions.assertEquals(15, asserting.getPosition());

            var resetBuffer = new byte[15];
            var resetRead = limited.read(resetBuffer);
            Assertions.assertEquals(15, resetRead);
            Assertions.assertEquals(30, asserting.getPosition());
            Assertions.assertArrayEquals(secondRead, resetBuffer);
        }

        @Test
        @DisplayName("Should verify that the mark limit boundary is respected from inside.")
        void shouldVerifyMarkLimitBoundaryFromInside() throws IOException {
            var limit = 50;
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Initial read.
            var firstRead = new byte[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new byte[15];
            limited.read(secondRead);
            Assertions.assertEquals(30, asserting.getPosition());

            // Reset and read again.
            limited.reset();
            Assertions.assertEquals(15, asserting.getPosition());

            var resetBuffer = new byte[15];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Initial read.
            var firstRead = new byte[15];
            limited.read(firstRead);
            Assertions.assertFalse(limited.isMarkSet());
            limited.mark(15);
            Assertions.assertTrue(limited.isMarkSet());

            // Read more.
            var secondRead = new byte[16];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Initial read.
            var firstRead = new byte[15];
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
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

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
            var asserting = new AssertionInputStream(TEST_DATA, limit, false);
            var limited = new LimitedInputStream(asserting, limit);

            var buffer = new byte[limit];
            var read = limited.read(buffer);

            Assertions.assertEquals(limit, read);
            for (var i = 0; i < limit; i++) {
                Assertions.assertEquals(TEST_DATA[i], buffer[i]);
            }
            Assertions.assertEquals(limit, asserting.getPosition());
        }

        @Test
        @DisplayName("Should maintain correct position after complex operations.")
        void shouldMaintainCorrectPositionAfterComplexOperations() throws IOException {
            var limit = 45;
            var asserting = new AssertionInputStream(TEST_DATA, limit, true);
            var limited = new LimitedInputStream(asserting, limit);

            // Read first 10 bytes.
            var first = new byte[10];
            var read1 = limited.read(first);
            Assertions.assertEquals(10, read1);
            Assertions.assertEquals(10, asserting.getPosition());

            // Mark position.
            limited.mark(100);

            // Read next 15 bytes.
            var second = new byte[15];
            var read2 = limited.read(second);
            Assertions.assertEquals(15, read2);
            Assertions.assertEquals(25, asserting.getPosition());

            // Reset.
            limited.reset();
            Assertions.assertEquals(10, asserting.getPosition());

            // Read again.
            var third = new byte[15];
            var read3 = limited.read(third);
            Assertions.assertEquals(15, read3);
            Assertions.assertEquals(25, asserting.getPosition());

            // Verify content matches.
            Assertions.assertArrayEquals(second, third);
        }
    }
}
