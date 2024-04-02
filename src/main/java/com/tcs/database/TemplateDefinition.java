/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.database;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.tcs.constants.DatabaseConstants;

/**
 * The Class TemplateDefinition.
 */
@Entity
@Table(name = "templatedefinition",indexes = {@Index(columnList = "templateid", unique = false,name="templateidindex"),
		@Index(name="templatenameindex",columnList = "templatename",unique = false),
		@Index(name="filetypeqnameindex",columnList = "filetypeqname",unique = false)})
public class TemplateDefinition implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The templateid. */
	@Id
	@Column(name = "templateid")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long templateid;
	
	/** The templatename. */
	@Column(name = "templatename")
	private String templatename;
	
	/** The filetypeqname. */
	@Column(name = "filetypeqname")
	private String filetypeqname;
	
	/** The filetype. */
	@Column(name = "filetype")
	private String filetype;
	
	/** The templatedetails. */
	@OneToMany(fetch = FetchType.LAZY,mappedBy="templatedefinition",cascade = CascadeType.ALL)
	private Set<TemplateDetails> templatedetails;
	
	/** The filecolumn. */
	@OneToOne(fetch = FetchType.LAZY,mappedBy = "templatedefinition", cascade = CascadeType.ALL)
	private FileColumn filecolumn;
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
    public String toString() {
		final JSONObject csvFileJson = new JSONObject();
		csvFileJson.put(DatabaseConstants.TEMPLATE_NAME, this.templatename);
		csvFileJson.put(DatabaseConstants.FILETYPE_QNAME, this.filetypeqname);
		csvFileJson.put(DatabaseConstants.FILETYPE, this.filetype);
		csvFileJson.put(DatabaseConstants.TEMPLATE_ID, this.templateid);
        return csvFileJson.toString();
    }

	/**
	 * Gets the filetypeqname.
	 *
	 * @return the filetypeqname
	 */
	public String getFiletypeqname() {
		return filetypeqname;
	}

	/**
	 * Sets the filetypeqname.
	 *
	 * @param filetypeqname the new filetypeqname
	 */
	public void setFiletypeqname(final String filetypeqname) {
		this.filetypeqname = filetypeqname;
	}

	/**
	 * Gets the templateid.
	 *
	 * @return the templateid
	 */
	public long getTemplateid() {
		return templateid;
	}

	/**
	 * Sets the templateid.
	 *
	 * @param templateid the new templateid
	 */
	public void setTemplateid(final long templateid) {
		this.templateid = templateid;
	}

	/**
	 * Gets the templatename.
	 *
	 * @return the templatename
	 */
	public String getTemplatename() {
		return templatename;
	}

	/**
	 * Sets the templatename.
	 *
	 * @param templatename the new templatename
	 */
	public void setTemplatename(final String templatename) {
		this.templatename = templatename;
	}

	/**
	 * Gets the filetype.
	 *
	 * @return the filetype
	 */
	public String getFiletype() {
		return filetype;
	}

	/**
	 * Sets the filetype.
	 *
	 * @param filetype the new filetype
	 */
	public void setFiletype(final String filetype) {
		this.filetype = filetype;
	}

	/**
	 * Gets the templatedetails.
	 *
	 * @return the templatedetails
	 */
	public Set<TemplateDetails> getTemplatedetails() {
		return templatedetails;
	}

	/**
	 * Sets the templatedetails.
	 *
	 * @param templatedetails the new templatedetails
	 */
	public void setTemplatedetails(final Set<TemplateDetails> templatedetails) {
		this.templatedetails = templatedetails;
	}

	/**
	 * Gets the filecolumn.
	 *
	 * @return the filecolumn
	 */
	public FileColumn getFilecolumn() {
		return filecolumn;
	}

	/**
	 * Sets the filecolumn.
	 *
	 * @param filecolumn the new filecolumn
	 */
	public void setFilecolumn(final FileColumn filecolumn) {
		this.filecolumn = filecolumn;
	}


}
