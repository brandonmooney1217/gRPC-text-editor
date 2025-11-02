package com.texteditor.buffer;

/**
 * Simple text buffer implementation using StringBuilder.
 * This implementation has O(n) complexity for insert and delete operations.
 * Suitable for small documents and learning purposes.
 *
 * Thread-safety: Uses synchronized methods for thread-safe operations.
 */
public class SimpleBuffer implements TextBuffer {

    private final StringBuilder buffer;
    private static final String TYPE = "simple";

    /**
     * Creates an empty SimpleBuffer.
     */
    public SimpleBuffer() {
        this.buffer = new StringBuilder();
    }

    /**
     * Creates a SimpleBuffer with initial content.
     * @param initialContent Initial text content
     */
    public SimpleBuffer(String initialContent) {
        if (initialContent == null) {
            throw new IllegalArgumentException("Initial content cannot be null");
        }
        this.buffer = new StringBuilder(initialContent);
    }

    @Override
    public synchronized void insert(int position, String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (position < 0 || position > buffer.length()) {
            throw new IndexOutOfBoundsException(
                String.format("Position %d is out of bounds (length: %d)", position, buffer.length())
            );
        }
        buffer.insert(position, text);
    }

    @Override
    public synchronized void delete(int start, int end) {
        validateRange(start, end);
        buffer.delete(start, end);
    }

    @Override
    public synchronized String substring(int start, int end) {
        validateRange(start, end);
        return buffer.substring(start, end);
    }

    @Override
    public synchronized int length() {
        return buffer.length();
    }

    @Override
    public synchronized String toString() {
        return buffer.toString();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public synchronized void clear() {
        buffer.setLength(0);
    }

    /**
     * Validates that the range [start, end) is valid.
     * @param start Start position (inclusive)
     * @param end End position (exclusive)
     * @throws IndexOutOfBoundsException if range is invalid
     */
    private void validateRange(int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException(
                String.format("Start position %d cannot be negative", start)
            );
        }
        if (end > buffer.length()) {
            throw new IndexOutOfBoundsException(
                String.format("End position %d is out of bounds (length: %d)", end, buffer.length())
            );
        }
        if (start > end) {
            throw new IndexOutOfBoundsException(
                String.format("Start position %d cannot be greater than end position %d", start, end)
            );
        }
    }
}
