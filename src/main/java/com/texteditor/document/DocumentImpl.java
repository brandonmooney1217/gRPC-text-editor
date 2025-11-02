package com.texteditor.document;

import com.texteditor.buffer.TextBuffer;

/**
 * Implementation of the Document interface with cursor tracking.
 *
 * <p>This class wraps a TextBuffer and adds cursor management functionality.
 * All text operations are delegated to the underlying buffer, while cursor
 * position is tracked and maintained by this class.
 *
 * <p>Thread Safety: This implementation is thread-safe. All public methods
 * are synchronized to ensure consistent state when accessed by multiple threads.
 *
 * <p>Cursor Management:
 * <ul>
 *   <li>Cursor is always kept in valid range: 0 <= cursor <= length()</li>
 *   <li>After insert: cursor adjusts based on moveCursor flag</li>
 *   <li>After delete: cursor adjusts to stay valid</li>
 *   <li>Position -1 in operations means "use current cursor position"</li>
 * </ul>
 */
public class DocumentImpl implements Document {

    private final String documentId;
    private final TextBuffer buffer;
    private int cursorPosition;

    /**
     * Creates a new document with the specified ID and buffer.
     *
     * @param documentId Unique identifier for this document (must not be null or empty)
     * @param buffer Text buffer to use for storing document content (must not be null)
     * @throws IllegalArgumentException if documentId is null/empty or buffer is null
     */
    public DocumentImpl(String documentId, TextBuffer buffer) {
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null");
        }

        this.documentId = documentId;
        this.buffer = buffer;
        this.cursorPosition = buffer.length(); // Initialize cursor at end
    }

    @Override
    public synchronized String getId() {
        return documentId;
    }

    @Override
    public synchronized TextBuffer getBuffer() {
        return buffer;
    }

    @Override
    public synchronized int getCursorPosition() {
        return cursorPosition;
    }

    @Override
    public synchronized void setCursorPosition(int position) {
        if (position < 0 || position > buffer.length()) {
            throw new IndexOutOfBoundsException(
                String.format("Cursor position %d is out of bounds (length: %d)",
                    position, buffer.length())
            );
        }
        this.cursorPosition = position;
    }

    @Override
    public synchronized void moveCursor(int offset) {
        int newPosition = cursorPosition + offset;

        // Clamp to valid range [0, length()]
        if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition > buffer.length()) {
            newPosition = buffer.length();
        }

        this.cursorPosition = newPosition;
    }

    @Override
    public synchronized void insert(int position, String text, boolean moveCursor) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        // Handle position -1 (use cursor position)
        int insertPosition;
        if (position == -1) {
            insertPosition = cursorPosition;
        } else {
            if (position < 0 || position > buffer.length()) {
                throw new IndexOutOfBoundsException(
                    String.format("Insert position %d is out of bounds (length: %d)",
                        position, buffer.length())
                );
            }
            insertPosition = position;
        }

        // Perform the insertion
        buffer.insert(insertPosition, text);

        // Update cursor position
        if (moveCursor) {
            // Move cursor to end of inserted text
            this.cursorPosition = insertPosition + text.length();
        } else {
            // If cursor was after insertion point, adjust it
            if (cursorPosition >= insertPosition) {
                this.cursorPosition += text.length();
            }
            // Otherwise cursor stays where it was
        }

        // Ensure cursor is still valid
        ensureCursorValid();
    }

    @Override
    public synchronized void delete(int start, int end, boolean moveCursor) {
        // Validate range (buffer.delete will also validate, but we need to check for cursor adjustment)
        if (start < 0) {
            throw new IndexOutOfBoundsException(
                String.format("Start position %d cannot be negative", start)
            );
        }
        if (end > buffer.length()) {
            throw new IndexOutOfBoundsException(
                String.format("End position %d is out of bounds (length: %d)",
                    end, buffer.length())
            );
        }
        if (start > end) {
            throw new IndexOutOfBoundsException(
                String.format("Start position %d cannot be greater than end position %d",
                    start, end)
            );
        }

        // Perform the deletion
        buffer.delete(start, end);

        int deletedLength = end - start;

        // Update cursor position
        if (moveCursor) {
            // Move cursor to start of deletion
            this.cursorPosition = start;
        } else {
            // Adjust cursor based on where it was relative to deleted range
            if (cursorPosition >= end) {
                // Cursor was after deleted range, shift it back
                this.cursorPosition -= deletedLength;
            } else if (cursorPosition > start) {
                // Cursor was inside deleted range, move to start
                this.cursorPosition = start;
            }
            // If cursor was before start, it stays the same
        }

        // Ensure cursor is still valid
        ensureCursorValid();
    }

    @Override
    public synchronized String substring(int start, int end) {
        return buffer.substring(start, end);
    }

    @Override
    public synchronized int length() {
        return buffer.length();
    }

    /**
     * Ensures the cursor position is within valid bounds.
     * Clamps cursor to [0, length()] range.
     */
    private void ensureCursorValid() {
        if (cursorPosition < 0) {
            cursorPosition = 0;
        } else if (cursorPosition > buffer.length()) {
            cursorPosition = buffer.length();
        }
    }

    @Override
    public synchronized String toString() {
        return String.format("Document[id=%s, length=%d, cursor=%d, buffer=%s]",
            documentId, buffer.length(), cursorPosition, buffer.getType());
    }
}
