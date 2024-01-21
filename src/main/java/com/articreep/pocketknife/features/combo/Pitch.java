package com.articreep.pocketknife.features.combo;

public enum Pitch {

    FSHARP(-1), G(-11/12F), GSHARP(-10/12F), A(-9/12F), ASHARP(-8/12F), B(-7/12F),
    C(-6/12F), CSHARP(-5/12F), D(-4/12F), DSHARP(-3/12F), E(-2/12F), F(-1/12F),
    FSHARPHI(0), GHI(1/12F), GSHARPHI(2/12F), AHI(3/12F), ASHARPHI(4/12F), BHI(5/12F),
    CHI(6/12F), CSHARPHI(7/12F), DHI(8/12F), DSHARPHI(9/12F), EHI(10/12F), FHI(11/12F),
    FSHARPHIGHEST(1);

    /** The power to which 2 will be raised to in order to calculate the correct pitch **/
    final float power;
    Pitch(float power) {
        this.power = power;
    }

    /**
     * Returns the numerical pitch that represents this note.
     */
    public float getPitch() {
        return (float) Math.pow(2, power);
    }

}
