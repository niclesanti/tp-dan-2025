package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.CuentaBancariaRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.HuespedDTORequest;
import edu.utn.frsf.isi.dan.user.dto.PropietarioDTORequest;
import edu.utn.frsf.isi.dan.user.mapper.CuentaBancariaMapper;
import edu.utn.frsf.isi.dan.user.mapper.HuespedMapper;
import edu.utn.frsf.isi.dan.user.mapper.PropietarioMapper;
import edu.utn.frsf.isi.dan.user.mapper.TarjetaCreditoMapper;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.CuentaBancaria;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.Propietario;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import edu.utn.frsf.isi.dan.user.model.Usuario;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class UserService {

    private final BancoRepository bancoRepository;
    private final CuentaBancariaRepository cuentaBancariaRepository;
    private final TarjetaCreditoRepository tarjetaCreditoRepository;
    private final UsuarioRepository usuarioRepository;

    private final HuespedMapper huespedMapper;
    private final PropietarioMapper propietarioMapper;
    private final CuentaBancariaMapper cuentaBancariaMapper;
    private final TarjetaCreditoMapper tarjetaCreditoMapper;

    public Huesped crearUsuarioHuesped(HuespedDTORequest huespedRequest) {
        // Buscar el banco por ID
        Optional<Banco> bancoOptional = bancoRepository.findById(huespedRequest.tarjetaCredito().bancoId());
        if (bancoOptional.isEmpty()) {
            throw new IllegalArgumentException("Banco no encontrado con ID: " + huespedRequest.tarjetaCredito().bancoId());
        }

        Banco banco = bancoOptional.get();

        // Crear y guardar el usuario
        Huesped usuario = huespedMapper.toEntity(huespedRequest);
        usuarioRepository.save(usuario);

        // Crear y guardar la tarjeta de crédito
        TarjetaCredito tarjetaCredito = tarjetaCreditoMapper.toEntity(huespedRequest.tarjetaCredito());
        tarjetaCredito.setHuesped(usuario);
        tarjetaCredito.setBanco(banco);
        TarjetaCredito tarjetaCreditoSaved =tarjetaCreditoRepository.save(tarjetaCredito);
        if (usuario.getTarjetaCredito() == null) {
            usuario.setTarjetaCredito(new ArrayList<>());
        }
        usuario.getTarjetaCredito().add(tarjetaCreditoSaved);
        return usuario;
    }

    public void crearUsuarioPropietario(PropietarioDTORequest propietarioRequest) {
        // Buscar el banco por ID
        Optional<Banco> bancoOptional = bancoRepository.findById(propietarioRequest.cuentaBancaria().bancoId());
        if (bancoOptional.isEmpty()) {
            throw new IllegalArgumentException("Banco no encontrado con ID: " + propietarioRequest.cuentaBancaria().bancoId());
        }

        Banco banco = bancoOptional.get();

        Propietario propietario = propietarioMapper.toEntity(propietarioRequest);
        CuentaBancaria cuentaBancaria = cuentaBancariaMapper.toEntity(propietarioRequest.cuentaBancaria());
        cuentaBancaria.setBanco(banco);
        propietario.setCuentaBancaria(cuentaBancariaRepository.save(cuentaBancaria));
        usuarioRepository.save(propietario);
    }
    
    public Page<Usuario> buscarPorNombre(String nombre, Pageable pageable) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    public Page<Usuario> buscarPorDni(String dni, Pageable pageable) {
        return usuarioRepository.findByDniContaining(dni, pageable);
    }

    public Usuario buscarPorDniExacto(String dni) {
        return usuarioRepository.findByDni(dni);
    }
}