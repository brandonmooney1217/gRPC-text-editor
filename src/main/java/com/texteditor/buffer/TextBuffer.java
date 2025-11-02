package com.texteditor.buffer;

/**
 * Interface for text buffer implementations.
 * All positions are 0-indexed.
 * Implementations must be thread-safe.
 */
public interface TextBuffer {

    /**
     * Inserts text at the specified position.
     * @param position Position to insert at (0-based)
     * @param text Text to insert
     * @throws IndexOutOfBoundsException if position is invalid (position < 0 or position > length())
     * @throws IllegalArgumentException if text is null
     */
    void insert(int position, String text);

    /**
     * Deletes text in the range [start, end).
     * @param start Start position (inclusive)
     * @param end End position (exclusive)
     * @throws IndexOutOfBoundsException if range is invalid
     */
    void delete(int start, int end);

    /**
     * Returns substring in the range [start, end).
     * @param start Start position (inclusive)
     * @param end End position (exclusive)
     * @return Substring
     * @throws IndexOutOfBoundsException if range is invalid
     */
    String substring(int start, int end);

    /**
     * Returns the current length of the buffer.
     * @return Length in characters
     */
    int length();

    /**
     * Returns the entire buffer contents.
     * @return Full text
     */
    String toString();

    /**
     * Returns the type/name of this buffer implementation.
     * @return Buffer type identifier
     */
    String getType();

    /**
     * Clears all content from the buffer.
     */
    void clear();
}
