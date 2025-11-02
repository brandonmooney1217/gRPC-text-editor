package com.texteditor.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.texteditor.buffer.SimpleBuffer;
import com.texteditor.buffer.TextBuffer;
import com.texteditor.document.Document;
import com.texteditor.document.DocumentImpl2;
import com.texteditor.exception.DocumentNotFoundException;

public class DocumentManager2 {

    private final ConcurrentHashMap<String, Document> documents;

    private final AtomicLong documentCounter;

    public DocumentManager2 () {
        this.documents = new ConcurrentHashMap<>();
        this.documentCounter = new AtomicLong(1);
    }

    public Document createDocument(final String initialContent, final String documentId) {
        String id = documentId;
        if (id == null || id.isEmpty()) {
            id = generateDocumentId();
        }

        if (documents.containsKey(id)) {
            throw new IllegalArgumentException(
                "Document with ID '" + id + "' already exists"
            );
        }

        String content = initialContent;
        if (content == null) {
            content = "";
        }

        TextBuffer buffer = new SimpleBuffer(initialContent);
        final Document document = new DocumentImpl2(documentId, buffer);

        return document;
    }

    public void closeDocument(final String documentId) throws DocumentNotFoundException {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        Document removedDocument = documents.remove(documentId);

        if (removedDocument == null) {
            throw new DocumentNotFoundException(documentId);
        }
    }

    public Document getDocument(final String documentId) throws DocumentNotFoundException {
        if (documentId == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        Document document = documents.get(documentId);

        if (document == null) {
            throw new DocumentNotFoundException(documentId);
        }

        return document;
    }

    public boolean hasDocument(final String documentId) {
        if (documentId == null) {
            return false;
        }
        return documents.containsKey(documentId);
    }

    private String generateDocumentId() {
        return "doc-" + documentCounter.getAndIncrement();
    } 
}
