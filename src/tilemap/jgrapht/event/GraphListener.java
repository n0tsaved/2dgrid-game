/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------
 * GraphListener.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package tilemap.jgrapht.event;

/**
 * A listener that is notified when the graph changes.
 *
 * <p>If only notifications on vertex set changes are required it is more
 * efficient to use the VertexSetListener.</p>
 *
 * @author Barak Naveh
 * @see VertexSetListener
 * @since Jul 18, 2003
 */
public interface GraphListener<V, E>
    extends VertexSetListener<V>
{


    /**
     * Notifies that an edge has been added to the graph.
     *
     * @param e the edge event.
     */
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e);

    /**
     * Notifies that an edge has been removed from the graph.
     *
     * @param e the edge event.
     */
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e);
}

// End GraphListener.java
