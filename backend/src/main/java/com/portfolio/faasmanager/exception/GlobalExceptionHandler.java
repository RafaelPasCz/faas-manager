package com.portfolio.faasmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(GeneralException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno no servidor",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ResourceEditException.class)
    public ResponseEntity<ErrorResponse> handleResourceEditException(ResourceEditException ex, WebRequest request){
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_CONTENT.value(), // Ajustado para 422
            "Erro ao Editar o Recurso",
            ex.getMessage(),
            request.getDescription(false).replace("uri=","") // Removida a barra extra
        );
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(AttributeException.class)
    public ResponseEntity<ErrorResponse> handleAttributeException(AttributeException ex, WebRequest request){
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
        "Erro de atributo",
        ex.getMessage(),
        request.getDescription(false).replace("uri=","")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResourceDeleteException.class)
    public ResponseEntity<ErrorResponse> handleResourceDeleteException(ResourceDeleteException ex, WebRequest request){
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Erro ao deletar o Recurso",
        ex.getMessage(),
        request.getDescription(false).replace("uri=","")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceCreationException.class)
    public ResponseEntity<ErrorResponse> handleResourceCreationException(ResourceCreationException ex, WebRequest request){
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Erro ao criar o Recurso",
        ex.getMessage(),
        request.getDescription(false).replace("uri=","")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno no Servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}