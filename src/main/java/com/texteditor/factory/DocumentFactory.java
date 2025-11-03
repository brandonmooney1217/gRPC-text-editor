package com.texteditor.factory;

import com.texteditor.buffer.SimpleBuffer;
import com.texteditor.buffer.TextBuffer;
import com.texteditor.document.Document;
import com.texteditor.document.DocumentImpl;

/**
 * Factory for creating Document instances with appropriate buffer implementations.
 *
 * <p>The DocumentFactory encapsulates the creation logic for documents,
 * separating the concern of "how to create" from "where to store" (DocumentManager).
 *
 * <p>Design Benefits:
 * <ul>
 *   <li>Single Responsibility: Focuses only on document creation</li>
 *   <li>Testability: Creation logic can be tested independently</li>
 *   <li>Extensibility: Easy to add different buffer selection strategies</li>
 *   <li>Dependency Injection: Can inject different factory implementations</li>
 * </ul>
 *
 * <p>Current Implementation: Uses SimpleBuffer for all documents.
 * Future implementations could:
 * <ul>
 *   <li>Select buffer based on document size (SimpleBuffer vs RopeBuffer)</li>
 *   <li>Use different buffers based on configuration</li>
 *   <li>Support custom buffer implementations</li>
 * </ul>
 *
 * <p>Thread Safety: This class is stateless and thread-safe.
 * Multiple threads can safely call createDocument() concurrently.
 */
public class DocumentFactory {

    /**
     * Creates a new DocumentFactory.
     *
     * <p>The factory is stateless and can be shared across multiple threads.
     */
    public DocumentFactory() {
        // Stateless - no initialization needed
    }

    /**
     * Creates a new document with the specified ID and initial content.
     *
     * <p>The factory creates a Document with an appropriate TextBuffer implementation.
     * Currently uses SimpleBuffer for all documents, but this can be extended
     * in the future to select different buffer types based on requirements.
     *
     * @param documentId Unique identifier for the document (must not be null or empty)
     * @param initialContent Initial text content (if null, treated as empty string)
     * @return A new Document instance
     * @throws IllegalArgumentException if documentId is null or empty
     */
    public Document createDocument(String documentId, String initialContent) {
        // Validate document ID
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }

        // Normalize initial content (null → empty string)
        String content = (initialContent != null) ? initialContent : "";

        // Create buffer with initial content
        // Currently using SimpleBuffer - can be extended to use different buffer types
        TextBuffer buffer = createBuffer(content);

        // Create and return document
        return new DocumentImpl(documentId, buffer);
    }

    /**
     * Creates a TextBuffer with the specified initial content.
     *
     * <p>This method encapsulates the buffer creation logic, making it easy
     * to change buffer implementation in the future.
     *
     * <p>Current Strategy: Always uses SimpleBuffer.
     *
     * <p>Future Strategies (examples):
     * <pre>{@code
     * // Size-based selection
     * if (content.length() > 10000) {
     *     return new RopeBuffer(content);  // Large documents
     * } else {
     *     return new SimpleBuffer(content); // Small documents
     * }
     *
     * // Configuration-based
     * if (config.getBufferType().equals("rope")) {
     *     return new RopeBuffer(content);
     * }
     * }</pre>
     *
     * @param content Initial text content for the buffer
     * @return A new TextBuffer instance
     */
    private TextBuffer createBuffer(String content) {
        // Currently: always use SimpleBuffer
        // Future: can add logic to select different buffer implementations
        return new SimpleBuffer(content);
    }
}
