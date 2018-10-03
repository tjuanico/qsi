package es.caib.qssiEJB.interfaces;

import java.util.ArrayList;

import javax.ejb.Local;

import es.caib.qssiEJB.entity.Expedient;

/**
 * Interf�cie del servei (EJB) ExpedientService - �s el core de l'aplicaci�
 * @author [u97091] Toni Juanico Soler
 * data: 18/09/2018
 */

@Local
public interface ExpedientServiceInterface {
	public void addExpedient(Expedient e);
	public ArrayList<Expedient> getLlista_Expedients();
	public boolean getResultat();
	public String getError();
}
