package com.medflow.pharmacy.exception;

/**
 * Excepción lanzada cuando no hay stock suficiente para despachar una receta.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
