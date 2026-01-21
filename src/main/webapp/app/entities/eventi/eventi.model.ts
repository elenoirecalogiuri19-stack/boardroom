import { IPrenotazioni } from 'app/entities/prenotazioni/prenotazioni.model';
import { TipoEvento } from 'app/entities/enumerations/tipo-evento.model';

export interface IEventi {
  id: string;
  titolo?: string | null;
  tipo?: keyof typeof TipoEvento | null;
  prezzo?: number | null;
  prenotazione?: Pick<IPrenotazioni, 'id'> | null;
}

export type NewEventi = Omit<IEventi, 'id'> & { id: null };
