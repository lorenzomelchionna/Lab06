package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private MeteoDAO dao;
	
	private Map<Integer,String> Mesi = new LinkedHashMap<Integer,String>();
	
	private Set<Citta> CittaDiPartenza = new HashSet<>();
	private List<Citta> SequenzaMigliore;
	int costoSequenzaMigliore;
	int giorniConsecutivi;
	
	public Model() {
		
		dao = new MeteoDAO();
		
		Mesi.put(1, "Gennaio");
		Mesi.put(2, "Febbraio");
		Mesi.put(3, "Marzo");
		Mesi.put(4, "Aprile");
		Mesi.put(5, "Maggio");
		Mesi.put(6, "Giugno");
		Mesi.put(7, "Luglio");
		Mesi.put(8, "Agosto");
		Mesi.put(9, "Settembre");
		Mesi.put(10, "Ottobre");
		Mesi.put(11, "Novembre");
		Mesi.put(12, "Dicembre");
		
		CittaDiPartenza.add(new Citta("Genova"));
		CittaDiPartenza.add(new Citta("Milano"));
		CittaDiPartenza.add(new Citta("Torino"));
		
	}
	
	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		
		String output = "Umidità media nel mese di "+Mesi.get(mese)+":\n";
		
		List<Rilevamento> RilGenova = dao.getAllRilevamentiLocalitaMese(mese, "Genova");
		double mediaGenova = 0;
		
		for(Rilevamento r : RilGenova)
			mediaGenova += r.getUmidita();
		
		mediaGenova /= RilGenova.size();
		
		output += "Genova: "+mediaGenova+"\n";
		
		List<Rilevamento> RilTorino = dao.getAllRilevamentiLocalitaMese(mese, "Torino");
		double mediaTorino = 0;
		
		for(Rilevamento r : RilTorino)
			mediaTorino += r.getUmidita();
		
		mediaTorino /= RilTorino.size();
		
		output += "Torino: "+mediaTorino+"\n";
		
		List<Rilevamento> RilMilano = dao.getAllRilevamentiLocalitaMese(mese, "Milano");
		double mediaMilano = 0;
		
		for(Rilevamento r : RilMilano)
			mediaMilano += r.getUmidita();
		
		mediaMilano /= RilMilano.size();
		
		output += "Milano: "+mediaMilano+"\n";
		
		return output;
		
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		
		for(Citta c : CittaDiPartenza)
			c.setRilevamenti(dao.getAllRilevamentiLocalitaMese(mese, c.getNome()));
			
		List<Citta> Parziale = new ArrayList<>();
		SequenzaMigliore = new ArrayList<>();
		costoSequenzaMigliore = -1;
		giorniConsecutivi = 1;
		String stringaSequenza = "Sequenza dal Minimo costo nel mese di "+Mesi.get(mese)+":\n";
		
		cercaSequenza(Parziale, 0, mese);
		
		for(Citta c : SequenzaMigliore)
			stringaSequenza += c.getNome()+"-";
		
		return stringaSequenza;
		
	}

	private void cercaSequenza(List<Citta> parziale, int livello, int mese) {
		
		if(livello == NUMERO_GIORNI_TOTALI) {
			
			for(Citta c : CittaDiPartenza)
				if(!parziale.contains(c))
					return;
			
			int costo = calcolaCosto(parziale);
			
			if(costo < costoSequenzaMigliore || costoSequenzaMigliore == -1) {
				
				SequenzaMigliore = new ArrayList<Citta>(parziale);
				costoSequenzaMigliore = costo;
				
			}
			
			return;
			
		}
		
		if(giorniConsecutivi(parziale) < NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN) {
			
			if(livello == 0) {
				
				for(Citta c : CittaDiPartenza) {
					
					parziale.add(c);
					cercaSequenza(parziale, livello+1, mese);
					parziale.remove(c);
					//cercaSequenza(parziale, livello, mese);
					
				}
				
				return;
				
			} else {
			
				parziale.add(parziale.get(livello-1));
				cercaSequenza(parziale, livello+1, mese);
				parziale.remove(parziale.get(livello));
				
				return;
				
			}
			
		} else {
		
			for(Citta c : CittaDiPartenza) {
			
				if(calcolaGiorniCitta(parziale, c) < NUMERO_GIORNI_CITTA_MAX) {
			
					parziale.add(c);
					cercaSequenza(parziale, livello+1, mese);
					parziale.remove(c);
				
				}
			
			}
			
			return;
			
		}
		
	}

	private int calcolaCosto(List<Citta> parziale) {
		
		int costo = 0;
		
		
		for(int i=0; i<parziale.size(); i++) {
			
			if(i>0)
				if(!parziale.get(i).equals(parziale.get(i-1)))
					costo += COST;
			
			costo += parziale.get(i).getRilevamenti().get(i).getUmidita();
			
		}
		
		return costo;
		
	}
	
	private int calcolaGiorniCitta(List<Citta> parziale, Citta daCalcolare) {
			
		int giorni = 0;
		
		for(Citta c : parziale)
			if(c.equals(daCalcolare))
				giorni++;
		
		return giorni;
		
	}
	
	private int giorniConsecutivi(List<Citta> parziale) {
		
		int giorni = 1;
		
		if(parziale.size() > 1) {
			
			for(int i = parziale.size(); i>2; i--) {
			
				if(parziale.get(i-1).equals(parziale.get(i-2)))
					giorni++;
				else
					break;
			
			}
			
		}
		
		return giorni;
		
	}

}
