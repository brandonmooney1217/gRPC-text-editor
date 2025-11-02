package com.texteditor.document;

import com.texteditor.buffer.SimpleBuffer;
import com.texteditor.buffer.TextBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DocumentImpl.
 */
@DisplayName("DocumentImpl Tests")
class DocumentImplTest {

    private TextBuffer buffer;
    private Document document;

    @BeforeEach
    void setUp() {
        buffer = new SimpleBuffer();
        document = new DocumentImpl("test-doc", buffer);
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Constructor creates document with valid parameters")
    void testConstructorValid() {
        TextBuffer buf = new SimpleBuffer("Hello");
        Document doc = new DocumentImpl("doc-1", buf);

        assertEquals("doc-1", doc.getId());
        assertEquals(buf, doc.getBuffer());
        assertEquals(5, doc.getCursorPosition()); // Cursor at end
        assertEquals(5, doc.length());
    }

    @Test
    @DisplayName("Constructor initializes cursor at end of buffer")
    void testConstructorCursorAtEnd() {
        TextBuffer buf = new SimpleBuffer("Test");
        Document doc = new DocumentImpl("doc-1", buf);
        assertEquals(4, doc.getCursorPosition());
    }

    @Test
    @DisplayName("Constructor with empty buffer has cursor at 0")
    void testConstructorEmptyBuffer() {
        TextBuffer buf = new SimpleBuffer();
        Document doc = new DocumentImpl("doc-1", buf);
        assertEquals(0, doc.getCursorPosition());
    }

    @Test
    @DisplayName("Constructor rejects null document ID")
    void testConstructorNullId() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentImpl(null, buffer));
    }

    @Test
    @DisplayName("Constructor rejects empty document ID")
    void testConstructorEmptyId() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentImpl("", buffer));
    }

    @Test
    @DisplayName("Constructor rejects null buffer")
    void testConstructorNullBuffer() {
        assertThrows(IllegalArgumentException.class,
            () -> new DocumentImpl("doc-1", null));
    }

    // ========== Get ID Tests ==========

    @Test
    @DisplayName("getId returns correct document ID")
    void testGetId() {
        Document doc = new DocumentImpl("my-doc-123", buffer);
        assertEquals("my-doc-123", doc.getId());
    }

    // ========== Get Buffer Tests ==========

    @Test
    @DisplayName("getBuffer returns underlying buffer")
    void testGetBuffer() {
        assertEquals(buffer, document.getBuffer());
    }

    // ========== Cursor Position Tests ==========

    @Test
    @DisplayName("getCursorPosition returns current position")
    void testGetCursorPosition() {
        assertEquals(0, document.getCursorPosition());
        document.insert(-1, "Hello", true);
        assertEquals(5, document.getCursorPosition());
    }

    @Test
    @DisplayName("setCursorPosition sets valid position")
    void testSetCursorPositionValid() {
        document.insert(-1, "Hello", false);

        document.setCursorPosition(0);
        assertEquals(0, document.getCursorPosition());

        document.setCursorPosition(3);
        assertEquals(3, document.getCursorPosition());

        document.setCursorPosition(5);
        assertEquals(5, document.getCursorPosition());
    }

    @Test
    @DisplayName("setCursorPosition rejects negative position")
    void testSetCursorPositionNegative() {
        document.insert(-1, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.setCursorPosition(-1));
    }

    @Test
    @DisplayName("setCursorPosition rejects position beyond length")
    void testSetCursorPositionBeyondLength() {
        document.insert(-1, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.setCursorPosition(6));
    }

    @Test
    @DisplayName("moveCursor with positive offset")
    void testMoveCursorPositive() {
        document.insert(-1, "Hello", false);
        document.setCursorPosition(0);

        document.moveCursor(3);
        assertEquals(3, document.getCursorPosition());

        document.moveCursor(2);
        assertEquals(5, document.getCursorPosition());
    }

    @Test
    @DisplayName("moveCursor with negative offset")
    void testMoveCursorNegative() {
        document.insert(-1, "Hello", true);
        assertEquals(5, document.getCursorPosition());

        document.moveCursor(-2);
        assertEquals(3, document.getCursorPosition());

        document.moveCursor(-3);
        assertEquals(0, document.getCursorPosition());
    }

    @Test
    @DisplayName("moveCursor clamps to lower bound")
    void testMoveCursorClampLower() {
        document.insert(-1, "Hello", false);
        document.setCursorPosition(2);

        document.moveCursor(-10);
        assertEquals(0, document.getCursorPosition());
    }

    @Test
    @DisplayName("moveCursor clamps to upper bound")
    void testMoveCursorClampUpper() {
        document.insert(-1, "Hello", false);
        document.setCursorPosition(2);

        document.moveCursor(10);
        assertEquals(5, document.getCursorPosition());
    }

    // ========== Insert Tests ==========

    @Test
    @DisplayName("Insert at cursor position (-1)")
    void testInsertAtCursor() {
        document.insert(-1, "Hello", true);
        assertEquals("Hello", buffer.toString());
        assertEquals(5, document.getCursorPosition());

        document.setCursorPosition(0);
        document.insert(-1, ">> ", true);
        assertEquals(">> Hello", buffer.toString());
        assertEquals(3, document.getCursorPosition());
    }

    @Test
    @DisplayName("Insert at explicit position")
    void testInsertAtExplicitPosition() {
        document.insert(0, "Hello", false);
        document.insert(5, " World", false);
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Insert with moveCursor=true moves cursor")
    void testInsertWithMoveCursorTrue() {
        document.setCursorPosition(0);
        document.insert(0, "Hello", true);
        assertEquals(5, document.getCursorPosition());

        document.insert(5, " World", true);
        assertEquals(11, document.getCursorPosition());
    }

    @Test
    @DisplayName("Insert with moveCursor=false keeps cursor if before insertion")
    void testInsertWithMoveCursorFalse_CursorBefore() {
        document.insert(0, "Hello", false);
        document.setCursorPosition(2);

        document.insert(5, " World", false);
        assertEquals(2, document.getCursorPosition()); // Cursor stays at 2
        assertEquals("Hello World", buffer.toString());
    }

    @Test
    @DisplayName("Insert with moveCursor=false adjusts cursor if at or after insertion")
    void testInsertWithMoveCursorFalse_CursorAfter() {
        document.insert(0, "Hello", false);
        document.setCursorPosition(5);

        document.insert(0, ">> ", false);
        assertEquals(8, document.getCursorPosition()); // Adjusted from 5 to 8
        assertEquals(">> Hello", buffer.toString());
    }

    @Test
    @DisplayName("Insert empty string does not move cursor")
    void testInsertEmptyString() {
        document.insert(0, "Hello", true);
        document.setCursorPosition(3);

        document.insert(3, "", true);
        assertEquals(3, document.getCursorPosition());
        assertEquals("Hello", buffer.toString());
    }

    @Test
    @DisplayName("Insert rejects null text")
    void testInsertRejectsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> document.insert(0, null, true));
    }

    @Test
    @DisplayName("Insert rejects invalid position")
    void testInsertRejectsInvalidPosition() {
        document.insert(0, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.insert(-2, "test", true));
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.insert(10, "test", true));
    }

    // ========== Delete Tests ==========

    @Test
    @DisplayName("Delete with moveCursor=true moves cursor to start")
    void testDeleteWithMoveCursorTrue() {
        document.insert(0, "Hello World", false);
        document.setCursorPosition(10);

        document.delete(6, 11, true);  // Delete "World" leaving "Hello "
        assertEquals(6, document.getCursorPosition());
        assertEquals("Hello ", buffer.toString());
    }

    @Test
    @DisplayName("Delete with moveCursor=false, cursor before deletion")
    void testDeleteWithMoveCursorFalse_CursorBefore() {
        document.insert(0, "Hello World", false);
        document.setCursorPosition(2);

        document.delete(6, 11, false);
        assertEquals(2, document.getCursorPosition()); // Stays at 2
        assertEquals("Hello ", buffer.toString());
    }

    @Test
    @DisplayName("Delete with moveCursor=false, cursor after deletion")
    void testDeleteWithMoveCursorFalse_CursorAfter() {
        document.insert(0, "Hello World", false);
        document.setCursorPosition(10);

        document.delete(0, 6, false);
        assertEquals(4, document.getCursorPosition()); // Adjusted from 10 to 4
        assertEquals("World", buffer.toString());
    }

    @Test
    @DisplayName("Delete with moveCursor=false, cursor inside deletion")
    void testDeleteWithMoveCursorFalse_CursorInside() {
        document.insert(0, "Hello World", false);
        document.setCursorPosition(7);

        document.delete(6, 11, false);  // Delete "World" leaving "Hello "
        assertEquals(6, document.getCursorPosition()); // Moved to start of deletion
        assertEquals("Hello ", buffer.toString());
    }

    @Test
    @DisplayName("Delete entire document")
    void testDeleteEntireDocument() {
        document.insert(0, "Hello", true);
        document.delete(0, 5, false);

        assertEquals("", buffer.toString());
        assertEquals(0, document.length());
        assertEquals(0, document.getCursorPosition());
    }

    @Test
    @DisplayName("Delete empty range has no effect")
    void testDeleteEmptyRange() {
        document.insert(0, "Hello", false);
        document.setCursorPosition(3);

        document.delete(2, 2, false);
        assertEquals("Hello", buffer.toString());
        assertEquals(3, document.getCursorPosition());
    }

    @Test
    @DisplayName("Delete rejects negative start")
    void testDeleteRejectsNegativeStart() {
        document.insert(0, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.delete(-1, 3, false));
    }

    @Test
    @DisplayName("Delete rejects end beyond length")
    void testDeleteRejectsEndBeyondLength() {
        document.insert(0, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.delete(0, 10, false));
    }

    @Test
    @DisplayName("Delete rejects start greater than end")
    void testDeleteRejectsInvalidRange() {
        document.insert(0, "Hello", false);
        assertThrows(IndexOutOfBoundsException.class,
            () -> document.delete(3, 1, false));
    }

    // ========== Substring Tests ==========

    @Test
    @DisplayName("substring returns correct text")
    void testSubstring() {
        document.insert(0, "Hello World", false);

        assertEquals("Hello", document.substring(0, 5));
        assertEquals("World", document.substring(6, 11));
        assertEquals("lo Wo", document.substring(3, 8));
    }

    @Test
    @DisplayName("substring does not affect cursor")
    void testSubstringDoesNotAffectCursor() {
        document.insert(0, "Hello", true);
        document.setCursorPosition(3);

        document.substring(0, 5);
        assertEquals(3, document.getCursorPosition());
    }

    // ========== Length Tests ==========

    @Test
    @DisplayName("length returns correct document length")
    void testLength() {
        assertEquals(0, document.length());

        document.insert(0, "Hello", false);
        assertEquals(5, document.length());

        document.insert(5, " World", false);
        assertEquals(11, document.length());

        document.delete(0, 6, false);
        assertEquals(5, document.length());
    }

    // ========== Complex Scenarios ==========

    @Test
    @DisplayName("Complex editing scenario with cursor tracking")
    void testComplexScenario() {
        // Start with empty document, cursor at 0
        assertEquals(0, document.getCursorPosition());

        // Insert "Hello"
        document.insert(-1, "Hello", true);
        assertEquals("Hello", buffer.toString());
        assertEquals(5, document.getCursorPosition());

        // Move cursor to beginning
        document.setCursorPosition(0);
        assertEquals(0, document.getCursorPosition());

        // Insert at cursor
        document.insert(-1, ">> ", true);
        assertEquals(">> Hello", buffer.toString());
        assertEquals(3, document.getCursorPosition());

        // Move cursor to end
        document.moveCursor(10);
        assertEquals(8, document.getCursorPosition());

        // Insert at end
        document.insert(-1, " World", true);
        assertEquals(">> Hello World", buffer.toString());
        assertEquals(14, document.getCursorPosition());

        // Delete middle part
        document.delete(3, 8, true);
        assertEquals(">>  World", buffer.toString());
        assertEquals(3, document.getCursorPosition());
    }

    @Test
    @DisplayName("Multiple insertions maintain cursor correctly")
    void testMultipleInsertions() {
        document.insert(0, "AAA", false);
        document.setCursorPosition(1);

        // Insert after cursor
        document.insert(3, "BBB", false);
        assertEquals(1, document.getCursorPosition());
        assertEquals("AAABBB", buffer.toString());

        // Insert before cursor
        document.insert(0, "CCC", false);
        assertEquals(4, document.getCursorPosition()); // Adjusted from 1 to 4
        assertEquals("CCCAAABBB", buffer.toString());

        // Insert at cursor (position 4 is between 'A' at index 3 and 'A' at index 4)
        document.insert(-1, "DDD", true);
        assertEquals(7, document.getCursorPosition());
        assertEquals("CCCADDDAABBB", buffer.toString()); // DDD inserted at position 4
    }

    @Test
    @DisplayName("Cursor adjustment after sequential deletes")
    void testSequentialDeletes() {
        document.insert(0, "ABCDEFGHIJ", false);
        document.setCursorPosition(9); // At 'J'

        // Delete before cursor
        document.delete(0, 3, false); // Remove "ABC"
        assertEquals(6, document.getCursorPosition()); // Adjusted from 9 to 6
        assertEquals("DEFGHIJ", buffer.toString());

        // Delete including cursor
        document.delete(3, 7, true); // Remove "GHIJ"
        assertEquals(3, document.getCursorPosition());
        assertEquals("DEF", buffer.toString());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, true",
        "5, 5, true",
        "0, 5, false",
        "2, 7, false"
    })
    @DisplayName("Parameterized insert tests")
    void testParameterizedInsert(int initialCursor, int expectedCursor, boolean moveCursor) {
        document.insert(0, "Hello", false);
        document.setCursorPosition(initialCursor);
        document.insert(-1, "XX", moveCursor);

        if (moveCursor) {
            assertEquals(initialCursor + 2, document.getCursorPosition());
        }
    }

    // ========== ToString Test ==========

    @Test
    @DisplayName("toString returns descriptive string")
    void testToString() {
        String str = document.toString();
        assertTrue(str.contains("test-doc"));
        assertTrue(str.contains("cursor=0"));
        assertTrue(str.contains("length=0"));
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Document with initial content")
    void testDocumentWithInitialContent() {
        TextBuffer buf = new SimpleBuffer("Initial text");
        Document doc = new DocumentImpl("doc-1", buf);

        assertEquals("Initial text", doc.substring(0, doc.length()));
        assertEquals(12, doc.getCursorPosition());
        assertEquals(12, doc.length());
    }

    @Test
    @DisplayName("Unicode text handling")
    void testUnicodeHandling() {
        document.insert(0, "Hello 🌍", false);
        assertEquals(8, document.length()); // Emoji counts as 2 chars

        document.setCursorPosition(6);
        document.insert(-1, "World", true);
        assertEquals("Hello World🌍", buffer.toString());
        assertEquals(11, document.getCursorPosition());
    }

    @Test
    @DisplayName("Cursor remains valid after all operations")
    void testCursorAlwaysValid() {
        // This test verifies cursor never goes out of bounds
        document.insert(0, "Test", true);
        assertTrue(document.getCursorPosition() >= 0);
        assertTrue(document.getCursorPosition() <= document.length());

        document.delete(0, 2, false);
        assertTrue(document.getCursorPosition() >= 0);
        assertTrue(document.getCursorPosition() <= document.length());

        document.setCursorPosition(0);
        document.moveCursor(-100);
        assertTrue(document.getCursorPosition() >= 0);

        document.moveCursor(100);
        assertTrue(document.getCursorPosition() <= document.length());
    }
}
