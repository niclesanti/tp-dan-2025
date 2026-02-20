package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.CuentaBancariaDTORequest;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTORequest;
import edu.utn.frsf.isi.dan.user.mapper.CuentaBancariaMapper;
import edu.utn.frsf.isi.dan.user.mapper.HuespedMapper;
import edu.utn.frsf.isi.dan.user.mapper.PropietarioMapper;
import edu.utn.frsf.isi.dan.user.mapper.TarjetaCreditoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.CuentaBancaria;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.Propietario;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private BancoRepository bancoRepository;

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private TarjetaCreditoRepository tarjetaCreditoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private HuespedMapper huespedMapper;
    
    @Mock
    private PropietarioMapper propietarioMapper;
    
    @Mock
    private CuentaBancariaMapper cuentaBancariaMapper;
    
    @Mock
    private TarjetaCreditoMapper tarjetaCreditoMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearUsuarioHuesped() {
        // Arrange
        TarjetaCreditoDTORequest tarjetaCreditoDTO = new TarjetaCreditoDTORequest(
            "1234567890123456",
            "Jane Smith",
            "12/25",
            "123",
            true,
            1
        );
        
        HuespedDTORequest huespedRequest = new HuespedDTORequest(
            "Jane Smith",
            "jane.smith@example.com",
            "9876543210",
            "12345678",
            LocalDate.of(1990, 5, 15),
            tarjetaCreditoDTO
        );
        
        Banco banco = Banco.builder().id(1).nombre("Banco Test").build();
        Huesped huesped = new Huesped();
        huesped.setNombre("Jane Smith");
        TarjetaCredito tarjetaCredito = new TarjetaCredito();
        tarjetaCredito.setNumero("1234567890123456");

        when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
        when(huespedMapper.toEntity(huespedRequest)).thenReturn(huesped);
        when(tarjetaCreditoMapper.toEntity(tarjetaCreditoDTO)).thenReturn(tarjetaCredito);
        when(usuarioRepository.save(any(Huesped.class))).thenReturn(huesped);
        when(tarjetaCreditoRepository.save(any(TarjetaCredito.class))).thenReturn(tarjetaCredito);

        // Act
        Huesped result = userService.crearUsuarioHuesped(huespedRequest);

        // Assert
        assertNotNull(result);
        verify(usuarioRepository).save(huesped);
        verify(tarjetaCreditoRepository).save(any(TarjetaCredito.class));
        verify(bancoRepository).findById(1);
        verify(huespedMapper).toEntity(huespedRequest);
        verify(tarjetaCreditoMapper).toEntity(tarjetaCreditoDTO);
    }

    @Test
    public void testCrearUsuarioPropietario() {
        // Arrange
        CuentaBancariaDTORequest cuentaBancariaDTO = new CuentaBancariaDTORequest(
            "123456789",
            "1234567890123456789012",
            "john.doe.cuenta",
            1
        );
        
        PropietarioDTORequest propietarioRequest = new PropietarioDTORequest(
            "John Doe",
            "john.doe@example.com",
            "1234567890",
            "12345678",
            cuentaBancariaDTO,
            1L
        );
        
        Banco banco = Banco.builder().id(1).nombre("Banco Test").build();
        Propietario propietario = new Propietario();
        propietario.setNombre("John Doe");
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setNumeroCuenta("123456789");

        when(bancoRepository.findById(1)).thenReturn(Optional.of(banco));
        when(propietarioMapper.toEntity(propietarioRequest)).thenReturn(propietario);
        when(cuentaBancariaMapper.toEntity(cuentaBancariaDTO)).thenReturn(cuentaBancaria);
        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);
        when(usuarioRepository.save(any(Propietario.class))).thenReturn(propietario);

        // Act
        userService.crearUsuarioPropietario(propietarioRequest);

        // Assert
        verify(usuarioRepository).save(propietario);
        verify(cuentaBancariaRepository).save(any(CuentaBancaria.class));
        verify(bancoRepository).findById(1);
        verify(propietarioMapper).toEntity(propietarioRequest);
        verify(cuentaBancariaMapper).toEntity(cuentaBancariaDTO);
    }
}