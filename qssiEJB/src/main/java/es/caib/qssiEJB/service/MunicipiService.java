package es.caib.qssiEJB.service;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import es.caib.qssiEJB.entity.Municipi;
import es.caib.qssiEJB.interfaces.MunicipiServiceInterface;

/**
 * Servei (EJB) per a l'entitat Municipi
 * @author [u97091] Antoni Juanico soler
 * data 11/09/2018
 */

@Stateless
@RolesAllowed({"tothom", "QSSI_USUARI", "QSSI_GESTOR", "QSSI_ADMIN", "PBASE_ADMIN","JBOSSADMIN"}) // Si tothom -> sobren els altres rols
public class MunicipiService implements MunicipiServiceInterface {
	
	private final static Logger LOGGER = Logger.getLogger(EscritService.class);
	
	@PersistenceContext(unitName="qssiDB_PU")
	EntityManager em;
	
	private String strError = new String("");
	private boolean resultat;
	
	@PostConstruct
	public void init() {
		LOGGER.info("Proxy a init: "+this.em);
	}
	
	@Override
	public void addMunicipi(Municipi m) {
		
		LOGGER.info("in addMunicipi, estat entity manager: " + em.toString());
		
		try
		{
			em.persist(m);
			LOGGER.info("Inserit municipi");
			this.resultat = true;
		}
		catch(Exception ex) {
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString();
		}	

	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Municipi> getLlista_MunicipisActius(Integer id_provincia) {
		
		ArrayList<Municipi> lm = new ArrayList<Municipi>();
		
		String queryString = new String("select m from Municipi m where m.actiu=1 and m.provincia.id_provincia = :id_provincia order by m.nom ");

		try
		{
						
			LOGGER.info("in getLlista_MunicipisActius, parÓmetre id_provincia: " + id_provincia + " estat entity manager: " + em.toString());
			
		    Query query = em.createQuery(queryString);
		    query.setParameter("id_provincia", id_provincia);
		    lm = (ArrayList<Municipi>) query.getResultList();
		    			
			LOGGER.info("em operation done ");
			this.resultat = true;
			
		}
		catch (Exception ex)
		{
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString(); 
			
		}
		
		return lm;
	}

	@Override
	public boolean getResultat() { return this.resultat; }

	@Override
	public String getError() { return this.strError; }

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Municipi> getLlista_Municipis(Integer id_provincia) {
		
		ArrayList<Municipi> lm = new ArrayList<Municipi>();
		String queryString = new String("select m from Municipi m where m.provincia.id_provincia = :id_provincia order by m.nom ");
		try
		{
						
			LOGGER.info("in getLlista_Municipis, parÓmetre id_provincia: " + id_provincia + " estat entity manager: " + em.toString());
			
		    Query query = em.createQuery(queryString);
		    query.setParameter("id_provincia", id_provincia);
		    lm = (ArrayList<Municipi>) query.getResultList();
		    			
			LOGGER.info("em operation done ");
			this.resultat = true;
			
		}
		catch (Exception ex)
		{
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString(); 
			
		}
		
		return lm;
	}

}
