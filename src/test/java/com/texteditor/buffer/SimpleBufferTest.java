package com.texteditor.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for SimpleBuffer implementation.
 */
@DisplayName("SimpleBuffer Tests")
class SimpleBufferTest {

    private SimpleBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new SimpleBuffer();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Empty constructor creates empty buffer")
    void testEmptyConstructor() {
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
        assertEquals("simple", buffer.getType());
    }

    @Test
    @DisplayName("Constructor with initial content")
    void testConstructorWithContent() {
        SimpleBuffer buffer = new SimpleBuffer("Hello World");
        assertEquals(11, buffer.length());
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Constructor rejects null content")
    void testConstructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleBuffer(null));
    }

    // ========== Insert Tests ==========

    @Test
    @DisplayName("Insert into empty buffer")
    void testInsertIntoEmpty() {
        buffer.insert(0, "Hello");
        assertEquals(5, buffer.length());
        assertEquals("Hello", buffer.toString());
    }

    @Test
    @DisplayName("Insert at beginning")
    void testInsertAtBeginning() {
        buffer.insert(0, "World");
        buffer.insert(0, "Hello ");
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Insert at end")
    void testInsertAtEnd() {
        buffer.insert(0, "Hello");
        buffer.insert(5, " World");
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Insert in middle")
    void testInsertInMiddle() {
        buffer.insert(0, "Hello World");
        buffer.insert(6, "Beautiful ");
        assertEquals("Hello Beautiful World", buffer.toString());
    }

    @Test
    @DisplayName("Insert empty string")
    void testInsertEmptyString() {
        buffer.insert(0, "Hello");
        buffer.insert(5, "");
        assertEquals("Hello", buffer.toString());
        assertEquals(5, buffer.length());
    }

    @Test
    @DisplayName("Insert multiple times sequentially")
    void testMultipleInserts() {
        buffer.insert(0, "a");
        buffer.insert(1, "b");
        buffer.insert(2, "c");
        assertEquals("abc", buffer.toString());
    }

    @Test
    @DisplayName("Insert rejects null text")
    void testInsertRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> buffer.insert(0, null));
    }

    @Test
    @DisplayName("Insert rejects negative position")
    void testInsertRejectsNegativePosition() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.insert(-1, "test"));
    }

    @Test
    @DisplayName("Insert rejects position beyond length")
    void testInsertRejectsInvalidPosition() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.insert(6, "test"));
    }

    @Test
    @DisplayName("Insert Unicode characters")
    void testInsertUnicode() {
        buffer.insert(0, "Hello ");
        buffer.insert(6, "🌍");
        // Emoji 🌍 is a surrogate pair, takes 2 char positions in Java String
        buffer.insert(8, " 世界");
        assertEquals("Hello 🌍 世界", buffer.toString());
    }

    // ========== Delete Tests ==========

    @Test
    @DisplayName("Delete from beginning")
    void testDeleteFromBeginning() {
        buffer.insert(0, "Hello World");
        buffer.delete(0, 6);
        assertEquals("World", buffer.toString());
    }

    @Test
    @DisplayName("Delete from end")
    void testDeleteFromEnd() {
        buffer.insert(0, "Hello World");
        buffer.delete(5, 11);
        assertEquals("Hello", buffer.toString());
    }

    @Test
    @DisplayName("Delete from middle")
    void testDeleteFromMiddle() {
        buffer.insert(0, "Hello Beautiful World");
        buffer.delete(5, 15);  // Delete " Beautiful" to get "Hello World"
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Delete entire buffer")
    void testDeleteEntireBuffer() {
        buffer.insert(0, "Hello World");
        buffer.delete(0, 11);
        assertEquals("", buffer.toString());
        assertEquals(0, buffer.length());
    }

    @Test
    @DisplayName("Delete single character")
    void testDeleteSingleCharacter() {
        buffer.insert(0, "Hello");
        buffer.delete(1, 2);
        assertEquals("Hllo", buffer.toString());
    }

    @Test
    @DisplayName("Delete empty range has no effect")
    void testDeleteEmptyRange() {
        buffer.insert(0, "Hello");
        buffer.delete(2, 2);
        assertEquals("Hello", buffer.toString());
    }

    @Test
    @DisplayName("Delete rejects negative start position")
    void testDeleteRejectsNegativeStart() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.delete(-1, 3));
    }

    @Test
    @DisplayName("Delete rejects end position beyond length")
    void testDeleteRejectsInvalidEnd() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.delete(0, 10));
    }

    @Test
    @DisplayName("Delete rejects start greater than end")
    void testDeleteRejectsInvalidRange() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.delete(3, 1));
    }

    // ========== Substring Tests ==========

    @Test
    @DisplayName("Substring entire buffer")
    void testSubstringEntireBuffer() {
        buffer.insert(0, "Hello World");
        assertEquals("Hello World", buffer.substring(0, 11));
    }

    @Test
    @DisplayName("Substring from beginning")
    void testSubstringFromBeginning() {
        buffer.insert(0, "Hello World");
        assertEquals("Hello", buffer.substring(0, 5));
    }

    @Test
    @DisplayName("Substring from end")
    void testSubstringFromEnd() {
        buffer.insert(0, "Hello World");
        assertEquals("World", buffer.substring(6, 11));
    }

    @Test
    @DisplayName("Substring from middle")
    void testSubstringFromMiddle() {
        buffer.insert(0, "Hello World");
        assertEquals("lo Wo", buffer.substring(3, 8));
    }

    @Test
    @DisplayName("Substring single character")
    void testSubstringSingleCharacter() {
        buffer.insert(0, "Hello");
        assertEquals("e", buffer.substring(1, 2));
    }

    @Test
    @DisplayName("Substring empty range returns empty string")
    void testSubstringEmptyRange() {
        buffer.insert(0, "Hello");
        assertEquals("", buffer.substring(2, 2));
    }

    @Test
    @DisplayName("Substring from empty buffer")
    void testSubstringFromEmptyBuffer() {
        assertEquals("", buffer.substring(0, 0));
    }

    @Test
    @DisplayName("Substring rejects negative start")
    void testSubstringRejectsNegativeStart() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.substring(-1, 3));
    }

    @Test
    @DisplayName("Substring rejects end beyond length")
    void testSubstringRejectsInvalidEnd() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.substring(0, 10));
    }

    @Test
    @DisplayName("Substring rejects start greater than end")
    void testSubstringRejectsInvalidRange() {
        buffer.insert(0, "Hello");
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.substring(3, 1));
    }

    // ========== Length Tests ==========

    @Test
    @DisplayName("Length of empty buffer")
    void testLengthEmpty() {
        assertEquals(0, buffer.length());
    }

    @Test
    @DisplayName("Length after insertions")
    void testLengthAfterInserts() {
        buffer.insert(0, "Hello");
        assertEquals(5, buffer.length());
        buffer.insert(5, " World");
        assertEquals(11, buffer.length());
    }

    @Test
    @DisplayName("Length after deletions")
    void testLengthAfterDeletes() {
        buffer.insert(0, "Hello World");
        assertEquals(11, buffer.length());
        buffer.delete(5, 11);
        assertEquals(5, buffer.length());
    }

    // ========== Clear Tests ==========

    @Test
    @DisplayName("Clear empty buffer")
    void testClearEmpty() {
        buffer.clear();
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
    }

    @Test
    @DisplayName("Clear non-empty buffer")
    void testClearNonEmpty() {
        buffer.insert(0, "Hello World");
        buffer.clear();
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
    }

    @Test
    @DisplayName("Insert after clear")
    void testInsertAfterClear() {
        buffer.insert(0, "Hello");
        buffer.clear();
        buffer.insert(0, "World");
        assertEquals("World", buffer.toString());
    }

    // ========== Complex Scenario Tests ==========

    @Test
    @DisplayName("Complex editing scenario")
    void testComplexScenario() {
        buffer.insert(0, "The quick brown fox");
        assertEquals(19, buffer.length());

        buffer.insert(10, "red ");
        assertEquals("The quick red brown fox", buffer.toString());

        buffer.delete(10, 14);
        assertEquals("The quick brown fox", buffer.toString());

        String sub = buffer.substring(4, 9);
        assertEquals("quick", sub);

        buffer.clear();
        assertEquals(0, buffer.length());
    }

    @Test
    @DisplayName("Build document incrementally")
    void testIncrementalBuild() {
        buffer.insert(0, "Line 1");
        buffer.insert(6, "\n");
        buffer.insert(7, "Line 2");
        buffer.insert(13, "\n");
        buffer.insert(14, "Line 3");

        String expected = "Line 1\nLine 2\nLine 3";
        assertEquals(expected, buffer.toString());
        assertEquals(20, buffer.length());
    }

    @ParameterizedTest
    @CsvSource({
        "'', 0",
        "'a', 1",
        "'Hello', 5",
        "'Hello World', 11",
        "'🌍', 2",  // Emoji is a surrogate pair, counts as 2 chars
        "'Hello 🌍', 8"  // "Hello " (6) + emoji (2) = 8
    })
    @DisplayName("Parameterized length tests")
    void testParameterizedLength(String text, int expectedLength) {
        SimpleBuffer testBuffer = new SimpleBuffer(text);
        assertEquals(expectedLength, testBuffer.length());
    }

    // ========== GetType Tests ==========

    @Test
    @DisplayName("getType returns correct type")
    void testGetType() {
        assertEquals("simple", buffer.getType());
    }

    // ========== ToString Tests ==========

    @Test
    @DisplayName("toString on empty buffer")
    void testToStringEmpty() {
        assertEquals("", buffer.toString());
    }

    @Test
    @DisplayName("toString returns full content")
    void testToStringFull() {
        buffer.insert(0, "Hello World");
        assertEquals("Hello World", buffer.toString());
    }

    // ========== Edge Case Tests ==========

    @Test
    @DisplayName("Large text insertion")
    void testLargeTextInsertion() {
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            large.append("Line ").append(i).append("\n");
        }
        String largeText = large.toString();

        buffer.insert(0, largeText);
        assertEquals(largeText, buffer.toString());
        assertTrue(buffer.length() > 5000);
    }

    @Test
    @DisplayName("Multiple consecutive operations")
    void testMultipleConsecutiveOperations() {
        for (int i = 0; i < 100; i++) {
            buffer.insert(buffer.length(), String.valueOf(i));
        }
        assertTrue(buffer.length() > 100);

        buffer.delete(0, 50);
        assertTrue(buffer.length() < 200);

        buffer.clear();
        assertEquals(0, buffer.length());
    }

    @Test
    @DisplayName("Whitespace handling")
    void testWhitespaceHandling() {
        buffer.insert(0, "   ");
        assertEquals(3, buffer.length());
        buffer.insert(1, "\t");
        assertEquals(4, buffer.length());
        buffer.insert(4, "\n");
        assertEquals(5, buffer.length());
    }
}
