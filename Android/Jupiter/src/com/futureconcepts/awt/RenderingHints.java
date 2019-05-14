/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.futureconcepts.awt;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.ref.WeakReference;

/**
 * The {@code RenderingHints} class defines and manages collections of
 * keys and associated values which allow an application to provide input
 * into the choice of algorithms used by other classes which perform
 * rendering and image manipulation services.
 * The {@link java.awt.Graphics2D} class, and classes that implement
 * {@link java.awt.image.BufferedImageOp} and
 * {@link java.awt.image.RasterOp} all provide methods to get and
 * possibly to set individual or groups of {@code RenderingHints}
 * keys and their associated values.
 * When those implementations perform any rendering or image manipulation
 * operations they should examine the values of any {@code RenderingHints}
 * that were requested by the caller and tailor the algorithms used
 * accordingly and to the best of their ability.
 * <p>
 * Note that since these keys and values are <i>hints</i>, there is
 * no requirement that a given implementation supports all possible
 * choices indicated below or that it can respond to requests to
 * modify its choice of algorithm.
 * The values of the various hint keys may also interact such that
 * while all variants of a given key are supported in one situation,
 * the implementation may be more restricted when the values associated
 * with other keys are modified.
 * For example, some implementations may be able to provide several
 * types of dithering when the antialiasing hint is turned off, but
 * have little control over dithering when antialiasing is on.
 * The full set of supported keys and hints may also vary by destination
 * since runtimes may use different underlying modules to render to
 * the screen, or to {@link java.awt.image.BufferedImage} objects,
 * or while printing.
 * <p>
 * Implementations are free to ignore the hints completely, but should
 * try to use an implementation algorithm that is as close as possible
 * to the request.
 * If an implementation supports a given algorithm when any value is used
 * for an associated hint key, then minimally it must do so when the
 * value for that key is the exact value that specifies the algorithm.
 * <p>
 * The keys used to control the hints are all special values that
 * subclass the associated {@link RenderingHints.Key} class.
 * Many common hints are expressed below as static constants in this
 * class, but the list is not meant to be exhaustive.
 * Other hints may be created by other packages by defining new objects
 * which subclass the {@code Key} class and defining the associated values.
 */
public class RenderingHints
    implements Map<Object,Object>, Cloneable
{
    /**
     * Defines the base type of all keys used along with the
     * {@link RenderingHints} class to control various
     * algorithm choices in the rendering and imaging pipelines.
     * Instances of this class are immutable and unique which
     * means that tests for matches can be made using the
     * {@code ==} operator instead of the more expensive
     * {@code equals()} method.
     */
    public abstract static class Key {
        private static HashMap identitymap = new HashMap(17);

        private String getIdentity() {
            // Note that the identity string is dependent on 3 variables:
            //     - the name of the subclass of Key
            //     - the identityHashCode of the subclass of Key
            //     - the integer key of the Key
            // It is theoretically possible for 2 distinct keys to collide
            // along all 3 of those attributes in the context of multiple
            // class loaders, but that occurence will be extremely rare and
            // we account for that possibility below in the recordIdentity
            // method by slightly relaxing our uniqueness guarantees if we
            // end up in that situation.
            return getClass().getName()+"@"+
                Integer.toHexString(System.identityHashCode(getClass()))+":"+
                Integer.toHexString(privatekey);
        }

        private synchronized static void recordIdentity(Key k) {
            Object identity = k.getIdentity();
            Object otherref = identitymap.get(identity);
            if (otherref != null) {
                Key otherkey = (Key) ((WeakReference) otherref).get();
                if (otherkey != null && otherkey.getClass() == k.getClass()) {
                    throw new IllegalArgumentException(identity+
                                                       " already registered");
                }
                // Note that this system can fail in a mostly harmless
                // way.  If we end up generating the same identity
                // String for 2 different classes (a very rare case)
                // then we correctly avoid throwing the exception above,
                // but we are about to drop through to a statement that
                // will replace the entry for the old Key subclass with
                // an entry for the new Key subclass.  At that time the
                // old subclass will be vulnerable to someone generating
                // a duplicate Key instance for it.  We could bail out
                // of the method here and let the old identity keep its
                // record in the map, but we are more likely to see a
                // duplicate key go by for the new class than the old
                // one since the new one is probably still in the
                // initialization stage.  In either case, the probability
                // of loading 2 classes in the same VM with the same name
                // and identityHashCode should be nearly impossible.
            }
            // Note: Use a weak reference to avoid holding on to extra
            // objects and classes after they should be unloaded.
            identitymap.put(identity, new WeakReference(k));
        }

        private int privatekey;

        /**
         * Construct a key using the indicated private key.  Each
         * subclass of Key maintains its own unique domain of integer
         * keys.  No two objects with the same integer key and of the
         * same specific subclass can be constructed.  An exception
         * will be thrown if an attempt is made to construct another
         * object of a given class with the same integer key as a
         * pre-existing instance of that subclass of Key.
         * @param privatekey the specified key
         */
        protected Key(int privatekey) {
            this.privatekey = privatekey;
            recordIdentity(this);
        }

        /**
         * Returns true if the specified object is a valid value
         * for this Key.
         * @param val the <code>Object</code> to test for validity
         * @return <code>true</code> if <code>val</code> is valid;
         *         <code>false</code> otherwise.
         */
        public abstract boolean isCompatibleValue(Object val);

        /**
         * Returns the private integer key that the subclass
         * instantiated this Key with.
         * @return the private integer key that the subclass
         * instantiated this Key with.
         */
        protected final int intKey() {
            return privatekey;
        }

        /**
         * The hash code for all Key objects will be the same as the
         * system identity code of the object as defined by the
         * System.identityHashCode() method.
         */
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * The equals method for all Key objects will return the same
         * result as the equality operator '=='.
         */
        public final boolean equals(Object o) {
            return this == o;
        }
    }

    HashMap hintmap = new HashMap(7);


    /**
     * Constructs a new object with keys and values initialized
     * from the specified Map object which may be null.
     * @param init a map of key/value pairs to initialize the hints
     *          or null if the object should be empty
     */
    public RenderingHints(Map<Key,?> init) {
        if (init != null) {
            hintmap.putAll(init);
        }
    }

    /**
     * Constructs a new object with the specified key/value pair.
     * @param key the key of the particular hint property
     * @param value the value of the hint property specified with
     * <code>key</code>
     */
    public RenderingHints(Key key, Object value) {
        hintmap.put(key, value);
    }

    /**
     * Returns the number of key-value mappings in this
     * <code>RenderingHints</code>.
     *
     * @return the number of key-value mappings in this
     * <code>RenderingHints</code>.
     */
    public int size() {
        return hintmap.size();
    }

    /**
     * Returns <code>true</code> if this
     * <code>RenderingHints</code> contains no key-value mappings.
     *
     * @return <code>true</code> if this
     * <code>RenderingHints</code> contains no key-value mappings.
     */
    public boolean isEmpty() {
        return hintmap.isEmpty();
    }

    /**
     * Returns <code>true</code> if this <code>RenderingHints</code>
     *  contains a mapping for the specified key.
     *
     * @param key key whose presence in this
     * <code>RenderingHints</code> is to be tested.
     * @return <code>true</code> if this <code>RenderingHints</code>
     *          contains a mapping for the specified key.
     * @exception <code>ClassCastException</code> if the key can not
     *            be cast to <code>RenderingHints.Key</code>
     */
    public boolean containsKey(Object key) {
        return hintmap.containsKey((Key) key);
    }

    /**
     * Returns true if this RenderingHints maps one or more keys to the
     * specified value.
     * More formally, returns <code>true</code> if and only
     * if this <code>RenderingHints</code>
     * contains at least one mapping to a value <code>v</code> such that
     * <pre>
     * (value==null ? v==null : value.equals(v))
     * </pre>.
     * This operation will probably require time linear in the
     * <code>RenderingHints</code> size for most implementations
     * of <code>RenderingHints</code>.
     *
     * @param value value whose presence in this
     *          <code>RenderingHints</code> is to be tested.
     * @return <code>true</code> if this <code>RenderingHints</code>
     *           maps one or more keys to the specified value.
     */
    public boolean containsValue(Object value) {
        return hintmap.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped.
     * @param   key   a rendering hint key
     * @return  the value to which the key is mapped in this object or
     *          <code>null</code> if the key is not mapped to any value in
     *          this object.
     * @exception <code>ClassCastException</code> if the key can not
     *            be cast to <code>RenderingHints.Key</code>
     * @see     #put(Object, Object)
     */
    public Object get(Object key) {
        return hintmap.get((Key) key);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this <code>RenderingHints</code> object.
     * Neither the key nor the value can be <code>null</code>.
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     * @param      key     the rendering hint key.
     * @param      value   the rendering hint value.
     * @return     the previous value of the specified key in this object
     *             or <code>null</code> if it did not have one.
     * @exception <code>NullPointerException</code> if the key is
     *            <code>null</code>.
     * @exception <code>ClassCastException</code> if the key can not
     *            be cast to <code>RenderingHints.Key</code>
     * @exception <code>IllegalArgumentException</code> if the
     *            {@link Key#isCompatibleValue(java.lang.Object)
     *                   Key.isCompatibleValue()}
     *            method of the specified key returns false for the
     *            specified value
     * @see     #get(Object)
     */
    public Object put(Object key, Object value) {
        if (!((Key) key).isCompatibleValue(value)) {
            throw new IllegalArgumentException(value+
                                               " incompatible with "+
                                               key);
        }
        return hintmap.put((Key) key, value);
    }

    /**
     * Adds all of the keys and corresponding values from the specified
     * <code>RenderingHints</code> object to this
     * <code>RenderingHints</code> object. Keys that are present in
     * this <code>RenderingHints</code> object, but not in the specified
     * <code>RenderingHints</code> object are not affected.
     * @param hints the set of key/value pairs to be added to this
     * <code>RenderingHints</code> object
     */
    public void add(RenderingHints hints) {
        hintmap.putAll(hints.hintmap);
    }

    /**
     * Clears this <code>RenderingHints</code> object of all key/value
     * pairs.
     */
    public void clear() {
        hintmap.clear();
    }

    /**
     * Removes the key and its corresponding value from this
     * <code>RenderingHints</code> object. This method does nothing if the
     * key is not in this <code>RenderingHints</code> object.
     * @param   key   the rendering hints key that needs to be removed
     * @exception <code>ClassCastException</code> if the key can not
     *            be cast to <code>RenderingHints.Key</code>
     * @return  the value to which the key had previously been mapped in this
     *          <code>RenderingHints</code> object, or <code>null</code>
     *          if the key did not have a mapping.
     */
    public Object remove(Object key) {
        return hintmap.remove((Key) key);
    }

    /**
     * Copies all of the mappings from the specified <code>Map</code>
     * to this <code>RenderingHints</code>.  These mappings replace
     * any mappings that this <code>RenderingHints</code> had for any
     * of the keys currently in the specified <code>Map</code>.
     * @param m the specified <code>Map</code>
     * @exception <code>ClassCastException</code> class of a key or value
     *          in the specified <code>Map</code> prevents it from being
     *          stored in this <code>RenderingHints</code>.
     * @exception <code>IllegalArgumentException</code> some aspect
     *          of a key or value in the specified <code>Map</code>
     *           prevents it from being stored in
     *            this <code>RenderingHints</code>.
     */
    public void putAll(Map<?,?> m) {
        // ## javac bug?
        //if (m instanceof RenderingHints) {
        if (RenderingHints.class.isInstance(m)) {
            //hintmap.putAll(((RenderingHints) m).hintmap);
            for (Map.Entry<?,?> entry : m.entrySet())
                hintmap.put(entry.getKey(), entry.getValue());
        } else {
            // Funnel each key/value pair through our protected put method
            for (Map.Entry<?,?> entry : m.entrySet())
                put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a <code>Set</code> view of the Keys contained in this
     * <code>RenderingHints</code>.  The Set is backed by the
     * <code>RenderingHints</code>, so changes to the
     * <code>RenderingHints</code> are reflected in the <code>Set</code>,
     * and vice-versa.  If the <code>RenderingHints</code> is modified
     * while an iteration over the <code>Set</code> is in progress,
     * the results of the iteration are undefined.  The <code>Set</code>
     * supports element removal, which removes the corresponding
     * mapping from the <code>RenderingHints</code>, via the
     * <code>Iterator.remove</code>, <code>Set.remove</code>,
     * <code>removeAll</code> <code>retainAll</code>, and
     * <code>clear</code> operations.  It does not support
     * the <code>add</code> or <code>addAll</code> operations.
     *
     * @return a <code>Set</code> view of the keys contained
     * in this <code>RenderingHints</code>.
     */
    public Set<Object> keySet() {
        return hintmap.keySet();
    }

    /**
     * Returns a <code>Collection</code> view of the values
     * contained in this <code>RenderinHints</code>.
     * The <code>Collection</code> is backed by the
     * <code>RenderingHints</code>, so changes to
     * the <code>RenderingHints</code> are reflected in
     * the <code>Collection</code>, and vice-versa.
     * If the <code>RenderingHints</code> is modified while
     * an iteration over the <code>Collection</code> is
     * in progress, the results of the iteration are undefined.
     * The <code>Collection</code> supports element removal,
     * which removes the corresponding mapping from the
     * <code>RenderingHints</code>, via the
     * <code>Iterator.remove</code>,
     * <code>Collection.remove</code>, <code>removeAll</code>,
     * <code>retainAll</code> and <code>clear</code> operations.
     * It does not support the <code>add</code> or
     * <code>addAll</code> operations.
     *
     * @return a <code>Collection</code> view of the values
     *          contained in this <code>RenderingHints</code>.
     */
    public Collection<Object> values() {
        return hintmap.values();
    }

    /**
     * Returns a <code>Set</code> view of the mappings contained
     * in this <code>RenderingHints</code>.  Each element in the
     * returned <code>Set</code> is a <code>Map.Entry</code>.
     * The <code>Set</code> is backed by the <code>RenderingHints</code>,
     * so changes to the <code>RenderingHints</code> are reflected
     * in the <code>Set</code>, and vice-versa.  If the
     * <code>RenderingHints</code> is modified while
     * while an iteration over the <code>Set</code> is in progress,
     * the results of the iteration are undefined.
     * <p>
     * The entrySet returned from a <code>RenderingHints</code> object
     * is not modifiable.
     *
     * @return a <code>Set</code> view of the mappings contained in
     * this <code>RenderingHints</code>.
     */
    public Set<Map.Entry<Object,Object>> entrySet() {
        return Collections.unmodifiableMap(hintmap).entrySet();
    }

    /**
     * Compares the specified <code>Object</code> with this
     * <code>RenderingHints</code> for equality.
     * Returns <code>true</code> if the specified object is also a
     * <code>Map</code> and the two <code>Map</code> objects represent
     * the same mappings.  More formally, two <code>Map</code> objects
     * <code>t1</code> and <code>t2</code> represent the same mappings
     * if <code>t1.keySet().equals(t2.keySet())</code> and for every
     * key <code>k</code> in <code>t1.keySet()</code>,
     * <pre>
     * (t1.get(k)==null ? t2.get(k)==null : t1.get(k).equals(t2.get(k)))
     * </pre>.
     * This ensures that the <code>equals</code> method works properly across
     * different implementations of the <code>Map</code> interface.
     *
     * @param o <code>Object</code> to be compared for equality with
     * this <code>RenderingHints</code>.
     * @return <code>true</code> if the specified <code>Object</code>
     * is equal to this <code>RenderingHints</code>.
     */
    public boolean equals(Object o) {
        if (o instanceof RenderingHints) {
            return hintmap.equals(((RenderingHints) o).hintmap);
        } else if (o instanceof Map) {
            return hintmap.equals(o);
        }
        return false;
    }

    /**
     * Returns the hash code value for this <code>RenderingHints</code>.
     * The hash code of a <code>RenderingHints</code> is defined to be
     * the sum of the hashCodes of each <code>Entry</code> in the
     * <code>RenderingHints</code> object's entrySet view.  This ensures that
     * <code>t1.equals(t2)</code> implies that
     * <code>t1.hashCode()==t2.hashCode()</code> for any two <code>Map</code>
     * objects <code>t1</code> and <code>t2</code>, as required by the general
     * contract of <code>Object.hashCode</code>.
     *
     * @return the hash code value for this <code>RenderingHints</code>.
     * @see java.util.Map.Entry#hashCode()
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return hintmap.hashCode();
    }

    /**
     * Creates a clone of this <code>RenderingHints</code> object
     * that has the same contents as this <code>RenderingHints</code>
     * object.
     * @return a clone of this instance.
     */
    public Object clone() {
        RenderingHints rh;
        try {
            rh = (RenderingHints) super.clone();
            if (hintmap != null) {
                rh.hintmap = (HashMap) hintmap.clone();
            }
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }

        return rh;
    }

    /**
     * Returns a rather long string representation of the hashmap
     * which contains the mappings of keys to values for this
     * <code>RenderingHints</code> object.
     * @return  a string representation of this object.
     */
    public String toString() {
        if (hintmap == null) {
            return getClass().getName() + "@" +
                Integer.toHexString(hashCode()) +
                " (0 hints)";
        }

        return hintmap.toString();
    }
}
