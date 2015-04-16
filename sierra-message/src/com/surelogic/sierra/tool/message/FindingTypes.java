package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for FindingTypes complex type.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindingTypes", propOrder = { "findingType", "category" })
public class FindingTypes {
	protected List<FindingType> findingType;
	protected List<Category> category;

	/**
	 * Gets the value of the findingType property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the findingType property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFindingType().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link FindingType }
	 * 
	 * 
	 */
	public List<FindingType> getFindingType() {
		if (findingType == null) {
			findingType = new ArrayList<FindingType>();
		}

		return this.findingType;
	}

	/**
	 * Gets the value of the category property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the category property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCategory().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Category }
	 * 
	 * 
	 */
	@Deprecated
	public List<Category> getCategory() {
		if (category == null) {
			category = new ArrayList<Category>();
		}

		return this.category;
	}
}
