package es.caib.qssiWeb.controller;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import es.caib.qssiEJB.entity.Motiu;
import es.caib.qssiEJB.interfaces.MotiuServiceInterface;

/**
 * Controlador de la vista Motiu
 * @author [u97091] Toni Juanico Soler
 * data: 20/09/2018
 */

@ManagedBean
@ViewScoped
public class MotiuController {
	
	// EJB's
	@EJB
	MotiuServiceInterface MotiuServ;
	
	
	// Private properties
	private final static Logger LOGGER = Logger.getLogger(MotiuController.class);
	
	private DataTable taula_motius;
	private ArrayList<Motiu> llista_motius;
	
	private String motiuId;
	private String motiuNom;
	private boolean actiu;
	private String message = new String("");
	
	// Getters & Setters
	public void setMessage(String m) { this.message = m; }
	public String getMessage() { return this.message; }
	
	public DataTable getTaula_motius() { return taula_motius;	}
	public void setTaula_motius(DataTable dataTable) { this.taula_motius = dataTable;	}

	public String getMotiuId() { return this.motiuId; }
	public void setMotiuId(String mId) { this.motiuId = mId;}
		
	public String getMotiuNom() { return this.motiuNom; }
	public void setMotiuNom(String n) { this.motiuNom = n; }
		
	public boolean getActiu() { return this.actiu;}
	public void setActiu(boolean a) { this.actiu = a; }
	
	// Methods
	@PostConstruct
	public void init() {
				
		LOGGER.info("Proxy a MotiuController ");
		
		String param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("motiuId_param");
		
		if (param!=null) {
			LOGGER.info("MotiuController amb motiuId_param= "+param);
			this.motiuId = param;
			this.getMotiuInfo(this.motiuId);
		}
		
	}
	
	public ArrayList<Motiu> getLlista_motius() 
	{ 
		LOGGER.info("Obtenim llista de motius ");
		
		try
		{
			LOGGER.info("EJB lookup "+ MotiuServ);	
			
			this.llista_motius = MotiuServ.getLlista_Motius(); // Cridem l'EJB
			
			if (!MotiuServ.getResultat())
			{
				LOGGER.info("error obtingut: "+ MotiuServ.getError());
				this.message = MotiuServ.getError();
			}
		}
		catch (Exception e) {
			LOGGER.info("Error_+ " + e.toString());
			this.message = this.message + " -- " + e.toString();
		}
		
		return this.llista_motius; 
	}
	/* BUG 001 
	public void getMotiuInfo(String motiuId) {
		
		LOGGER.info("getMotiuInfo: " + motiuId);
			
		try
		{
			LOGGER.info("EJB lookup " + MotiuServ);
				
			Motiu m = new Motiu();
			m = MotiuServ.getMotiu(Integer.parseInt(motiuId));
				
			if (MotiuServ.getResultat())
			{
				this.motiuId = motiuId;
				this.motiuNom = m.getNom();
				this.actiu = m.getActiu(); 
			}
			else
			{
				LOGGER.info("error obtingut: "+ MotiuServ.getError());
				FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Error obtenint el motiu",  MotiuServ.getError()));
			}
				
		}
		catch(Exception ex)
		{
			LOGGER.info("error obtingut: "+ ex.toString());
			FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Error obtenint el motiu",  ex.toString()));
		}
	}
	*/

	public void updateMotiu() {
		
		LOGGER.info("updateMotiu");
			
		try 
		{
			HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		    LOGGER.info("EJB lookup" + MotiuServ + "--> motiuId: " + this.motiuId);
		    	
		    // Construim el motiu
		    Motiu m = new Motiu();
		    m.setId(Integer.parseInt(this.motiuId));
			m.setNom(this.motiuNom);
			m.setDatacreacio(new Date());
			m.setUsuari(origRequest.getRemoteUser()); 
			m.setActiu(this.actiu);
							
		    MotiuServ.updateMotiu(m);
		    	
		    //FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
		    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Motiu actualitzat correctament", "Motiu actualitzat correctament"));				
			FacesContext.getCurrentInstance().getExternalContext().redirect(origRequest.getContextPath()  + "/manteniments/motius/llistat_motiu.xhtml");
		} 
		catch (Exception ex) {
			LOGGER.info("Error: " + ex.toString());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error actualitzant el motiu", ex.toString()));
		}
	}

	public void addMotiu()
	{
					
		LOGGER.info("addMotiu ");
			
		try
		{
			LOGGER.info("EJB lookup "+ MotiuServ);	
				
			HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				
			// Contruim el motiu
			Motiu m = new Motiu();
			m.setNom(this.motiuNom);
			m.setDatacreacio(new Date());
			m.setUsuari(origRequest.getRemoteUser()); 
			m.setActiu(this.actiu);
				
			MotiuServ.addMotiu(m); // Cridem l'EJB
					 
			if (MotiuServ.getResultat()==true)
			{
				//FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Motiu afegit correctament", "Motiu afegit correctament"));				
			    FacesContext.getCurrentInstance().getExternalContext().redirect(origRequest.getContextPath()  + "/manteniments/motius/llistat_motiu.xhtml");
			}
			
			else
			{
				
				LOGGER.info("error obtingut: "+ MotiuServ.getError());
				FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Error afegint el motiu",  MotiuServ.getError()));
			}
		} 
		catch (Exception ex) {
				
			LOGGER.info("Error: " + ex.toString());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error desant el motiu", ex.toString()));
		}
	}
	
	public void removeMotiu()
	{
		Motiu m = (Motiu) this.taula_motius.getRowData();
						
		LOGGER.info("Entram a remove amb paràmetre: " + m.getId());
		
		try
		{
			LOGGER.info("EJB lookup " + MotiuServ);
			
			MotiuServ.removeMotiu(m.getId());
		    			
			if (!MotiuServ.getResultat())
			{
				LOGGER.info("Error a remove: " + MotiuServ.getError());
				this.message = this.message + " -- " + MotiuServ.getError();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error eliminant el motiu", this.message));
			}
			else
			{
				FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Motiu eliminat correctament", "Motiu eliminat correctament"));
			}
		}
		catch (Exception e) {
			LOGGER.info("Error_+ " + e.toString());
			this.message = this.message + " -- " + e.toString();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error eliminant el motiu", this.message));
		}
		
	}
}
