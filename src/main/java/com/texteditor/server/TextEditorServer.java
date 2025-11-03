package com.texteditor.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.texteditor.document.Document;
import com.texteditor.grpc.*;
import com.texteditor.manager.DocumentManager;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class TextEditorServer {
    private static final Logger logger = Logger.getLogger(TextEditorServer.class.getName());

    private final int port;
    private final Server server;

    public TextEditorServer(int port) throws IOException {
        this(Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create()), port);
    }

    public TextEditorServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        // this.headerServerInterceptor = new HeaderServerInterceptor();

        server = serverBuilder
            .addService(new TextEditorService())
            //.intercept(headerServerInterceptor)
            .build();
    }

    public void start () throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                TextEditorServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }
        });
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        TextEditorServer server = new TextEditorServer(8980);
        server.start();
        server.blockUntilShutdown();
    }

    private static class TextEditorService extends TextEditorServiceGrpc.TextEditorServiceImplBase {
        private final DocumentManager documentManager;

        TextEditorService() {
            this.documentManager = new DocumentManager();
        }

        TextEditorService(DocumentManager documentManager) {
            this.documentManager = documentManager;
        }

        @Override
        public void createDocument(CreateDocumentRequest request, StreamObserver<CreateDocumentResponse> responseObserver) {
            // TODO: Implement createDocument
            // 1. Extract documentId, initialContent, bufferType from request
            final String documentId = request.getDocumentId();
            final String content = request.getInitialContent();

            Document document = documentManager.createDocument(documentId, content);

            final CreateDocumentResponse response = CreateDocumentResponse.newBuilder()
                .setDocumentId(documentId)
                .setSuccess(true)
                .setMessage("success")
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void closeDocument(CloseDocumentRequest request, StreamObserver<CloseDocumentResponse> responseObserver) {
            // TODO: Implement closeDocument
            // 1. Extract documentId from request
            // 2. Validate documentId is not null/empty
            // 3. Call documentManager.closeDocument()
            // 4. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 5. Build and send CloseDocumentResponse
            // 6. Call responseObserver.onCompleted()
        }

        @Override
        public void insert(InsertRequest request, StreamObserver<OperationResponse> responseObserver) {
            // TODO: Implement insert
            // 1. Extract documentId, position, text, moveCursor from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Call document.insert(position, text, moveCursor)
            // 5. Handle IndexOutOfBoundsException -> Status.INVALID_ARGUMENT
            // 6. Build OperationResponse with cursor position and document length
            // 7. Call responseObserver.onCompleted()
        }

        @Override
        public void delete(DeleteRequest request, StreamObserver<OperationResponse> responseObserver) {
            // TODO: Implement delete
            // 1. Extract documentId, startPosition, endPosition, moveCursor from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Call document.delete(startPosition, endPosition, moveCursor)
            // 5. Handle IndexOutOfBoundsException -> Status.INVALID_ARGUMENT
            // 6. Build OperationResponse with cursor position and document length
            // 7. Call responseObserver.onCompleted()
        }

        @Override
        public void moveCursor(MoveCursorRequest request, StreamObserver<CursorResponse> responseObserver) {
            // TODO: Implement moveCursor
            // 1. Extract documentId from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Check which movement type (absolute_position or relative_offset)
            // 5. Call document.setCursorPosition() or document.moveCursor()
            // 6. Handle IndexOutOfBoundsException -> Status.INVALID_ARGUMENT
            // 7. Build CursorResponse with cursor position and document length
            // 8. Call responseObserver.onCompleted()
        }

        @Override
        public void getCursorPosition(GetCursorPositionRequest request, StreamObserver<CursorResponse> responseObserver) {
            // TODO: Implement getCursorPosition
            // 1. Extract documentId from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Call document.getCursorPosition()
            // 5. Build CursorResponse with cursor position and document length
            // 6. Call responseObserver.onCompleted()
        }

        @Override
        public void getSubstring(GetSubstringRequest request, StreamObserver<GetSubstringResponse> responseObserver) {
            // TODO: Implement getSubstring
            // 1. Extract documentId, startPosition, endPosition from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Handle endPosition == -1 (means end of document)
            // 5. Call document.substring(startPosition, endPosition)
            // 6. Handle IndexOutOfBoundsException -> Status.INVALID_ARGUMENT
            // 7. Build GetSubstringResponse with text and actual_length
            // 8. Call responseObserver.onCompleted()
        }

        @Override
        public void getDocumentInfo(GetDocumentInfoRequest request, StreamObserver<GetDocumentInfoResponse> responseObserver) {
            // TODO: Implement getDocumentInfo
            // 1. Extract documentId from request
            // 2. Get document from documentManager
            // 3. Handle DocumentNotFoundException -> Status.NOT_FOUND
            // 4. Get document.getId(), length(), getCursorPosition(), getBuffer().getType()
            // 5. Build GetDocumentInfoResponse with all document info
            // 6. Call responseObserver.onCompleted()
        }

        @Override
        public StreamObserver<ChangeRequest> streamChanges(StreamObserver<ChangeResponse> responseObserver) {
            // TODO: Implement streamChanges (optional - Phase 5)
            // This is a bidirectional streaming RPC for future enhancement
            // 1. Return a StreamObserver that handles incoming ChangeRequest messages
            // 2. Process each change (insert/delete/moveCursor)
            // 3. Send ChangeResponse back to client
            // For now, return unimplemented error
            responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("streamChanges is not yet implemented")
                .asRuntimeException());
            return new StreamObserver<ChangeRequest>() {
                @Override
                public void onNext(ChangeRequest value) {}
                @Override
                public void onError(Throwable t) {}
                @Override
                public void onCompleted() {}
            };
        }
    }

}
