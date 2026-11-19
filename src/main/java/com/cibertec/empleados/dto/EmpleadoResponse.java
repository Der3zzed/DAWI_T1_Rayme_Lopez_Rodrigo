package com.cibertec.empleados.dto;

import com.cibertec.empleados.entity.Empleado;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpleadoResponse {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String especialidad;

    public static EmpleadoResponse from(Empleado empleado) {
        return EmpleadoResponse.builder()
            .id(empleado.getId())
            .nombre(empleado.getNombre())
            .apellido(empleado.getApellido())
            .email(empleado.getEmail())
            .especialidad(empleado.getEspecialidad())
            .build();
    }
}
