/*
 ** 2012 March 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package net.dragonmounts.neo.common.util;

import net.minecraft.util.Mth;

import java.util.Arrays;

import static net.dragonmounts.neo.common.util.math.Interpolation.clampedSmoothLinear;

/**
 * Very simple fixed size circular buffer implementation for animation purposes.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class CircularBuffer {
    private final float[] buffer;
    private int index = 0;

    public CircularBuffer(int size) {
        // & with a mask only works if size is a power of 2
        if ((size & (size - 1)) != 0) throw new IllegalArgumentException();
        this.buffer = new float[size];
    }

    public void fill(float value) {
        Arrays.fill(this.buffer, value);
    }

    public void update(float value) {
        // move forward
        this.index++;
        // restart pointer at the end to form a virtual ring
        this.index &= this.buffer.length - 1;
        this.buffer[this.index] = value;
    }

    public float get(float x, int offset) {
        int i = this.index - offset;
        int mask = this.buffer.length - 1;
        return clampedSmoothLinear(this.buffer[i - 1 & mask], this.buffer[i & mask], x);
    }

    public float getClamped(float x, int offset1, int offset2, float range) {
        return Mth.clamp(this.get(x, offset2) - this.get(x, offset1), -range, range);
    }
}