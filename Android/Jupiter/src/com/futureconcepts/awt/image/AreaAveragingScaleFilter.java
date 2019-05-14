/*
 * Copyright 1996-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.futureconcepts.awt.image;

import com.futureconcepts.awt.image.ImageConsumer;
import java.util.Hashtable;
import com.futureconcepts.awt.Rectangle;

/**
 * An ImageFilter class for scaling images using a simple area averaging
 * algorithm that produces smoother results than the nearest neighbor
 * algorithm.
 * <p>This class extends the basic ImageFilter Class to scale an existing
 * image and provide a source for a new image containing the resampled
 * image.  The pixels in the source image are blended to produce pixels
 * for an image of the specified size.  The blending process is analogous
 * to scaling up the source image to a multiple of the destination size
 * using pixel replication and then scaling it back down to the destination
 * size by simply averaging all the pixels in the supersized image that
 * fall within a given pixel of the destination image.  If the data from
 * the source is not delivered in TopDownLeftRight order then the filter
 * will back off to a simple pixel replication behavior and utilize the
 * requestTopDownLeftRightResend() method to refilter the pixels in a
 * better way at the end.
 * <p>It is meant to be used in conjunction with a FilteredImageSource
 * object to produce scaled versions of existing images.  Due to
 * implementation dependencies, there may be differences in pixel values
 * of an image filtered on different platforms.
 *
 * @see FilteredImageSource
 * @see ReplicateScaleFilter
 * @see ImageFilter
 *
 * @author      Jim Graham
 */
public class AreaAveragingScaleFilter extends ReplicateScaleFilter {
    private static final int neededHints = (TOPDOWNLEFTRIGHT
                                            | COMPLETESCANLINES);

    private boolean passthrough;
    private float reds[], greens[], blues[], alphas[];
    private int savedy;
    private int savedyrem;

    /**
     * Constructs an AreaAveragingScaleFilter that scales the pixels from
     * its source Image as specified by the width and height parameters.
     * @param width the target width to scale the image
     * @param height the target height to scale the image
     */
    public AreaAveragingScaleFilter(int width, int height) {
        super(width, height);
    }

    /**
     * Detect if the data is being delivered with the necessary hints
     * to allow the averaging algorithm to do its work.
     * <p>
     * Note: This method is intended to be called by the
     * <code>ImageProducer</code> of the <code>Image</code> whose
     * pixels are being filtered.  Developers using
     * this class to filter pixels from an image should avoid calling
     * this method directly since that operation could interfere
     * with the filtering operation.
     * @see ImageConsumer#setHints
     */
    public void setHints(int hints) {
        passthrough = ((hints & neededHints) != neededHints);
        super.setHints(hints);
    }

    private void makeAccumBuffers() {
        reds = new float[destWidth];
        greens = new float[destWidth];
        blues = new float[destWidth];
        alphas = new float[destWidth];
    }

    private int[] calcRow() {
        float origmult = ((float) srcWidth) * srcHeight;
        if (outpixbuf == null || !(outpixbuf instanceof int[])) {
            outpixbuf = new int[destWidth];
        }
        int[] outpix = (int[]) outpixbuf;
        for (int x = 0; x < destWidth; x++) {
            float mult = origmult;
            int a = Math.round(alphas[x] / mult);
            if (a <= 0) {
                a = 0;
            } else if (a >= 255) {
                a = 255;
            } else {
                // un-premultiply the components (by modifying mult here, we
                // are effectively doing the divide by mult and divide by
                // alpha in the same step)
                mult = alphas[x] / 255;
            }
            int r = Math.round(reds[x] / mult);
            int g = Math.round(greens[x] / mult);
            int b = Math.round(blues[x] / mult);
            if (r < 0) {r = 0;} else if (r > 255) {r = 255;}
            if (g < 0) {g = 0;} else if (g > 255) {g = 255;}
            if (b < 0) {b = 0;} else if (b > 255) {b = 255;}
            outpix[x] = (a << 24 | r << 16 | g << 8 | b);
        }
        return outpix;
    }
}
