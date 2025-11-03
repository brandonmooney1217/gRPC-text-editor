package com.texteditor.factory;

import com.texteditor.document.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentFactory.
 */
@DisplayName("DocumentFactory Tests")
class DocumentFactoryTest {

    private DocumentFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DocumentFactory();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Constructor creates factory")
    void testConstructor() {
        DocumentFactory f = new DocumentFactory();
        assertNotNull(f);
    }

    // ========== Create Document Tests ==========

    @Test
    @DisplayName("Create document with valid parameters")
    void testCreateDocumentValid() {
        Document doc = factory.createDocument("test-doc", "Hello World");

        assertNotNull(doc);
        assertEquals("test-doc", doc.getId());
        assertEquals("Hello World", doc.getBuffer().toString());
        assertEquals(11, doc.length());
    }

    @Test
    @DisplayName("Create document with empty content")
    void testCreateDocumentEmptyContent() {
        Document doc = factory.createDocument("doc-1", "");

        assertEquals("doc-1", doc.getId());
        assertEquals("", doc.getBuffer().toString());
        assertEquals(0, doc.length());
    }

    @Test
    @DisplayName("Create document with null content defaults to empty")
    void testCreateDocumentNullContent() {
        Document doc = factory.createDocument("doc-1", null);

        assertEquals("doc-1", doc.getId());
        assertEquals("", doc.getBuffer().toString());
        assertEquals(0, doc.length());
    }

    @Test
    @DisplayName("Create document with simple buffer type")
    void testCreateDocumentUsesSimpleBuffer() {
        Document doc = factory.createDocument("doc-1", "test");

        assertEquals("simple", doc.getBuffer().getType());
    }

    @Test
    @DisplayName("Create document rejects null ID")
    void testCreateDocumentRejectsNullId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createDocument(null, "content")
        );

        assertTrue(exception.getMessage().toLowerCase().contains("id"));
        assertTrue(exception.getMessage().toLowerCase().contains("null"));
    }

    @Test
    @DisplayName("Create document rejects empty ID")
    void testCreateDocumentRejectsEmptyId() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createDocument("", "content")
        );

        assertTrue(exception.getMessage().toLowerCase().contains("id"));
    }

    @Test
    @DisplayName("Create multiple documents with same factory")
    void testCreateMultipleDocuments() {
        Document doc1 = factory.createDocument("doc-1", "First");
        Document doc2 = factory.createDocument("doc-2", "Second");
        Document doc3 = factory.createDocument("doc-3", "Third");

        assertEquals("doc-1", doc1.getId());
        assertEquals("doc-2", doc2.getId());
        assertEquals("doc-3", doc3.getId());

        assertEquals("First", doc1.getBuffer().toString());
        assertEquals("Second", doc2.getBuffer().toString());
        assertEquals("Third", doc3.getBuffer().toString());
    }

    @Test
    @DisplayName("Created documents are independent")
    void testCreatedDocumentsIndependent() {
        Document doc1 = factory.createDocument("doc-1", "Content 1");
        Document doc2 = factory.createDocument("doc-2", "Content 2");

        // Modify one document
        doc1.insert(9, " Modified", false);

        // Other document should be unaffected
        assertEquals("Content 1 Modified", doc1.getBuffer().toString());
        assertEquals("Content 2", doc2.getBuffer().toString());
    }

    @Test
    @DisplayName("Created documents have cursor at end")
    void testCreatedDocumentsCursorAtEnd() {
        Document doc1 = factory.createDocument("doc-1", "Hello");
        Document doc2 = factory.createDocument("doc-2", "World");

        assertEquals(5, doc1.getCursorPosition());
        assertEquals(5, doc2.getCursorPosition());
    }

    @Test
    @DisplayName("Create document with long content")
    void testCreateDocumentWithLongContent() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("Line ").append(i).append("\n");
        }

        Document doc = factory.createDocument("large-doc", longContent.toString());

        assertEquals("large-doc", doc.getId());
        assertTrue(doc.length() > 5000);
        assertEquals(longContent.toString(), doc.getBuffer().toString());
    }

    @Test
    @DisplayName("Create document with Unicode content")
    void testCreateDocumentWithUnicode() {
        Document doc = factory.createDocument("unicode-doc", "Hello 🌍 世界");

        assertEquals("unicode-doc", doc.getId());
        assertEquals("Hello 🌍 世界", doc.getBuffer().toString());
    }

    @Test
    @DisplayName("Create document with various ID formats")
    void testCreateDocumentVariousIdFormats() {
        // Simple IDs
        Document doc1 = factory.createDocument("a", "test");
        assertEquals("a", doc1.getId());

        // Numeric IDs
        Document doc2 = factory.createDocument("123", "test");
        assertEquals("123", doc2.getId());

        // UUID-like IDs
        Document doc3 = factory.createDocument("550e8400-e29b-41d4-a716-446655440000", "test");
        assertEquals("550e8400-e29b-41d4-a716-446655440000", doc3.getId());

        // Path-like IDs
        Document doc4 = factory.createDocument("user/docs/essay.txt", "test");
        assertEquals("user/docs/essay.txt", doc4.getId());

        // Special characters
        Document doc5 = factory.createDocument("my-doc_v2.0", "test");
        assertEquals("my-doc_v2.0", doc5.getId());
    }

    // ========== Thread Safety Tests ==========

    @Test
    @DisplayName("Factory is thread-safe for concurrent creation")
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        Document[] documents = new Document[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                documents[index] = factory.createDocument("thread-doc-" + index, "Content " + index);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all documents were created correctly
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(documents[i]);
            assertEquals("thread-doc-" + i, documents[i].getId());
            assertEquals("Content " + i, documents[i].getBuffer().toString());
        }
    }
}
