package com.texteditor.exception;

/**
 * Exception thrown when attempting to access a document that doesn't exist.
 *
 * <p>This is a checked exception that forces callers to handle the case
 * where a requested document ID is not found in the DocumentManager.
 *
 * <p>Common scenarios:
 * <ul>
 *   <li>Client requests a document with an invalid ID</li>
 *   <li>Document was already closed</li>
 *   <li>Typo in document ID</li>
 * </ul>
 */
public class DocumentNotFoundException extends Exception {

    private final String documentId;

    /**
     * Creates a new DocumentNotFoundException.
     *
     * @param documentId The ID of the document that was not found
     */
    public DocumentNotFoundException(String documentId) {
        super("Document not found: " + documentId);
        this.documentId = documentId;
    }

    /**
     * Gets the document ID that was not found.
     *
     * @return The document ID
     */
    public String getDocumentId() {
        return documentId;
    }
}
