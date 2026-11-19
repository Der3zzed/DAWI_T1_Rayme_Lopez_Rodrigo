package com.cibertec.empleados.service;

import com.cibertec.empleados.dto.*;
import java.util.List;

public interface IEmpleadoService {
    List<EmpleadoResponse> listar();
    EmpleadoResponse buscar(Integer id);
    EmpleadoResponse registrar(EmpleadoRequest request);
    EmpleadoResponse actualizar(Integer id, EmpleadoRequest request);
    void eliminar(Integer id);
}
