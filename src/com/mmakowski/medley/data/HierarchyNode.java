/*
 * Created on 15-Jan-2005
 */
package com.mmakowski.medley.data;

import java.util.Iterator;
import java.util.Vector;

import com.mmakowski.medley.resources.Errors;

/**
 * A node of tag/rating hierarchy tree.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/04/24 16:02:45 $
 */
public class HierarchyNode {
	/** a constant indicating that an object is inner (group) node */
	public static final int GROUP = 0;
	/** a constant indicating that an object is a leaf (tag/rating) */
	public static final int ELEMENT = 1;
	
	/** is it a group or an element? */
	private int type;
	/** id of data item represented by this node */
	private int id;
	/** a string with which this tree element should be labelled */
	private String label;
	/** a list of child nodes (null for element nodes) */
	private Vector children;
	
	/**
	 * Create a hierarchy node 
	 * @param type type of node
	 * @param id id of data item
	 * @param label string this element should be labelled with
	 */
	public HierarchyNode(int type, int id, String label) {
		this.type = type;
		this.id = id;
		this.label = label;
		if (type == GROUP) {
			this.children = new Vector();
		} else {
			this.children = null;
		}
	}
	
	/**
	 * Add a child to list of children
	 * @param child the child nod to be added
	 * @throws DataSourceException
	 */
	public void addChild(HierarchyNode child) throws DataSourceException {
		if (type == ELEMENT) {
			throw new DataSourceException(Errors.CANT_ADD_CHILD_TO_ELEMENT, new Object[] {label, child.getLabel()});
		}
		// add only once
		if (!children.contains(child)) {
			children.add(child);
		}
	}
	
	/**
	 * @return Returns the children.
	 */
	public Vector getChildren() {
		return children;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Remove empty subgroups from the subtree.
	 *
	 */
	public void removeEmptySubgroups() {
		if (type != GROUP) {
			return;
		}
		for (Iterator i = children.iterator(); i.hasNext();) {
			HierarchyNode n = (HierarchyNode) i.next();
			n.removeEmptySubgroups();
			if (n.getType() == GROUP && n.getChildren().isEmpty()) {
				i.remove();
			}
		}
	}
	
}
