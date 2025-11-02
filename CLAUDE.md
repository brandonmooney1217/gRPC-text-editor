# gRPC Text Editor Service - Claude Code Instructions

## Project Overview

This is a distributed text editor service implemented using gRPC in Java. The service provides core text editing operations (insert, delete, cursor movement, substring retrieval) with a pluggable text buffer implementation.

**Current Status**: Phase 1 Complete
- TextBuffer interface defined
- SimpleBuffer implementation complete with tests
- Protocol Buffer definitions complete
- Maven build configured with gRPC code generation

**Project Location**: /Users/brandonmooney/Downloads/rpc/gRPC-rope

## Development Philosophy

This project follows a **phased, incremental development approach**. Refer to DESIGN.md for the complete architecture and all planned phases. Always work within the current phase unless explicitly instructed to move forward.

### Implementation Phases

1. **Phase 1 (COMPLETE)**: Foundation - TextBuffer interface, SimpleBuffer, proto definitions
2. **Phase 2 (NEXT)**: Document Management - Document interface, DocumentManager, cursor tracking
3. **Phase 3**: gRPC Service - TextEditorServiceImpl, server implementation
4. **Phase 4**: Client - Basic client, CLI client
5. **Phase 5**: Advanced Buffers - RopeBuffer, PieceTableBuffer, performance benchmarking

## Code Conventions

### Java Style Guidelines

1. **Package Structure**:
   - `com.texteditor.buffer` - Text buffer implementations
   - `com.texteditor.document` - Document management
   - `com.texteditor.manager` - DocumentManager
   - `com.texteditor.service` - gRPC service implementation
   - `com.texteditor.server` - Server startup/shutdown
   - `com.texteditor.client` - Client implementations
   - `com.texteditor.grpc` - Generated gRPC code (do not modify)
   - `com.texteditor.exception` - Custom exceptions

2. **Naming Conventions**:
   - Interfaces: Descriptive nouns (e.g., `TextBuffer`, `Document`)
   - Implementations: Interface name + descriptor (e.g., `SimpleBuffer`, `RopeBuffer`, `DocumentImpl`)
   - Constants: UPPER_SNAKE_CASE (e.g., `TYPE`, `DEFAULT_PORT`)
   - Methods: camelCase verbs (e.g., `insertText`, `moveCursor`)

3. **Documentation Requirements**:
   - All public interfaces and classes MUST have JavaDoc comments
   - All public methods MUST document parameters, return values, and thrown exceptions
   - Include time complexity in comments for buffer operations
   - Document thread-safety guarantees

4. **Thread Safety**:
   - All TextBuffer implementations MUST be thread-safe
   - Use `synchronized` methods or explicit locking
   - Document thread-safety approach in class-level JavaDoc

5. **Error Handling**:
   - Use `IndexOutOfBoundsException` for invalid positions/ranges
   - Use `IllegalArgumentException` for null or invalid parameters
   - Provide descriptive error messages including actual values
   - Example: `"Position 5 is out of bounds (length: 3)"`

### Position and Range Conventions

**CRITICAL**: These conventions apply throughout the entire codebase:

- **All positions are 0-indexed**
- **All ranges are [start, end)** - start inclusive, end exclusive
- **Position -1 means "use current cursor position"** (in gRPC operations)
- **Position equal to length means "append at end"**
- **Cursor position is always valid**: 0 <= cursor <= length

### Code Quality Standards

1. **Input Validation**:
   - Validate all inputs before processing
   - Check for null parameters
   - Validate position/range boundaries
   - Provide clear error messages

2. **Testing Requirements**:
   - EVERY new class must have corresponding unit tests
   - Test class naming: `[ClassName]Test.java`
   - Test boundary conditions: empty, single element, maximum size
   - Test error cases: invalid positions, null inputs
   - Use JUnit 5 with `@Test`, `@BeforeEach`, `@ParameterizedTest` annotations

3. **Test Organization**:
   ```java
   @BeforeEach
   void setUp() {
       // Initialize test fixtures
   }

   @Test
   void methodName_scenario_expectedBehavior() {
       // Arrange, Act, Assert
   }
   ```

## Working with Protocol Buffers

### Proto File Location
- Source: `/Users/brandonmooney/Downloads/rpc/gRPC-rope/src/main/proto/text_editor.proto`
- Generated Java: `/Users/brandonmooney/Downloads/rpc/gRPC-rope/target/generated-sources/protobuf/`

### Important Rules for Proto Files

1. **NEVER manually edit generated Java code** in `target/generated-sources/`
2. **Always regenerate code after proto changes**: `mvn clean compile`
3. **Use the proto package**: `com.texteditor.grpc` for all generated types
4. **Proto message fields are immutable** - use builders for construction

### Working with Generated gRPC Code

```java
// Building a request
CreateDocumentRequest request = CreateDocumentRequest.newBuilder()
    .setDocumentId("doc-123")
    .setInitialContent("Hello world")
    .setBufferType("simple")
    .build();

// Accessing fields
String docId = request.getDocumentId();
```

## Maven Build Commands

### Essential Commands

```bash
# Clean and compile (regenerates proto files)
mvn clean compile

# Run tests
mvn test

# Run specific test
mvn test -Dtest=SimpleBufferTest

# Run server (when implemented)
mvn exec:java -Dexec.mainClass="com.texteditor.server.TextEditorServer"

# Package JAR
mvn package

# Skip tests during build
mvn clean compile -DskipTests
```

### Build Workflow

1. After modifying `.proto` files: **ALWAYS** run `mvn clean compile`
2. Before committing: **ALWAYS** run `mvn test` to ensure all tests pass
3. Generated code locations:
   - Protocol Buffer messages: `target/generated-sources/protobuf/java/`
   - gRPC service stubs: `target/generated-sources/protobuf/grpc-java/`

## Buffer Implementation Guidelines

### Performance Characteristics

When implementing or working with buffers, be aware of these complexities:

| Operation | SimpleBuffer | RopeBuffer | PieceTable |
|-----------|--------------|------------|------------|
| Insert    | O(n)         | O(log n)   | O(log m)   |
| Delete    | O(n)         | O(log n)   | O(log m)   |
| Substring | O(k)         | O(log n+k) | O(log m+k) |
| Length    | O(1)         | O(1)       | O(1)       |

*n = document size, m = number of pieces, k = substring length*

### Buffer Interface Contract

All implementations of `TextBuffer` must:
1. Support all operations defined in the interface
2. Be thread-safe
3. Return consistent results (length, substring, toString)
4. Handle edge cases: empty buffer, insertion at boundaries
5. Validate inputs and throw appropriate exceptions

## Testing Strategy

### Test Coverage Requirements

1. **Unit Tests** (per class):
   - Empty state operations
   - Single element operations
   - Boundary operations (beginning, middle, end)
   - Large data sets
   - Error conditions (invalid inputs)

2. **Integration Tests** (future phases):
   - gRPC service with all buffer types
   - Multiple concurrent documents
   - Error handling paths
   - Client-server interaction

3. **Test Scenarios to Always Include**:
   ```
   - Empty document operations
   - Insert at position 0, middle, end
   - Delete at position 0, middle, end
   - Delete ranges: single char, multiple chars, entire document
   - Invalid positions: negative, beyond length
   - Null parameter handling
   - Unicode text (emojis, multi-byte characters)
   ```

### Test Naming Convention

```java
@Test
void insert_atBeginning_shiftsExistingContent()

@Test
void delete_invalidRange_throwsException()

@Test
void substring_entireBuffer_returnsFullContent()
```

## gRPC Service Guidelines (Phase 3+)

### Status Codes

Use appropriate gRPC status codes:
- `NOT_FOUND` - Document doesn't exist
- `INVALID_ARGUMENT` - Invalid position, range, or parameter
- `ALREADY_EXISTS` - Document ID already in use
- `INTERNAL` - Server errors, unexpected failures

### Cursor Behavior

Default cursor behavior after operations:
- **After insert at cursor**: cursor moves to end of inserted text
- **After delete**: cursor moves to start of deletion range
- **`move_cursor` flag**: when false, cursor remains unchanged

## Common Development Tasks

### Creating a New Buffer Implementation

1. Create class implementing `TextBuffer` interface
2. Add thread-safety mechanism (synchronized or locks)
3. Implement all interface methods
4. Add comprehensive unit tests
5. Document time complexity in class JavaDoc
6. Update buffer factory (when implemented) to support new type

### Adding New gRPC Operations (Future)

1. Update `text_editor.proto` with new RPC and messages
2. Run `mvn clean compile` to regenerate code
3. Implement the operation in service implementation
4. Add validation and error handling
5. Write integration tests
6. Update client to support new operation

### Debugging Tips

1. **Proto compilation errors**: Check `target/protoc-dependencies/` for conflicts
2. **gRPC runtime errors**: Verify all required dependencies in `pom.xml`
3. **Test failures**: Run tests individually to isolate issues: `mvn test -Dtest=TestClassName`
4. **Thread safety issues**: Look for unsynchronized access to shared state

## Project Dependencies

### Key Libraries

- **gRPC Java** (v1.58.0): RPC framework
- **Protocol Buffers** (v3.24.0): Serialization
- **JUnit 5** (v5.10.0): Testing framework
- **Logback** (v1.4.11): Logging
- **Java 11**: Minimum version required

### Adding New Dependencies

1. Add dependency to `pom.xml` under appropriate scope
2. Run `mvn clean compile` to download
3. Document why the dependency is needed (in commit message or comments)

## File Organization

```
/Users/brandonmooney/Downloads/rpc/gRPC-rope/
├── pom.xml                          # Maven configuration
├── DESIGN.md                        # Complete architecture documentation
├── CLAUDE.md                        # This file
├── src/main/
│   ├── proto/
│   │   └── text_editor.proto       # gRPC service definition
│   ├── java/com/texteditor/
│   │   ├── buffer/                 # TextBuffer implementations
│   │   ├── document/               # Document management (Phase 2)
│   │   ├── manager/                # DocumentManager (Phase 2)
│   │   ├── service/                # gRPC service impl (Phase 3)
│   │   ├── server/                 # Server startup (Phase 3)
│   │   ├── client/                 # Client implementations (Phase 4)
│   │   └── exception/              # Custom exceptions
│   └── resources/
│       └── logback.xml             # Logging configuration
├── src/test/java/com/texteditor/   # Mirror structure of main
└── target/                          # Generated code (do not edit)
```

## Critical Rules

1. **Never modify generated code** in `target/` directory
2. **Always run tests before committing**: `mvn test`
3. **Follow the phase-based development approach** - don't skip ahead
4. **Document all public APIs** with JavaDoc
5. **Validate all inputs** before processing
6. **Use consistent position/range conventions** (0-indexed, [start, end))
7. **Make all TextBuffer implementations thread-safe**
8. **Write tests for every new class**

## When Working on This Project

1. **Read DESIGN.md first** to understand the complete architecture
2. **Check current phase** before implementing features
3. **Run `mvn clean compile`** after pulling changes or modifying proto files
4. **Run `mvn test`** frequently to catch regressions early
5. **Refer to existing code** (TextBuffer, SimpleBuffer) as examples of style
6. **Ask for clarification** if phase boundaries are unclear

## Future Enhancements (Do Not Implement Yet)

These features are planned but not yet in scope:
- Undo/redo support
- Multi-cursor editing
- Find/replace operations
- Line-based operations
- Syntax highlighting
- Collaborative editing
- Persistence/database integration
- Authentication/authorization

Refer to DESIGN.md Section 10 for details on future enhancements.

## Learning Objectives

This project teaches:
- gRPC service design and implementation
- Protocol Buffer schema design
- Text editor data structures (Rope, Piece Table)
- Interface-based design patterns
- Thread-safe concurrent programming
- Unit and integration testing
- Performance analysis and optimization

---

**Remember**: This is a learning project focused on understanding gRPC and text editor fundamentals. Prioritize clarity and correctness over premature optimization.
