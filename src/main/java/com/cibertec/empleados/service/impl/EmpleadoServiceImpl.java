package com.cibertec.empleados.service.impl;

import com.cibertec.empleados.dto.*;
import com.cibertec.empleados.entity.Empleado;
import com.cibertec.empleados.exception.EmpleadoNoEncontradoException;
import com.cibertec.empleados.repository.EmpleadoRepository;
import com.cibertec.empleados.service.IEmpleadoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadoServiceImpl implements IEmpleadoService {
    private final EmpleadoRepository repository;

    @Override
    public List<EmpleadoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream()
            .map(EmpleadoResponse::from).toList();
    }

    @Override
    public EmpleadoResponse buscar(Integer id) {
        return EmpleadoResponse.from(obtenerExistente(id));
    }

    @Override
    @Transactional
    public EmpleadoResponse registrar(EmpleadoRequest request) {
        Empleado empleado = Empleado.builder()
            .nombre(request.getNombre().strip())
            .apellido(request.getApellido().strip())
            .email(request.getEmail().strip())
            .especialidad(normalizarOpcional(request.getEspecialidad()))
            .build();
        return EmpleadoResponse.from(repository.save(empleado));
    }

    @Override
    @Transactional
    public EmpleadoResponse actualizar(Integer id, EmpleadoRequest request) {
        Empleado empleado = obtenerExistente(id);
        empleado.setNombre(request.getNombre().strip());
        empleado.setApellido(request.getApellido().strip());
        empleado.setEmail(request.getEmail().strip());
        // PUT reemplaza los datos: omitir especialidad la deja en null.
        empleado.setEspecialidad(normalizarOpcional(request.getEspecialidad()));
        return EmpleadoResponse.from(repository.save(empleado));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        repository.delete(obtenerExistente(id));
    }

    private Empleado obtenerExistente(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new EmpleadoNoEncontradoException(id));
    }

    private String normalizarOpcional(String valor) {
        return valor == null ? null : valor.strip();
    }
}
