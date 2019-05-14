/*
 * Copyright 1995-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.futureconcepts.awt.image.ImageProducer;
import com.futureconcepts.awt.image.ImageObserver;
import com.futureconcepts.awt.image.ImageFilter;
import com.futureconcepts.awt.image.FilteredImageSource;
import com.futureconcepts.awt.image.AreaAveragingScaleFilter;
import com.futureconcepts.awt.image.ReplicateScaleFilter;

/**
 * The abstract class <code>Image</code> is the superclass of all
 * classes that represent graphical images. The image must be
 * obtained in a platform-specific manner.
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public abstract class Image {

    /**
     * convenience object; we can use this single static object for
     * all images that do not create their own image caps; it holds the
     * default (unaccelerated) properties.
     */

    /**
     * Priority for accelerating this image.  Subclasses are free to
     * set different default priorities and applications are free to
     * set the priority for specific images via the
     * <code>setAccelerationPriority(float)</code> method.
     * @since 1.5
     */
    protected float accelerationPriority = .5f;

    /**
     * Determines the width of the image. If the width is not yet known,
     * this method returns <code>-1</code> and the specified
     * <code>ImageObserver</code> object is notified later.
     * @param     observer   an object waiting for the image to be loaded.
     * @return    the width of this image, or <code>-1</code>
     *                   if the width is not yet known.
     * @see       java.awt.Image#getHeight
     * @see       java.awt.image.ImageObserver
     */
    public abstract int getWidth(ImageObserver observer);

    /**
     * Determines the height of the image. If the height is not yet known,
     * this method returns <code>-1</code> and the specified
     * <code>ImageObserver</code> object is notified later.
     * @param     observer   an object waiting for the image to be loaded.
     * @return    the height of this image, or <code>-1</code>
     *                   if the height is not yet known.
     * @see       java.awt.Image#getWidth
     * @see       java.awt.image.ImageObserver
     */
    public abstract int getHeight(ImageObserver observer);

    /**
     * Gets the object that produces the pixels for the image.
     * This method is called by the image filtering classes and by
     * methods that perform image conversion and scaling.
     * @return     the image producer that produces the pixels
     *                                  for this image.
     * @see        java.awt.image.ImageProducer
     */
    public abstract ImageProducer getSource();

    /**
     * Creates a graphics context for drawing to an off-screen image.
     * This method can only be called for off-screen images.
     * @return  a graphics context to draw to the off-screen image.
     * @exception UnsupportedOperationException if called for a
     *            non-off-screen image.
     * @see     java.awt.Graphics
     * @see     java.awt.Component#createImage(int, int)
     */
    public abstract Graphics getGraphics();

    /**
     * Gets a property of this image by name.
     * <p>
     * Individual property names are defined by the various image
     * formats. If a property is not defined for a particular image, this
     * method returns the <code>UndefinedProperty</code> object.
     * <p>
     * If the properties for this image are not yet known, this method
     * returns <code>null</code>, and the <code>ImageObserver</code>
     * object is notified later.
     * <p>
     * The property name <code>"comment"</code> should be used to store
     * an optional comment which can be presented to the application as a
     * description of the image, its source, or its author.
     * @param       name   a property name.
     * @param       observer   an object waiting for this image to be loaded.
     * @return      the value of the named property.
     * @throws      <code>NullPointerException</code> if the property name is null.
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.Image#UndefinedProperty
     */
    public abstract Object getProperty(String name, ImageObserver observer);

    /**
     * The <code>UndefinedProperty</code> object should be returned whenever a
     * property which was not defined for a particular image is fetched.
     */
    public static final Object UndefinedProperty = new Object();


    /**
     * Use the default image-scaling algorithm.
     * @since JDK1.1
     */
    public static final int SCALE_DEFAULT = 1;

    /**
     * Choose an image-scaling algorithm that gives higher priority
     * to scaling speed than smoothness of the scaled image.
     * @since JDK1.1
     */
    public static final int SCALE_FAST = 2;

    /**
     * Choose an image-scaling algorithm that gives higher priority
     * to image smoothness than scaling speed.
     * @since JDK1.1
     */
    public static final int SCALE_SMOOTH = 4;

    /**
     * Use the image scaling algorithm embodied in the
     * <code>ReplicateScaleFilter</code> class.
     * The <code>Image</code> object is free to substitute a different filter
     * that performs the same algorithm yet integrates more efficiently
     * into the imaging infrastructure supplied by the toolkit.
     * @see        java.awt.image.ReplicateScaleFilter
     * @since      JDK1.1
     */
    public static final int SCALE_REPLICATE = 8;

    /**
     * Use the Area Averaging image scaling algorithm.  The
     * image object is free to substitute a different filter that
     * performs the same algorithm yet integrates more efficiently
     * into the image infrastructure supplied by the toolkit.
     * @see java.awt.image.AreaAveragingScaleFilter
     * @since JDK1.1
     */
    public static final int SCALE_AREA_AVERAGING = 16;



    /**
     * Returns the current value of the acceleration priority hint.
     * @see #setAccelerationPriority(float priority) setAccelerationPriority
     * @return value between 0 and 1, inclusive, which represents the current
     * priority value
     * @since 1.5
     */
    public float getAccelerationPriority() {
        return accelerationPriority;
    }

    static {
    }
}
