package main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import main.domain.Prenotazioni;
import main.domain.StatiPrenotazione;
import main.domain.User;
import main.domain.Utenti;
import main.domain.enumeration.StatoCodice;
import main.repository.PrenotazioniRepository;
import main.repository.SaleRepository;
import main.repository.StatiPrenotazioneRepository;
import main.repository.UtentiRepository;
import main.service.mapper.PrenotazioniMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PrenotazioniServiceTest {

    @Mock
    private PrenotazioniRepository prenotazioniRepository;

    @Mock
    private StatiPrenotazioneRepository statiPrenotazioneRepository;

    @Mock
    private UtentiRepository utentiRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PrenotazioniMapper prenotazioniMapper;

    @InjectMocks
    private PrenotazioniService prenotazioniService;

    private UUID prenotazioneId;
    private Prenotazioni prenotazione;
    private Utenti utenteOwner;
    private User userOwner;
    private StatiPrenotazione statoCancelled;

    @BeforeEach
    void setUp() {
        prenotazioneId = UUID.randomUUID();

        userOwner = new User();
        userOwner.setLogin("owner");

        utenteOwner = new Utenti();
        utenteOwner.setUser(userOwner);

        prenotazione = new Prenotazioni();
        prenotazione.setId(prenotazioneId);
        prenotazione.setData(LocalDate.now().plusDays(1));
        prenotazione.setOraInizio(LocalTime.of(10, 0));
        prenotazione.setOraFine(LocalTime.of(11, 0));
        prenotazione.setUtente(utenteOwner);

        statoCancelled = new StatiPrenotazione();
        statoCancelled.setCodice(StatoCodice.CANCELLED);
    }

    @Test
    void delete_shouldSetCancelledState_whenUserIsOwner() throws Exception {
        setAuthenticatedUser("owner");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));
        when(statiPrenotazioneRepository.findByCodice(StatoCodice.CANCELLED)).thenReturn(Optional.of(statoCancelled));
        when(prenotazioniRepository.save(any(Prenotazioni.class))).thenAnswer(invocation -> invocation.getArgument(0));

        prenotazioniService.delete(prenotazioneId);

        verify(prenotazioniRepository).findById(prenotazioneId);
        verify(statiPrenotazioneRepository).findByCodice(StatoCodice.CANCELLED);
        verify(prenotazioniRepository).save(prenotazione);

        assertThat(prenotazione.getStato()).isEqualTo(statoCancelled);
    }

    @Test
    void delete_shouldThrowAccessDenied_whenUserIsNotOwner() {
        setAuthenticatedUser("other-user");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.of(prenotazione));

        assertThatThrownBy(() -> prenotazioniService.delete(prenotazioneId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Utente non ha i permessi");

        verify(prenotazioniRepository).findById(prenotazioneId);
        verify(statiPrenotazioneRepository, never()).findByCodice(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowAccessDenied_whenUserNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> prenotazioniService.delete(prenotazioneId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Utente non autenticato");

        verify(prenotazioniRepository, never()).findById(any());
        verify(statiPrenotazioneRepository, never()).findByCodice(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowEntityNotFound_whenPrenotazioneDoesNotExist() {
        setAuthenticatedUser("owner");

        when(prenotazioniRepository.findById(prenotazioneId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> prenotazioniService.delete(prenotazioneId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Prenotazione non trovata");

        verify(prenotazioniRepository).findById(prenotazioneId);
        verify(statiPrenotazioneRepository, never()).findByCodice(any());
        verify(prenotazioniRepository, never()).save(any());
    }

    private void setAuthenticatedUser(String username) {
        var auth = new UsernamePasswordAuthenticationToken(username, "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
