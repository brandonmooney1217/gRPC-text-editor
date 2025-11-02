package com.texteditor.manager;

import com.texteditor.buffer.SimpleBuffer;
import com.texteditor.buffer.TextBuffer;
import com.texteditor.document.Document;
import com.texteditor.document.DocumentImpl;
import com.texteditor.exception.DocumentNotFoundException;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages multiple document sessions.
 *
 * <p>The DocumentManager is responsible for:
 * <ul>
 *   <li>Creating and managing multiple documents simultaneously</li>
 *   <li>Document lifecycle management (create, retrieve, close)</li>
 *   <li>Auto-generating unique document IDs when not provided</li>
 *   <li>Creating appropriate buffer types based on client requests</li>
 *   <li>Routing operations to the correct document by ID</li>
 * </ul>
 *
 * <p>Thread Safety: This class is thread-safe. Multiple threads can
 * concurrently create, access, and close documents without external
 * synchronization. Thread safety is achieved using ConcurrentHashMap
 * for document storage and AtomicLong for ID generation.
 *
 * <p>Usage Example:
 * <pre>{@code
 * DocumentManager manager = new DocumentManager();
 *
 * // Create document with auto-generated ID
 * Document doc1 = manager.createDocument(null, "Hello", "simple");
 * System.out.println(doc1.getId());  // "doc-1"
 *
 * // Create document with explicit ID
 * Document doc2 = manager.createDocument("my-doc", "World", "simple");
 *
 * // Retrieve document
 * Document retrieved = manager.getDocument("my-doc");
 *
 * // Close document
 * manager.closeDocument("my-doc");
 * }</pre>
 */
public class DocumentManager {

    /**
     * Storage for all active documents.
     * ConcurrentHashMap provides thread-safe access without external locking.
     */
    private final ConcurrentHashMap<String, Document> documents;

    /**
     * Counter for auto-generating sequential document IDs.
     * AtomicLong provides thread-safe increment operations.
     */
    private final AtomicLong documentCounter;

    /**
     * Creates a new DocumentManager with no documents.
     */
    public DocumentManager() {
        this.documents = new ConcurrentHashMap<>();
        this.documentCounter = new AtomicLong(1); // Start at 1 for "doc-1", "doc-2", etc.
    }

    /**
     * Creates a new document with the specified parameters.
     *
     * <p>If the document ID is null or empty, a unique ID will be auto-generated
     * in the format "doc-N" where N is a sequential number.
     *
     * <p>Implementation Note: Currently uses SimpleBuffer for all documents.
     * The TextBuffer interface allows swapping to different implementations
     * (RopeBuffer, PieceTableBuffer) in the future without changing this API.
     *
     * @param documentId Document ID (if null or empty, auto-generates one)
     * @param initialContent Initial text content (use empty string for empty document)
     * @return The created Document
     * @throws IllegalArgumentException if documentId already exists
     */
    public Document createDocument(String documentId, String initialContent) {
        // Handle document ID (generate if not provided)
        String id = documentId;
        if (id == null || id.isEmpty()) {
            id = generateDocumentId();
        }

        // Check for duplicate ID
        if (documents.containsKey(id)) {
            throw new IllegalArgumentException(
                "Document with ID '" + id + "' already exists"
            );
        }

        // Ensure initial content is not null
        if (initialContent == null) {
            initialContent = "";
        }

        // Create buffer with initial content
        // Currently using SimpleBuffer - can be changed to RopeBuffer/PieceTableBuffer later
        TextBuffer buffer = new SimpleBuffer(initialContent);

        // Create document with buffer
        Document document = new DocumentImpl(id, buffer);

        // Store in map
        documents.put(id, document);

        // Return the created document
        return document;
    }

    /**
     * Gets an existing document by ID.
     *
     * @param documentId Document ID to retrieve (must not be null)
     * @return The document
     * @throws DocumentNotFoundException if document doesn't exist
     * @throws IllegalArgumentException if documentId is null
     */
    public Document getDocument(String documentId) throws DocumentNotFoundException {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        Document document = documents.get(documentId);

        if (document == null) {
            throw new DocumentNotFoundException(documentId);
        }

        return document;
    }

    /**
     * Closes and removes a document.
     *
     * <p>After closing, the document is removed from the manager and can no longer
     * be accessed. The document ID can be reused for a new document. The document
     * and its buffer will be garbage collected if no other references exist.
     *
     * @param documentId Document ID to close (must not be null)
     * @throws DocumentNotFoundException if document doesn't exist
     * @throws IllegalArgumentException if documentId is null
     */
    public void closeDocument(String documentId) throws DocumentNotFoundException {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }

        Document removed = documents.remove(documentId);

        if (removed == null) {
            throw new DocumentNotFoundException(documentId);
        }

        // Document is now removed from map and can be garbage collected
    }

    /**
     * Checks if a document exists.
     *
     * @param documentId Document ID to check
     * @return true if document exists, false otherwise
     */
    public boolean hasDocument(String documentId) {
        if (documentId == null) {
            return false;
        }
        return documents.containsKey(documentId);
    }

    /**
     * Gets all document IDs currently managed.
     *
     * <p>Returns an unmodifiable view of the document IDs. The returned set
     * is a snapshot and will not reflect subsequent changes to the manager.
     *
     * @return Unmodifiable set of document IDs (never null, may be empty)
     */
    public Set<String> getDocumentIds() {
        return Collections.unmodifiableSet(documents.keySet());
    }

    /**
     * Gets the number of documents currently managed.
     *
     * @return Number of active documents
     */
    public int getDocumentCount() {
        return documents.size();
    }

    /**
     * Generates a unique document ID.
     *
     * <p>Generated IDs are in the format "doc-N" where N is a sequential number
     * starting from 1. The counter is thread-safe and will never generate
     * duplicate IDs.
     *
     * @return Generated document ID
     */
    private String generateDocumentId() {
        return "doc-" + documentCounter.getAndIncrement();
    }
}
