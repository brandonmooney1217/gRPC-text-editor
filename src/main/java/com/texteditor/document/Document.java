package com.texteditor.document;

import com.texteditor.buffer.TextBuffer;

/**
 * Represents a text document with cursor management.
 *
 * <p>A Document wraps a TextBuffer and provides additional functionality:
 * <ul>
 *   <li>Cursor position tracking and management</li>
 *   <li>Document identification</li>
 *   <li>Text operations that can optionally move the cursor</li>
 * </ul>
 *
 * <p>Position Conventions:
 * <ul>
 *   <li>All positions are 0-indexed</li>
 *   <li>Ranges are [start, end) - start inclusive, end exclusive</li>
 *   <li>Position -1 means "use current cursor position"</li>
 *   <li>Cursor position is always valid: 0 <= cursor <= length</li>
 * </ul>
 *
 * <p>Cursor Behavior:
 * <ul>
 *   <li>After insert at cursor: cursor moves to end of inserted text (if moveCursor=true)</li>
 *   <li>After delete: cursor moves to start of deletion range (if moveCursor=true)</li>
 *   <li>When moveCursor=false, cursor remains unchanged</li>
 * </ul>
 *
 * <p>Implementations must be thread-safe if accessed by multiple threads.
 */
public interface Document {

    /**
     * Gets the document ID.
     *
     * @return Unique identifier for this document
     */
    String getId();

    /**
     * Gets the underlying text buffer.
     *
     * @return The TextBuffer implementation backing this document
     */
    TextBuffer getBuffer();

    /**
     * Gets the current cursor position.
     *
     * <p>The cursor position is always valid: 0 <= cursor <= length().
     * A cursor at position length() means it's at the end of the document.
     *
     * @return Current cursor position (0-indexed)
     */
    int getCursorPosition();

    /**
     * Sets the cursor position to an absolute position.
     *
     * @param position New cursor position (0-indexed), must be 0 <= position <= length()
     * @throws IndexOutOfBoundsException if position is negative or greater than document length
     */
    void setCursorPosition(int position);

    /**
     * Moves the cursor by a relative offset.
     *
     * <p>Positive offset moves cursor forward (right), negative moves backward (left).
     * The resulting position is clamped to valid range [0, length()].
     *
     * @param offset Number of positions to move (positive or negative)
     */
    void moveCursor(int offset);

    /**
     * Inserts text at the specified position.
     *
     * <p>If position is -1, the text is inserted at the current cursor position.
     *
     * @param position Position to insert at (-1 for cursor position, or 0 <= position <= length())
     * @param text Text to insert (must not be null)
     * @param moveCursor If true, moves cursor to end of inserted text; if false, cursor stays at original position
     * @throws IndexOutOfBoundsException if position is invalid (< -1 or > length())
     * @throws IllegalArgumentException if text is null
     */
    void insert(int position, String text, boolean moveCursor);

    /**
     * Deletes text in the range [start, end).
     *
     * @param start Start position (inclusive), must be 0 <= start < end
     * @param end End position (exclusive), must be start < end <= length()
     * @param moveCursor If true, moves cursor to start position; if false, cursor stays at original position
     *                   (or is adjusted if deletion affects cursor position)
     * @throws IndexOutOfBoundsException if range is invalid
     */
    void delete(int start, int end, boolean moveCursor);

    /**
     * Gets substring in the range [start, end).
     *
     * @param start Start position (inclusive)
     * @param end End position (exclusive)
     * @return Substring from start to end
     * @throws IndexOutOfBoundsException if range is invalid
     */
    String substring(int start, int end);

    /**
     * Gets the current document length in characters.
     *
     * @return Length of the document
     */
    int length();
}
