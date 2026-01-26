package main.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.Utenti;
import main.repository.PrenotazioniRepository;
import main.security.AuthoritiesConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrenotazioniServiceTest {

    @Mock
    private PrenotazioniRepository prenotazioniRepository;

    @InjectMocks
    private PrenotazioniService prenotazioniService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void delete_ShouldThrowAccessDeniedException_WhenUserIsNotOwnerNorAdmin() {
        UUID prenId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Prenotazioni p = new Prenotazioni();
        p.setId(prenId);
        Utenti owner = new Utenti();
        owner.setId(ownerId);
        p.setUtente(owner);

        when(prenotazioniRepository.findById(prenId)).thenReturn(Optional.of(p));

        Authentication authentication = mock(Authentication.class);
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        String callerUtenteId = UUID.randomUUID().toString();

        assertThrows(AccessDeniedException.class, () -> prenotazioniService.delete(prenId));
        verify(prenotazioniRepository, never()).deleteById(prenId);
    }

    @Test
    void delete_ShouldNotThrow_WhenUserIsOwner() {
        UUID prenId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Prenotazioni p = new Prenotazioni();
        p.setId(prenId);
        Utenti owner = new Utenti();
        owner.setId(ownerId);
        p.setUtente(owner);

        when(prenotazioniRepository.findById(prenId)).thenReturn(Optional.of(p));
        doNothing().when(prenotazioniRepository).deleteById(prenId);

        Authentication authentication = mock(Authentication.class);
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        String callerUtenteId = ownerId.toString();

        assertDoesNotThrow(() -> prenotazioniService.delete(prenId));
        verify(prenotazioniRepository, times(1)).deleteById(prenId);
    }

    @Test
    void delete_ShouldNotThrow_WhenUserIsAdmin() {
        UUID prenId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Prenotazioni p = new Prenotazioni();
        p.setId(prenId);
        Utenti owner = new Utenti();
        owner.setId(ownerId);
        p.setUtente(owner);

        when(prenotazioniRepository.findById(prenId)).thenReturn(Optional.of(p));
        doNothing().when(prenotazioniRepository).deleteById(prenId);

        Authentication authentication = mock(Authentication.class);
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN);
        doReturn(Collections.singletonList(adminAuthority)).when(authentication).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        String callerUtenteId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> prenotazioniService.delete(prenId));
        verify(prenotazioniRepository, times(1)).deleteById(prenId);
    }
}
