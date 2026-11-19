package com.cibertec.empleados;

import com.cibertec.empleados.repository.EmpleadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestExecutionListeners(listeners = DependencyInjectionTestExecutionListener.class)
class EmpleadosApiTest {
    private final MockMvc mvc;
    private final ObjectMapper mapper;
    private final EmpleadoRepository repository;

    EmpleadosApiTest(ApplicationContext context) {
        mvc = context.getBean(MockMvc.class);
        mapper = context.getBean(ObjectMapper.class);
        repository = context.getBean(EmpleadoRepository.class);
    }

    @BeforeEach
    void limpiarBaseTemporal() { repository.deleteAll(); }

    private Integer crear() throws Exception {
        String respuesta = mvc.perform(post("/api/v1/empleados")
            .contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Juan","apellido":"Perez","email":"juan@example.com","especialidad":"Backend"}
                """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(respuesta).get("id").asInt();
    }

    @Test
    void registrarConsultarYListarConFormatoDelContrato() throws Exception {
        Integer id = crear();
        mvc.perform(get("/api/v1/empleados/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Juan"))
            .andExpect(jsonPath("$.email").value("juan@example.com"));
        mvc.perform(get("/api/v1/empleados")).andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void listarSinDatosDevuelveArregloVacio() throws Exception {
        mvc.perform(get("/api/v1/empleados")).andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void actualizarPersisteSinCambiarElId() throws Exception {
        Integer id = crear();
        mvc.perform(put("/api/v1/empleados/" + id).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"Ana","apellido":"Lopez","email":"ana@example.com","especialidad":"Frontend"}
            """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.especialidad").value("Frontend"));
        mvc.perform(get("/api/v1/empleados/" + id)).andExpect(jsonPath("$.nombre").value("Ana"));
        Assertions.assertEquals(1, repository.count());
    }

    @Test
    void especialidadSePuedeOmitirOEnviarNula() throws Exception {
        String respuesta = mvc.perform(post("/api/v1/empleados").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ana","apellido":"Lopez","email":"ana@example.com"}
                """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.especialidad").value(org.hamcrest.Matchers.nullValue()))
            .andReturn().getResponse().getContentAsString();
        Integer id = mapper.readTree(respuesta).get("id").asInt();
        mvc.perform(put("/api/v1/empleados/" + id).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"Ana","apellido":"Lopez","email":"ana@example.com","especialidad":null}
            """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.especialidad").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void eliminarSoloElEmpleadoSolicitadoYDevolver204SinCuerpo() throws Exception {
        Integer id = crear();
        Integer otro = crear();
        mvc.perform(delete("/api/v1/empleados/" + id)).andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(get("/api/v1/empleados/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/empleados/" + otro)).andExpect(status().isOk());
        Assertions.assertEquals(1, repository.count());
    }

    @Test
    void operacionesConIdInexistenteDevuelven404() throws Exception {
        mvc.perform(get("/api/v1/empleados/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/empleados/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/empleados/999999").contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":"Ana","apellido":"Lopez","email":"ana@example.com"}
            """))
            .andExpect(status().isNotFound());
        Assertions.assertEquals(0, repository.count());
    }

    @Test
    void camposObligatoriosSeValidanEnRegistroYActualizacion() throws Exception {
        mvc.perform(post("/api/v1/empleados").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nombre").exists())
            .andExpect(jsonPath("$.campos.apellido").exists()).andExpect(jsonPath("$.campos.email").exists());
        Integer id = crear();
        mvc.perform(put("/api/v1/empleados/" + id).contentType(MediaType.APPLICATION_JSON).content("""
            {"nombre":" ","apellido":"Perez","email":"incorrecto"}
            """))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/empleados/" + id)).andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void respetarLongitudesDelPdf() throws Exception {
        String cuerpo = mapper.writeValueAsString(java.util.Map.of("nombre", "x".repeat(101),
            "apellido", "Perez", "email", "juan@example.com", "especialidad", "x".repeat(101)));
        mvc.perform(post("/api/v1/empleados").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nombre").exists())
            .andExpect(jsonPath("$.campos.especialidad").exists());
        Assertions.assertEquals(0, repository.count());
    }

    @Test
    void idLiteralInvalidoDevuelveMensajeClaro() throws Exception {
        mvc.perform(get("/api/v1/empleados/ID")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").value("El ID debe ser un numero entero valido"));
    }
}
