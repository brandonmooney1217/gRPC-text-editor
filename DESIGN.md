# gRPC Text Editor Service - Design Document

## 1. Project Overview

A distributed text editor service implemented using gRPC in Java. The service provides core text editing operations (insert, delete, cursor movement, substring retrieval) with a pluggable text buffer implementation.

### Goals
- Learn gRPC service implementation in Java
- Understand text editor fundamentals
- Design flexible, interface-based architecture
- Support multiple text buffer data structures (Rope, Gap Buffer, Piece Table)

## 2. System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    gRPC Client                          │
│              (CLI, GUI, or Test Client)                 │
└─────────────────────┬───────────────────────────────────┘
                      │ gRPC calls
                      │
┌─────────────────────▼───────────────────────────────────┐
│              TextEditorService (gRPC)                   │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Service Implementation (TextEditorServiceImpl)  │  │
│  └─────────────────────┬────────────────────────────┘  │
│                        │                                │
│  ┌─────────────────────▼────────────────────────────┐  │
│  │         DocumentManager                          │  │
│  │  - Manages multiple document sessions            │  │
│  │  - Document lifecycle (create, close)            │  │
│  │  - Session management                            │  │
│  └─────────────────────┬────────────────────────────┘  │
│                        │                                │
│  ┌─────────────────────▼────────────────────────────┐  │
│  │           Document                               │  │
│  │  - Cursor position tracking                      │  │
│  │  - Delegates to TextBuffer                       │  │
│  └─────────────────────┬────────────────────────────┘  │
│                        │                                │
│  ┌─────────────────────▼────────────────────────────┐  │
│  │     TextBuffer (Interface)                       │  │
│  │  - insert(position, text)                        │  │
│  │  - delete(start, end)                            │  │
│  │  - substring(start, end)                         │  │
│  │  - length()                                      │  │
│  └────────┬──────────────┬─────────────┬────────────┘  │
│           │              │             │                │
│  ┌────────▼──────┐ ┌────▼─────┐ ┌────▼──────────┐    │
│  │ SimpleBuffer  │ │RopeBuffer│ │PieceTableBuf. │    │
│  │(StringBuilder)│ │          │ │               │    │
│  └───────────────┘ └──────────┘ └───────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## 3. gRPC Service Definition

### 3.1 Protocol Buffer Schema (`text_editor.proto`)

```protobuf
syntax = "proto3";

package texteditor;

option java_multiple_files = true;
option java_package = "com.texteditor.grpc";
option java_outer_classname = "TextEditorProto";

// Main service definition
service TextEditorService {
  // Document lifecycle
  rpc CreateDocument(CreateDocumentRequest) returns (CreateDocumentResponse);
  rpc CloseDocument(CloseDocumentRequest) returns (CloseDocumentResponse);

  // Text operations
  rpc Insert(InsertRequest) returns (OperationResponse);
  rpc Delete(DeleteRequest) returns (OperationResponse);

  // Cursor operations
  rpc MoveCursor(MoveCursorRequest) returns (CursorResponse);
  rpc GetCursorPosition(GetCursorPositionRequest) returns (CursorResponse);

  // Query operations
  rpc GetSubstring(GetSubstringRequest) returns (GetSubstringResponse);
  rpc GetDocumentInfo(GetDocumentInfoRequest) returns (GetDocumentInfoResponse);

  // Streaming support (optional future enhancement)
  rpc StreamChanges(stream ChangeRequest) returns (stream ChangeResponse);
}

// Request/Response messages
message CreateDocumentRequest {
  string document_id = 1;  // Optional, server generates if empty
  string initial_content = 2;
  string buffer_type = 3;  // "simple", "rope", "piecetable"
}

message CreateDocumentResponse {
  string document_id = 1;
  bool success = 2;
  string message = 3;
}

message CloseDocumentRequest {
  string document_id = 1;
}

message CloseDocumentResponse {
  bool success = 1;
  string message = 2;
}

message InsertRequest {
  string document_id = 1;
  int32 position = 2;      // Position to insert at (-1 for cursor position)
  string text = 3;
  bool move_cursor = 4;    // Move cursor after insertion
}

message DeleteRequest {
  string document_id = 1;
  int32 start_position = 2;  // Start of deletion range
  int32 end_position = 3;    // End of deletion range
  bool move_cursor = 4;      // Move cursor to start after deletion
}

message OperationResponse {
  bool success = 1;
  string message = 2;
  int32 cursor_position = 3;  // Current cursor position after operation
  int32 document_length = 4;   // Total length after operation
}

message MoveCursorRequest {
  string document_id = 1;
  oneof movement {
    int32 absolute_position = 2;  // Move to absolute position
    int32 relative_offset = 3;    // Move relative to current position
  }
}

message GetCursorPositionRequest {
  string document_id = 1;
}

message CursorResponse {
  bool success = 1;
  string message = 2;
  int32 cursor_position = 3;
  int32 document_length = 4;
}

message GetSubstringRequest {
  string document_id = 1;
  int32 start_position = 2;
  int32 end_position = 3;     // -1 for end of document
}

message GetSubstringResponse {
  bool success = 1;
  string message = 2;
  string text = 3;
  int32 actual_length = 4;
}

message GetDocumentInfoRequest {
  string document_id = 1;
}

message GetDocumentInfoResponse {
  bool success = 1;
  string message = 2;
  string document_id = 3;
  int32 length = 4;
  int32 cursor_position = 5;
  string buffer_type = 6;
}

// For streaming (future enhancement)
message ChangeRequest {
  string document_id = 1;
  oneof operation {
    InsertRequest insert = 2;
    DeleteRequest delete = 3;
    MoveCursorRequest move_cursor = 4;
  }
}

message ChangeResponse {
  OperationResponse result = 1;
  int64 timestamp = 2;
}
```

## 4. Core Interfaces

### 4.1 TextBuffer Interface

```java
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
     * @throws IndexOutOfBoundsException if position is invalid
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
```

### 4.2 Document Interface

```java
package com.texteditor.document;

import com.texteditor.buffer.TextBuffer;

/**
 * Represents a text document with cursor management.
 */
public interface Document {
    /**
     * Gets the document ID.
     */
    String getId();

    /**
     * Gets the underlying text buffer.
     */
    TextBuffer getBuffer();

    /**
     * Gets the current cursor position.
     */
    int getCursorPosition();

    /**
     * Sets the cursor position (absolute).
     */
    void setCursorPosition(int position);

    /**
     * Moves the cursor by relative offset.
     */
    void moveCursor(int offset);

    /**
     * Inserts text at the specified position.
     * If position is -1, uses current cursor position.
     */
    void insert(int position, String text, boolean moveCursor);

    /**
     * Deletes text in range [start, end).
     */
    void delete(int start, int end, boolean moveCursor);

    /**
     * Gets substring in range [start, end).
     */
    String substring(int start, int end);

    /**
     * Gets document length.
     */
    int length();
}
```

## 5. Text Buffer Implementations

### 5.1 SimpleBuffer (StringBuilder-based)
- **Pros**: Simple to implement, good for small documents
- **Cons**: O(n) insert/delete operations
- **Use case**: Learning, small documents (<10KB)

### 5.2 RopeBuffer (Rope data structure)
- **Pros**: O(log n) insert/delete, efficient for large documents
- **Cons**: More complex, higher memory overhead
- **Use case**: Large documents, frequent edits in middle

### 5.3 PieceTableBuffer (Piece Table)
- **Pros**: O(1) undo/redo support, memory efficient
- **Cons**: More complex implementation
- **Use case**: Advanced text editors with undo/redo

## 6. Project Structure

```
grpc-text-editor/
├── pom.xml
├── DESIGN.md
├── README.md
├── src/
│   ├── main/
│   │   ├── proto/
│   │   │   └── text_editor.proto
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── texteditor/
│   │   │           ├── buffer/
│   │   │           │   ├── TextBuffer.java
│   │   │           │   ├── SimpleBuffer.java
│   │   │           │   ├── RopeBuffer.java
│   │   │           │   └── PieceTableBuffer.java
│   │   │           ├── document/
│   │   │           │   ├── Document.java
│   │   │           │   └── DocumentImpl.java
│   │   │           ├── manager/
│   │   │           │   └── DocumentManager.java
│   │   │           ├── service/
│   │   │           │   └── TextEditorServiceImpl.java
│   │   │           ├── server/
│   │   │           │   └── TextEditorServer.java
│   │   │           ├── client/
│   │   │           │   ├── TextEditorClient.java
│   │   │           │   └── CLIClient.java
│   │   │           └── exception/
│   │   │               ├── DocumentNotFoundException.java
│   │   │               └── InvalidPositionException.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       └── java/
│           └── com/
│               └── texteditor/
│                   ├── buffer/
│                   │   ├── SimpleBufferTest.java
│                   │   ├── RopeBufferTest.java
│                   │   └── BufferComparisonTest.java
│                   ├── document/
│                   │   └── DocumentTest.java
│                   ├── service/
│                   │   └── TextEditorServiceTest.java
│                   └── integration/
│                       └── EndToEndTest.java
```

## 7. Implementation Phases

### Phase 1: Foundation
1. Set up Maven project with gRPC dependencies
2. Define `.proto` file
3. Implement `TextBuffer` interface
4. Implement `SimpleBuffer` (basic)
5. Unit tests for `SimpleBuffer`

### Phase 2: Document Management
1. Implement `Document` interface
2. Implement `DocumentManager`
3. Add cursor management
4. Unit tests for document operations

### Phase 3: gRPC Service
1. Implement `TextEditorServiceImpl`
2. Implement server startup/shutdown
3. Add error handling and validation
4. Integration tests

### Phase 4: Client
1. Implement basic client
2. Implement CLI client for testing
3. End-to-end tests

### Phase 5: Advanced Buffers
1. Implement `RopeBuffer`
2. Implement `PieceTableBuffer`
3. Performance benchmarking
4. Buffer comparison tests

## 8. Key Design Considerations

### 8.1 Thread Safety
- Each `Document` is accessed by a single client at a time (for now)
- `DocumentManager` uses `ConcurrentHashMap` for thread safety
- Future: Add locking mechanism for concurrent editing

### 8.2 Position Conventions
- All positions are 0-indexed
- Ranges are [start, end) - start inclusive, end exclusive
- Position `-1` means "use current cursor position"
- Position equal to length means "append at end"

### 8.3 Error Handling
- Use gRPC status codes appropriately
  - `NOT_FOUND` - Document doesn't exist
  - `INVALID_ARGUMENT` - Invalid position/range
  - `INTERNAL` - Server errors
- Return descriptive error messages
- Validate all inputs before operations

### 8.4 Cursor Behavior
- Cursor position is always valid (0 to length)
- After insert at cursor: cursor moves to end of inserted text
- After delete: cursor moves to start of deletion range
- `move_cursor` flag allows override of default behavior

### 8.5 Unicode Support
- Use Java's `String` (UTF-16)
- Be aware of surrogate pairs and combining characters
- Consider code point vs code unit positions (future enhancement)

### 8.6 Performance Characteristics

| Operation | SimpleBuffer | RopeBuffer | PieceTable |
|-----------|--------------|------------|------------|
| Insert    | O(n)         | O(log n)   | O(log m)   |
| Delete    | O(n)         | O(log n)   | O(log m)   |
| Substring | O(k)         | O(log n+k) | O(log m+k) |
| Length    | O(1)         | O(1)       | O(1)       |

*n = document size, m = number of pieces, k = substring length*

## 9. Testing Strategy

### 9.1 Unit Tests
- Test each buffer implementation independently
- Test boundary conditions (empty, single char, very long)
- Test cursor management
- Test position validation

### 9.2 Integration Tests
- Test gRPC service with all buffer types
- Test multiple concurrent documents
- Test error handling paths

### 9.3 Performance Tests
- Benchmark different buffer implementations
- Test with various document sizes (1KB, 10KB, 100KB, 1MB)
- Test different edit patterns (sequential, random)

### 9.4 Test Scenarios
```
1. Empty document operations
2. Insert at beginning, middle, end
3. Delete at beginning, middle, end
4. Delete ranges (single char, multiple chars, entire document)
5. Cursor movements (absolute, relative, boundaries)
6. Invalid positions (negative, beyond length)
7. Unicode text (emojis, multi-byte characters)
8. Large document handling
9. Rapid sequential operations
10. Document lifecycle (create, use, close)
```

## 10. Future Enhancements

### 10.1 Features
- Undo/redo support
- Multi-cursor support
- Find/replace operations
- Line-based operations (getLine, insertLine)
- Syntax highlighting support
- Collaborative editing (operational transforms or CRDTs)

### 10.2 Performance
- Streaming large documents
- Incremental substring retrieval
- Compression for large documents
- Disk-backed storage for very large files

### 10.3 Deployment
- Dockerization
- Load balancing multiple server instances
- Document persistence (database integration)
- Authentication and authorization

## 11. Dependencies (pom.xml)

```xml
- gRPC Java (io.grpc:grpc-netty, grpc-protobuf, grpc-stub)
- Protocol Buffers (com.google.protobuf:protobuf-java)
- JUnit 5 for testing
- Logback for logging
- Guava (optional, for Rope implementation)
```

## 12. Getting Started (for developers)

1. Clone repository
2. Run `mvn clean compile` to generate gRPC stubs
3. Implement `SimpleBuffer` following TDD approach
4. Run tests: `mvn test`
5. Implement gRPC service
6. Start server: `mvn exec:java -Dexec.mainClass="com.texteditor.server.TextEditorServer"`
7. Test with client

## 13. Learning Objectives Achieved

By completing this project, you will learn:

✓ gRPC service definition and implementation in Java
✓ Protocol Buffer schema design
✓ Text buffer data structures (Rope, Piece Table)
✓ Interface-based design and dependency injection
✓ Cursor management in text editors
✓ Unit testing and integration testing
✓ Performance benchmarking and optimization
✓ Error handling in distributed systems
✓ Thread safety considerations
✓ Client-server architecture

---

**Next Steps**: Review this design, provide feedback, then proceed with Phase 1 implementation.
