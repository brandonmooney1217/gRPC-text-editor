# gRPC Text Editor Service

A text editor service implemented using gRPC in Java. This project demonstrates core text editing operations with a pluggable buffer architecture.

## Project Status

**Phase 1: Foundation - COMPLETE ✓**

- ✓ Maven project with gRPC dependencies
- ✓ Protocol Buffer service definitions
- ✓ TextBuffer interface
- ✓ SimpleBuffer implementation (StringBuilder-based)
- ✓ Comprehensive unit tests (52 tests, all passing)

## Project Structure

```
grpc-text-editor/
├── pom.xml                          # Maven configuration
├── DESIGN.md                        # Complete design document
├── README.md                        # This file
├── src/
│   ├── main/
│   │   ├── proto/
│   │   │   └── text_editor.proto    # gRPC service definitions
│   │   └── java/
│   │       └── com/texteditor/buffer/
│   │           ├── TextBuffer.java       # Buffer interface
│   │           └── SimpleBuffer.java     # Basic implementation
│   └── test/
│       └── java/com/texteditor/buffer/
│           └── SimpleBufferTest.java     # Comprehensive tests
└── target/                          # Generated code and artifacts
    └── generated-sources/protobuf/  # Generated gRPC stubs
```

## Features

### TextBuffer Interface

Core interface for text buffer implementations:

- `insert(position, text)` - Insert text at position
- `delete(start, end)` - Delete text range [start, end)
- `substring(start, end)` - Get text from range
- `length()` - Get current buffer length
- `clear()` - Clear all content
- `getType()` - Get buffer type identifier

### SimpleBuffer Implementation

Basic implementation using `StringBuilder`:
- **Complexity**: O(n) for insert/delete
- **Thread-safe**: Synchronized methods
- **Best for**: Small documents, learning purposes
- **Tests**: 52 comprehensive unit tests

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Build the Project

```bash
# Clean and compile (generates gRPC stubs from proto files)
mvn clean compile

# Run tests
mvn test

# Package
mvn package
```

### Run Tests

```bash
mvn test
```

Expected output:
```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Usage Examples

### Basic Buffer Operations

```java
import com.texteditor.buffer.SimpleBuffer;
import com.texteditor.buffer.TextBuffer;

// Create a buffer
TextBuffer buffer = new SimpleBuffer();

// Insert text
buffer.insert(0, "Hello World");
System.out.println(buffer.toString());  // "Hello World"
System.out.println(buffer.length());    // 11

// Insert in middle
buffer.insert(6, "Beautiful ");
System.out.println(buffer.toString());  // "Hello Beautiful World"

// Delete a range
buffer.delete(6, 16);  // Delete "Beautiful "
System.out.println(buffer.toString());  // "Hello World"

// Get substring
String sub = buffer.substring(0, 5);
System.out.println(sub);  // "Hello"

// Clear buffer
buffer.clear();
System.out.println(buffer.length());  // 0
```

### With Initial Content

```java
TextBuffer buffer = new SimpleBuffer("Initial content");
buffer.insert(buffer.length(), " - more text");
System.out.println(buffer.toString());  // "Initial content - more text"
```

## gRPC Service Definition

The proto file defines a complete text editor service (Phase 2+):

- **Document lifecycle**: CreateDocument, CloseDocument
- **Text operations**: Insert, Delete
- **Cursor operations**: MoveCursor, GetCursorPosition
- **Query operations**: GetSubstring, GetDocumentInfo
- **Future**: StreamChanges for real-time collaboration

See `src/main/proto/text_editor.proto` for complete definitions.

## Testing

The project includes comprehensive tests covering:

- ✓ Constructor tests (empty, with content, null handling)
- ✓ Insert operations (beginning, middle, end, empty strings)
- ✓ Delete operations (all positions, ranges, edge cases)
- ✓ Substring extraction (various ranges)
- ✓ Length tracking
- ✓ Clear functionality
- ✓ Unicode handling (emojis, multi-byte characters)
- ✓ Error handling (invalid positions, null inputs)
- ✓ Edge cases (large documents, consecutive operations)
- ✓ Thread safety

Run tests with: `mvn test`

View test results in: `target/surefire-reports/`

## Important Notes

### Unicode Handling

Java's `String` uses UTF-16 encoding where:
- Most characters = 1 char
- Emojis (like 🌍) = 2 chars (surrogate pairs)
- This affects position calculations

Example:
```java
buffer.insert(0, "Hello 🌍");
System.out.println(buffer.length());  // 8 (not 7)
//                 H e l l o   🌍
// Position:       0 1 2 3 4 5 6 7
```

### Position Conventions

- All positions are **0-indexed**
- Ranges are **[start, end)** - start inclusive, end exclusive
- Position equal to `length()` means "append at end"
- Invalid positions throw `IndexOutOfBoundsException`

## Next Steps (Future Phases)

### Phase 2: Document Management
- Implement `Document` interface with cursor tracking
- Implement `DocumentManager` for multiple documents
- Add cursor management operations

### Phase 3: gRPC Service
- Implement `TextEditorServiceImpl`
- Create server with proper lifecycle
- Add error handling and validation

### Phase 4: Client
- Implement basic gRPC client
- Create CLI client for testing
- End-to-end integration tests

### Phase 5: Advanced Buffers
- **RopeBuffer**: O(log n) operations for large documents
- **PieceTableBuffer**: Efficient undo/redo support
- Performance benchmarking

See `DESIGN.md` for complete implementation plan.

## Dependencies

- **gRPC Java**: 1.58.0
  - grpc-netty-shaded
  - grpc-protobuf
  - grpc-stub
  - grpc-services
- **Protocol Buffers**: 3.24.0
- **JUnit Jupiter**: 5.10.0 (testing)
- **Logback**: 1.4.11 (logging)

## License

Educational project for learning gRPC and text editor implementation.

## Contributing

This is a learning project. See `DESIGN.md` for architecture and future enhancements.

---

**Phase 1 Complete!** 🎉

All foundation components are implemented and tested:
- ✓ Maven build system configured
- ✓ Proto definitions created
- ✓ TextBuffer interface designed
- ✓ SimpleBuffer implementation working
- ✓ 52 comprehensive tests passing

Ready for Phase 2: Document Management
