package com.luxeride.taxistfg.Controller;



import com.itextpdf.text.DocumentException;
import com.luxeride.taxistfg.Model.*;
import com.luxeride.taxistfg.Repository.ViajeRepository;
import com.luxeride.taxistfg.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.luxeride.taxistfg.Util.LoginRequest;
import com.luxeride.taxistfg.JWT.AuthResponse;
import com.luxeride.taxistfg.Repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class ControladorUsuario {
    private static final Logger logger = LoggerFactory.getLogger(ControladorUsuario.class);

    @Autowired
    private final UsuarioService usuarioService;
    @Autowired
    private final AuthService authService;
    @Autowired
    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final CocheService cocheService;
    @Autowired
    private final ServicioService servicioService;
    @Autowired
    private final ViajeService viajeService;
    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private PdfService pdfService;

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsario(@RequestBody Usuario usuario) {
        try {
            usuarioService.registrarUsario(usuario);
            return ResponseEntity.ok("Usuario registrado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/iniciar")
    public ResponseEntity<AuthResponse> iniciar(@RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(authResponse);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(new AuthResponse(null, "Usuario no encontrado"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(new AuthResponse(null, "Tu cuenta está bloqueada. Contacta con el soporte"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new AuthResponse(null, "Error en el inicio de sesión"));
        }
    }
    

    @GetMapping("/info")
    public ResponseEntity<?> obtenerInformacion() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (String) auth.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidos(),
                    usuario.getDni(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    usuario.isAccountNonLocked()
            );
    
            return ResponseEntity.ok(usuarioDTO);
        } catch (UsernameNotFoundException e) {
            logger.error("Usuario no encontrado", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        } catch (Exception e) {
            logger.error("Error al obtener información del usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la información del usuario");
        }
    }
    @GetMapping("/enServicio")
    public List<CocheDTO> obtenerCochesEnServicio() {
        return cocheService.listarCochesEnServicio();
    }

    @GetMapping("/allServicios")
    public List<Servicio> obtenerServicios(){
        return servicioService.obtenerServicios();
    }

    @PostMapping("/hacerViaje")
    public ResponseEntity<String> registrarViaje(@RequestBody Viaje viaje) {
        try {
            viajeService.registrarViaje(viaje);
            return ResponseEntity.ok("Viaje registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error al registrar el viaje: " + e.getMessage());
        }
    }
    @GetMapping("/generarPDF/{idCliente}")
    public ResponseEntity<byte[]> generarPdfUltimoViaje(@PathVariable Integer idCliente) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ROL_ADMIN"));

            if (!esAdmin) {
                String email = (String) auth.getPrincipal();
                Usuario usuarioAutenticado = usuarioRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
                if (!usuarioAutenticado.getId().equals(idCliente)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            // Obtener todos los viajes del cliente ordenados por ID descendente
            List<Viaje> viajes = viajeRepository.findViajesCliente(idCliente);

            if (viajes.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Tomar solo el primer viaje (el más reciente)
            Viaje viaje = viajes.get(0);

            // Generar el PDF
            byte[] pdfBytes = pdfService.generarPdfViaje(viaje);

            // Configurar las cabeceras de la respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // Nombre del archivo: ticket_viaje_[fecha].pdf
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String fechaFormateada = viaje.getHoraLlegada().format(formatter);
            String filename = "ticket_viaje_" + fechaFormateada + ".pdf";

            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}