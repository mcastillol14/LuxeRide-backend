package com.luxeride.taxistfg.Model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void getAuthoritiesDevuelveElRolConPrefijoRole() {
        Usuario usuario = new Usuario();
        usuario.setRol(Rol.ROL_TAXISTA);

        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ROL_TAXISTA");
    }

    @Test
    void getUsernameDevuelveElEmail() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@luxeride.com");

        assertThat(usuario.getUsername()).isEqualTo("cliente@luxeride.com");
    }

    @Test
    void isAccountNonLockedReflejaElCampoAccountNonLocked() {
        Usuario usuario = new Usuario();

        usuario.setAccountNonLocked(false);
        assertThat(usuario.isAccountNonLocked()).isFalse();

        usuario.setAccountNonLocked(true);
        assertThat(usuario.isAccountNonLocked()).isTrue();
    }

    @Test
    void expiracionCredencialesYEnabledSiempreDevuelvenTrue() {
        Usuario usuario = new Usuario();

        assertThat(usuario.isAccountNonExpired()).isTrue();
        assertThat(usuario.isCredentialsNonExpired()).isTrue();
        assertThat(usuario.isEnabled()).isTrue();
    }
}
