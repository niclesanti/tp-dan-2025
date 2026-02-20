package edu.utn.frsf.isi.dan.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearUsuarioHuesped() throws Exception {
        // Arrange
        TarjetaCreditoDTORequest tarjetaCredito = new TarjetaCreditoDTORequest(
            "1234567890123456",  // numero
            "Jane Smith",        // nombreTitular
            "12/25",             // fechaVencimiento
            "123",               // cvc
            true,                // esPrincipal
            1                    // bancoId
        );
        
        HuespedDTORequest huespedRequest = new HuespedDTORequest(
            "Jane Smith",                    // nombre
            "jane.smith@example.com",        // email
            "9876543210",                    // telefono
            "12345678",                      // dni
            LocalDate.of(1990, 5, 15),       // fechaNacimiento
            tarjetaCredito                    // tarjetaCredito
        );

        // Act & Assert
        mockMvc.perform(post("/users/huesped")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(huespedRequest)))
            .andExpect(status().isCreated());
    }

    @Test
    public void testCrearUsuarioPropietario() throws Exception {
        // Arrange
        CuentaBancariaDTORequest cuentaBancaria = new CuentaBancariaDTORequest(
            "123456789",                          // numeroCuenta
            "1234567890123456789012",             // cbu (22 dígitos)
            "john.doe.cuenta",                    // alias
            1                                     // bancoId
        );
        
        PropietarioDTORequest propietarioRequest = new PropietarioDTORequest(
            "John Doe",                           // nombre
            "john.doe@example.com",               // email
            "1234567890",                         // telefono
            "12345678",                           // dni
            cuentaBancaria,                       // cuentaBancaria
            1L                                    // idHotel
        );

        // Act & Assert
        mockMvc.perform(post("/users/propietario")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(propietarioRequest)))
                .andExpect(status().isCreated());
    }
}