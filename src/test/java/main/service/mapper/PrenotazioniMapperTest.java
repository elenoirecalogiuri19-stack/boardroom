package main.service.mapper;

import static main.domain.PrenotazioniAsserts.*;
import static main.domain.PrenotazioniTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrenotazioniMapperTest {

    private PrenotazioniMapper prenotazioniMapper;

    @BeforeEach
    void setUp() {
        prenotazioniMapper = new PrenotazioniMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPrenotazioniSample1();
        var actual = prenotazioniMapper.toEntity(prenotazioniMapper.toDto(expected));
        assertPrenotazioniAllPropertiesEquals(expected, actual);
    }
}
