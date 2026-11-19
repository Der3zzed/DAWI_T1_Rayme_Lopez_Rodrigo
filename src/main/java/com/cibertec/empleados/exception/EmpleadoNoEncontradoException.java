package com.cibertec.empleados.exception;

public class EmpleadoNoEncontradoException extends RuntimeException {
    public EmpleadoNoEncontradoException(Integer id) {
        super("No existe un empleado con ID " + id);
    }
}
