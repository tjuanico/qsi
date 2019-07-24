package es.caib.qssiEJB.service;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import es.caib.qssiEJB.entity.Expedient;
import es.caib.qssiEJB.interfaces.ExpedientServiceInterface;


/**
 * Servei (EJB) per a l'entitat Expedient
 * @author [u97091] Antoni Juanico soler
 * data 18/09/2018
 */

@Stateless
@RolesAllowed({"tothom", "QSSI_USUARI", "QSSI_GESTOR", "QSSI_ADMIN"}) // Si tothom -> sobren els altres rols
public class ExpedientService implements ExpedientServiceInterface {

	private final static Logger LOGGER = Logger.getLogger(EscritService.class);

	@PersistenceContext(unitName="qssiDB_PU")
	EntityManager em;
	
	private String strError = new String("");
	private boolean resultat;
	
	@PostConstruct
	public void init() {
		LOGGER.info("Proxy a init: "+this.em);
	}
	
	private boolean getPrimaryKey(Integer any, Expedient e) {
		
				
		try
		{
			// La idea �s obtenir el seg�ent valor de la seq��ncia qsi_expedient_seq_ANY
			// NOTA, Toni Juanico, 20/06/2019 amb la finalitat d'evitar repeticions en els
			// n�meros de seq��ncia la idea �s fer un bloqueig, llegir el n�m. incrementar-lo i actualitzar-lo
			// aix� amb JPA 2.0 �s pot fer mitjan�ant la seg�ent sent�ncia
			// SequenciaExpedient se = em.find(SequenciaExpedient.class,any,LockModeType.PESSIMISTIC_WRITE);
			// per� amb JPA 1.0 no existeix aquesta signatura i per tant ho fem directament amb query's damunt la base de dades (createQueryString);
			// Recordem que a JBoss 5.2 tenim JPA 1.0, JSP 2.1, EJB 3.0, etc.
						
			String strQuery = "select valor from QSI_SEQUENCIA_EXPEDIENT where id_sequencia =" + any +" for update";
			
			LOGGER.info("Obtenim clau primaria " + e.getEmail() + " query: " + strQuery );
			
			Query myQuery = em.createNativeQuery(strQuery);

			Integer val = (Integer) myQuery.getSingleResult();
			
			LOGGER.info("Obtinguda: " + val);
			
			if (val != null) 
			{
				e.setId((any * 100000)+ val);
				LOGGER.info("Calculada clau primaria expedient: " + e.getId());
				
				val = val + 1;
				strQuery = "update QSI_SEQUENCIA_EXPEDIENT set valor=" + val + " where id_sequencia=" + any;
				myQuery = em.createNativeQuery(strQuery);
				myQuery.executeUpdate();
				
				return true;
			}
			else
			{
				// No existeix la seq��ncia per l'any donat
				LOGGER.info("No existeix la seq��ncia per l'any donat");
				return false;
			}
			
		}
		catch(Exception ex)
		{
			LOGGER.info("Error obtenint la seq��ncia: " + ex.toString());
			return false;
		}
	}
	
	private boolean generateSequence(long any) {
		try {
		
			String queryStringCreatePK = new String("INSERT INTO qsi_sequencia_expedient (id_sequencia, valor) values (:num_any, 1)");
			Query queryCreatePK = em.createNativeQuery(queryStringCreatePK);
			queryCreatePK.setParameter("num_any", any);
			queryCreatePK.executeUpdate();
			LOGGER.info("Generada seq��ncia nova: qsi_expedient_seq_" + any);
			return true;
		}
		catch (Exception ex) {
			LOGGER.info("error generant nova seq��ncia: " + ex.toString());
			return false;
		}	
	}
	
	@Override
	public void addExpedient(Expedient e) {
		
		LOGGER.info("in addExpedient, estat entity manager: " + em.toString());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(e.getDataentrada());
		Integer any = calendar.get(Calendar.YEAR);
		
		boolean obtingudaPK = getPrimaryKey(any,e);
		
		if (obtingudaPK)
		{
			try {
				// Afegim l'expedient
				em.persist(e);
				LOGGER.info("Inserit expedient");
				this.resultat = true;
			}
			catch(Exception ex) {
				LOGGER.error(ex);
				this.resultat = false;
				this.strError = ex.toString();
			}
		}
		else
		{
			// Si no hem pogut obtenir la clau prim�ria molt probablement no existeix la seq��ncia, 
			// estem a un any nou i s'ha de generar la seq��ncia
			if (generateSequence(any)){
				obtingudaPK = getPrimaryKey(any,e);
				if (obtingudaPK)
				{
					try {
						// Afegim l'expedient
						em.persist(e);
						LOGGER.info("Inserit expedient");
						this.resultat = true;
					}
					catch(Exception ex) {
						LOGGER.error(ex);
						this.resultat = false;
						this.strError = ex.toString();
					}
				}
			}
			else
			{
				LOGGER.info("No s'ha pogut generar la seq��ncia per a obtenir la clau prim�ria: ");
				this.resultat = false;
			}
		}
	}

	@Override
	public Expedient getExpedient(Integer id_expedient) {
		try
		{
			LOGGER.info("in getExpedient, estat entity manager: " + em.toString());
			Expedient e = em.find(Expedient.class, id_expedient);
			this.resultat = true;
			return e;
		}
		catch (Exception ex)
		{
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString();
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Expedient> getLlista_Expedients(TipusCerca tc) {
		
		ArrayList<Expedient> l = new ArrayList<Expedient>();
		
		String queryString = new String();
		String estats_cercats;
		
		switch (tc)
		{
			case PENDENTS_ASSIGNAR_PER_CENTRE:
				estats_cercats = ExpedientServiceInterface.EstatExpedient.ASSIGNAT_EQUIP_FILTRATGE.getValue() + ", " + 
						          ExpedientServiceInterface.EstatExpedient.ASSIGNAT_RESPONSABLE_CONSELLERIA.getValue();
				
				queryString = new String("select e from Expedient e where e.id_estat in (" + estats_cercats + ") order by id_centre, id_subcentre, id_expedient");
				break;
			case PENDENTS_ASSIGNAR_PER_ESTAT:
				estats_cercats = ExpedientServiceInterface.EstatExpedient.ASSIGNAT_EQUIP_FILTRATGE.getValue() + ", " + 
						          ExpedientServiceInterface.EstatExpedient.ASSIGNAT_RESPONSABLE_CONSELLERIA.getValue();
				
				queryString = new String("select e from Expedient e where e.id_estat in (" + estats_cercats + ") order by id_estat");
				break;
			case PENDENTS_RESPOSTA: // Ojo, aquest tipus de cerca l'hem de clarificar!!!!!, Toni Juanico, 09/07/2019
				estats_cercats = ExpedientServiceInterface.EstatExpedient.ASSIGNAT_TRAMITADOR.getValue() + " ";
				queryString = new String("select e from Expedient e where e.id_estat in (" + estats_cercats + ") order by data_entrada");
				break;
			case REBUTJADES:
				estats_cercats = ExpedientServiceInterface.EstatExpedient.REBUTJADA.getValue() + " ";
				queryString = new String("select e from Expedient e where e.id_estat in (" + estats_cercats + ") order by data_entrada");
				break;
			case FINALITZADES:
				estats_cercats = ExpedientServiceInterface.EstatExpedient.FINALITZADA.getValue() + " ";
				queryString = new String("select e from Expedient e where e.id_estat in (" + estats_cercats + ") order by data_entrada");
				break;
			case TOTES_PER_CENTRE:
				queryString = new String("select e from Expedient e order by id_centre, id_subcentre, data_entrada");
				break;
			case TOTES_PER_ESTAT:
				queryString = new String("select e from Expedient e order by id_estat");
				break;
			default:
				queryString = null;
				
		}
		
		try
		{
						
			LOGGER.info("in getLlista_Expedients, estat entity manager: " + em.toString());
			LOGGER.info("Query seleccionada: " + queryString);
			
			l = (ArrayList<Expedient>) em.createQuery(queryString).getResultList();
			
			LOGGER.info("em operation done ");
			this.resultat = true;
			
		}
		catch (Exception ex)
		{
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString(); 
			
		}
		
		return l;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Expedient> getLlista_Expedients_assignats_usuari(String usuari) {
		
		ArrayList<Expedient> l = new ArrayList<Expedient>();
		
		String queryString = new String("select e from Expedient e where e.usuari_assignat = :u order by id_expedient");
		try
		{
						
			LOGGER.info("in getLlista_Expedients_assignats_usuari, estat entity manager: " + em.toString());
			Query q = em.createQuery(queryString);
			q.setParameter("u", usuari);
			l = (ArrayList<Expedient>) q.getResultList();
			
			LOGGER.info("em operation done ");
			this.resultat = true;
			
		}
		catch (Exception ex)
		{
			LOGGER.error(ex);
			this.resultat = false;
			this.strError = ex.toString(); 
			
		}
		
		return l;
	}
	
	@Override
	public AccioExpedient[] getAccionsDisponiblesExpedient (EstatExpedient e) {
		AccioExpedient[] llista_accions = null;
		
		switch (e)
		{
		case ASSIGNAT_EQUIP_FILTRATGE: 
			llista_accions = new AccioExpedient[2];
			AccioExpedient accio1 = ExpedientServiceInterface.AccioExpedient.ASSIGNAR_CONSELLERIA;
			AccioExpedient accio2 = ExpedientServiceInterface.AccioExpedient.REBUTJAR;
			llista_accions[0] = accio1;
			llista_accions[1] = accio2;
			
			break;
		case ASSIGNAT_RESPONSABLE_CONSELLERIA:
			llista_accions = new AccioExpedient[3];
			AccioExpedient accio3 = ExpedientServiceInterface.AccioExpedient.ASSIGNAR_TRAMITADOR;
			AccioExpedient accio4 = ExpedientServiceInterface.AccioExpedient.RETORNAR_EQUIP_FILTRATGE;
			AccioExpedient accio5 = ExpedientServiceInterface.AccioExpedient.REBUTJAR;
			
			llista_accions[0] = accio3;
			llista_accions[1] = accio4;
			llista_accions[2] = accio5;
			break;
		case ASSIGNAT_TRAMITADOR:
			llista_accions = new AccioExpedient[3];
			AccioExpedient accio6 = ExpedientServiceInterface.AccioExpedient.TRAMITAR_RESPOSTA;
			AccioExpedient accio7 = ExpedientServiceInterface.AccioExpedient.RETORNAR_RESPONSABLE_CONSELLERIA;
			AccioExpedient accio8 = ExpedientServiceInterface.AccioExpedient.REBUTJAR;
			
			llista_accions[0] = accio6;
			llista_accions[1] = accio7;
			llista_accions[2] = accio8;
			break;
		case FINALITZADA:
			llista_accions = null;
			break;
		case REBUTJADA:
			llista_accions = null;
			break;
		default:
			llista_accions = null;
		}
		
        return llista_accions;
	}
	
	@Override
	public void assignarCentreExpedient(Integer id_expedient, Integer id_centre) {
		
		LOGGER.info("in assignarCentreExpedient, estat entity manager: " + em.toString());
		
		String queryString = new String("select id_centre FROM qsi_expedient where id_expedient = :id_expedient");
		String updateQueryString;
		
		
		try {
			
			Query query = em.createNativeQuery(queryString);
			query.setParameter("id_expedient", id_expedient);
			Integer id_centre_anterior = (Integer) query.getSingleResult();
			LOGGER.info("centre anterior: " + id_centre_anterior + ", centre nou: " + id_centre);
			
			if (id_centre_anterior.equals(id_centre))
			{
				// La unitat de filtratge no ha canviat el centre seleccionat inicialment
				// Nom�s canviam l'estat de l'expedient	
				LOGGER.info("kiki");
				updateQueryString = new String("UPDATE qsi_expedient set id_centre = :id_centre, id_estat = :estat where id_expedient = :id_expedient");
			}
			else
			{
				// La unitat de filtratge ha canviat el centre seleccionat inicialment
				// Canviam el centre, posem el subcentre a null i canviam l'estat
				LOGGER.info("bubu");
				updateQueryString = new String("UPDATE qsi_expedient set id_centre = :id_centre, id_subcentre = null, id_estat= :estat where id_expedient = :id_expedient");
			}
			
			Query updateQuery = em.createNativeQuery(updateQueryString);
			updateQuery.setParameter("id_expedient", id_expedient);
			updateQuery.setParameter("id_centre", id_centre);
			updateQuery.setParameter("estat", ExpedientServiceInterface.EstatExpedient.ASSIGNAT_RESPONSABLE_CONSELLERIA.getValue());
			
			updateQuery.executeUpdate();	
			
			this.resultat = true;	
		}
		catch(Exception ex)
		{
			this.strError = ex.toString();
			this.resultat = false;
		}
		
	}
	
	@Override
	public void assignarTramitador(Integer id_expedient, Integer id_subcentre,  String unitat_organica, String usuari) {
		
		LOGGER.info("in assignarTramitador, estat entity manager: " + em.toString());
		String queryString;
		
		queryString = new String("UPDATE qsi_expedient set id_subcentre = :id_subcentre, unitat_organica = :unitat_organica, usuari_assignat = :usuari_assignat, id_estat= :estat where id_expedient = :id_expedient");
		
		try {
			
			Query query = em.createNativeQuery(queryString);
			query.setParameter("id_expedient", id_expedient);
			query.setParameter("id_subcentre", id_subcentre);
			query.setParameter("unitat_organica", unitat_organica);
			query.setParameter("usuari_assignat", usuari);
			query.setParameter("estat", ExpedientServiceInterface.EstatExpedient.ASSIGNAT_TRAMITADOR.getValue());
			
			query.executeUpdate();
			this.resultat = true;	
		}
		catch(Exception ex)
		{
			this.strError = ex.toString();
			this.resultat = false;
		}
	}
	
	@Override
	public void desarRespostaExpedient(Integer id_expedient, String text_resposta)
	{
		LOGGER.info("in desarRespostaExpedient, estat entity manager: " + em.toString());
		String queryString;
		
		queryString = new String("UPDATE qsi_expedient set text_resposta = :text_resposta, data_resposta = :data_resposta where id_expedient = :id_expedient");
		
		try {
			Date data_resposta = new Date();
			Query query = em.createNativeQuery(queryString);
			query.setParameter("id_expedient", id_expedient);
			query.setParameter("text_resposta", text_resposta);
			query.setParameter("data_resposta", data_resposta);
			
			query.executeUpdate();
			this.resultat = true;	
		}
		catch(Exception ex)
		{
			this.strError = ex.toString();
			this.resultat = false;
		}
	}
	
	@Override
	public void tancarExpedient(Integer id_expedient)
	{
		LOGGER.info("in tancarExpedient, estat entity manager: " + em.toString());
		String queryString;
		
		// Obtenir dades expedient
		
		try {
			// Generar pdf
			FileOutputStream myStream = new FileOutputStream("a.pdf");
			Document document = new Document();
			LOGGER.info("1");
			PdfWriter.getInstance(document, myStream);
			LOGGER.info("2");
			document.open();
			LOGGER.info("3");
			document.add(new Paragraph("Hello World!"));
			LOGGER.info("4");
			document.close();
			
			// Enviar a arxiu
			
			// Enviar a usuari
			
			this.resultat = true;
		}
		catch(Exception ex)
		{
			this.strError = ex.toString();
			this.resultat = false;
		}
	}
	
	@Override
	public boolean getResultat() {return this.resultat;	}

	@Override
	public String getError() { return this.strError; }

}
