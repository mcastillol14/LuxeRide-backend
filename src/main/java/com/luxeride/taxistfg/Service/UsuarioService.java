package com.luxeride.taxistfg.Service;

import com.luxeride.taxistfg.Model.Rol;
import com.luxeride.taxistfg.Model.Usuario;
import com.luxeride.taxistfg.Model.UsuarioDTO;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.luxeride.taxistfg.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder encriptadoPassword;
    @Autowired
    private MailService mailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encriptadoPassword) {
        this.usuarioRepository = usuarioRepository;
        this.encriptadoPassword = encriptadoPassword;
    }

    @Transactional
    public void registrarUsario(Usuario usuario) {
        Optional<Usuario> existingUsuarioByEmail = usuarioRepository.findByEmail(usuario.getEmail());
        Optional<Usuario> existingUsuarioByDni = usuarioRepository.findByDni(usuario.getDni());

        if (existingUsuarioByDni.isPresent() && existingUsuarioByEmail.isPresent()) {
            throw new IllegalArgumentException("El DNI y el correo ya existen");
        } else if (existingUsuarioByEmail.isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya existe");
        } else if (existingUsuarioByDni.isPresent()) {
            throw new IllegalArgumentException("El DNI ya existe");
        }
        if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        if (usuario.getDni() == null || usuario.getDni().isEmpty()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El correo electronico es obligatorio");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        String passwordEncriptada = encriptadoPassword.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);
        usuario.setRol(Rol.ROL_CLIENTE);
        usuario.setAccountNonLocked(true);

        usuarioRepository.save(usuario);

        try{
            mailService.enviarMail(usuario.getEmail(), usuario.getNombre());
        }catch (MessagingException e){
            System.out.println(e.getMessage());
        }
    }

    @Transactional
    public void editarUsuario(Integer id, String nombre, String apellidos, String dni, String email) {
        Optional<Usuario> existingUsuario = usuarioRepository.findById(id);

        if (!existingUsuario.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario que quieres editar no existe");
        }

        Usuario usuarioActualizado = existingUsuario.get();

        if (nombre != null && !nombre.trim().isEmpty()) {
            usuarioActualizado.setNombre(nombre);
        }
        if (apellidos != null && !apellidos.trim().isEmpty()) {
            usuarioActualizado.setApellidos(apellidos);
        }
        if (dni != null && !dni.trim().isEmpty()) {
            usuarioActualizado.setDni(dni);
        }
        if (email != null && !email.trim().isEmpty()) {
            Optional<Usuario> existingUsuarioByEmail = usuarioRepository.findByEmail(email);
            if (existingUsuarioByEmail.isPresent() && !existingUsuarioByEmail.get().getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo electrónico ya está en uso");
            }
            usuarioActualizado.setEmail(email);
        }

        usuarioRepository.save(usuarioActualizado);
    }

    private UsuarioDTO usuarioAUsuarioDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isAccountNonLocked()
        );
    }
    public Page<UsuarioDTO> pageUsuarioDTO(Page<Usuario> usuariosPage) {
        List<UsuarioDTO> usuarioDTOs = usuariosPage.getContent()
                .stream()
                .map(this::usuarioAUsuarioDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(
                usuarioDTOs,
                usuariosPage.getPageable(),
                usuariosPage.getTotalElements()
        );
    }
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> obtenerUsuariosDTOPorFiltro(Pageable pageable, String dni) {
        Page<Usuario> usuariosPage;
        if (dni != null && !dni.isEmpty()) {
            usuariosPage = usuarioRepository.findByDni(pageable, dni);
        } else {
            usuariosPage = usuarioRepository.findAll(pageable);
        }
        return pageUsuarioDTO(usuariosPage);
    }

    @Transactional
    public void bloquearCuenta(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setAccountNonLocked(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desbloquearCuenta(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setAccountNonLocked(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void addTaxista(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setRol(Rol.ROL_TAXISTA);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteTaxista(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (!usuarioOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        if (usuario.getRol() == Rol.ROL_TAXISTA) {
            usuario.setRol(Rol.ROL_CLIENTE);
            usuarioRepository.save(usuario);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no tiene el rol de taxista");
        }
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTaxistas() {
        return usuarioRepository.findByRol(Rol.ROL_TAXISTA);
    }
}

