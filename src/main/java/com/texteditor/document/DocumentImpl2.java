package com.texteditor.document;

import com.texteditor.buffer.TextBuffer;

public class DocumentImpl2 implements Document {

    private final String documentId;
    private final TextBuffer buffer;
    private int cursorPosition;

    public DocumentImpl2(final String documentId, final TextBuffer buffer) {
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null");
        }

        this.documentId = documentId;
        this.buffer = buffer;
        // initially set the pos to end of text buffer
        this.cursorPosition = buffer.length();
    }

    @Override
    public String getId() {
        return this.documentId;
    }

    @Override
    public TextBuffer getBuffer() {
        return this.buffer;
    }

    @Override
    public int getCursorPosition() {
        return this.cursorPosition;
    }

    @Override
    public void setCursorPosition(int position) {
        if (position < 0 || position > buffer.length()) {
            throw new IndexOutOfBoundsException(
                String.format("Cursor position %d is out of bounds (length: %d)",
                    position, buffer.length())
            );
        }
        this.cursorPosition = position;
    }

    @Override
    public void moveCursor(int offset) {
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
    public void insert(int position, String text, boolean moveCursor) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

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

    }

    @Override
    public void delete(int start, int end, boolean moveCursor) {
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

        // delete operation
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
        }
    }

    @Override
    public String substring(int start, int end) {
        return buffer.substring(start, end);
    }

    @Override
    public int length() {
        return buffer.length();
    }
    
}
