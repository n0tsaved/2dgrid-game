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
/* -------------------------
 * MaskEdgeSet.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 *
 */
package tilemap.jgrapht.graph;

import tilemap.jgrapht.*;
import tilemap.jgrapht.util.*;
import tilemap.jgrapht.util.PrefetchIterator.NextElementFunctor;

import java.util.*;


/**
 * Helper for {@link MaskSubgraph}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
class MaskEdgeSet<V, E>
    extends AbstractSet<E>
{


    private Set<E> edgeSet;

    private Graph<V, E> graph;

    private MaskFunctor<V, E> mask;

    private transient TypeUtil<E> edgeTypeDecl = null;

    private int size;



    public MaskEdgeSet(
        Graph<V, E> graph,
        Set<E> edgeSet,
        MaskFunctor<V, E> mask)
    {
        this.graph = graph;
        this.edgeSet = edgeSet;
        this.mask = mask;
        this.size = -1;
    }



    /**
     * @see Collection#contains(Object)
     */
    @Override public boolean contains(Object o)
    {
        return this.edgeSet.contains(o)
            && !this.mask.isEdgeMasked(TypeUtil.uncheckedCast(o, edgeTypeDecl));
    }

    /**
     * @see Set#iterator()
     */
    @Override public Iterator<E> iterator()
    {
        return new PrefetchIterator<E>(new MaskEdgeSetNextElementFunctor());
    }

    /**
     * @see Set#size()
     */
    @Override public int size()
    {
        if (this.size == -1) {
            this.size = 0;
            for (Iterator<E> iter = iterator(); iter.hasNext();) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }



    private class MaskEdgeSetNextElementFunctor
        implements NextElementFunctor<E>
    {
        private Iterator<E> iter;

        public MaskEdgeSetNextElementFunctor()
        {
            this.iter = tilemap.jgrapht.graph.MaskEdgeSet.this.edgeSet.iterator();
        }

        @Override public E nextElement()
            throws NoSuchElementException
        {
            E edge = this.iter.next();
            while (isMasked(edge)) {
                edge = this.iter.next();
            }
            return edge;
        }

        private boolean isMasked(E edge)
        {
            return tilemap.jgrapht.graph.MaskEdgeSet.this.mask.isEdgeMasked(edge)
                || tilemap.jgrapht.graph.MaskEdgeSet.this.mask.isVertexMasked(
                    tilemap.jgrapht.graph.MaskEdgeSet.this.graph.getEdgeSource(edge))
                || tilemap.jgrapht.graph.MaskEdgeSet.this.mask.isVertexMasked(
                    tilemap.jgrapht.graph.MaskEdgeSet.this.graph.getEdgeTarget(edge));
        }
    }
}

// End MaskEdgeSet.java
