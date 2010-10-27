/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package org.apache.mahout.math.jet.random;

import java.util.Random;

/** @deprecated until unit tests are in place.  Until this time, this class/interface is unsupported. */
@Deprecated
public class Zeta extends AbstractDiscreteDistribution {

  private static final double MAX_LONG_INT = Long.MAX_VALUE - 1.5;
  // The uniform random number generated shared by all <b>static</b> methods.
  //private static final Zeta SHARED = new Zeta(1.0, 1.0, RandomUtils.getRandom());

  private double ro;
  private double pk;

  // cached values (for performance)
  private double c;
  private double d;
  private double roPrev = -1.0;
  private double pkPrev = -1.0;

  /** Constructs a Zeta distribution. */
  public Zeta(double ro, double pk, Random randomGenerator) {
    setRandomGenerator(randomGenerator);
    setState(ro, pk);
  }

  @Override
  public int nextInt() {
    return (int) generateZeta(ro, pk, randomGenerator);
  }

  /** Returns a zeta distributed random number. */
  protected long generateZeta(double ro, double pk, Random randomGenerator) {
/******************************************************************
 *                                                                *
 *            Zeta Distribution - Acceptance Rejection            *
 *                                                                *
 ******************************************************************
 *                                                                *
 * To sample from the Zeta distribution with parameters ro and pk *
 * it suffices to sample variates x from the distribution with    *
 * density function  f(x)=B*{[x+0.5]+pk}^(-(1+ro)) ( x > .5 )     *
 * and then deliver k=[x+0.5].                                    *
 * 1/B=Sum[(j+pk)^-(ro+1)]  (j=1,2,...) converges for ro >= .5 .  *
 * It is not necessary to compute B, because variates x are       *
 * generated by acceptance rejection using density function       *
 * g(x)=ro*(c+0.5)^ro*(c+x)^-(ro+1).                              *
 *                                                                *
 * Integer overflow is possible, when ro is small (ro <= .5) and  *
 * pk large. In this case a new sample is generated. If ro and pk *
 * satisfy the inequality   ro > .14 + pk*1.85e-8 + .02*ln(pk)    *
 * the percentage of overflow is less than 1%, so that the        *
 * result is reliable.                                            *
 * NOTE: The comment above is likely to be nomore valid since     *
 * the C-version operated on 32-bit integers, while this Java     *
 * version operates on 64-bit integers. However, the following is *
 * still valid:                                                   *
 *                                                                *
 * If either ro > 100  or  k > 10000 numerical problems in        *
 * computing the theoretical moments arise, therefore ro<=100 and *
 * k<=10000 are recommended.                                      *
 *                                                                *
 ******************************************************************
 *                                                                *
 * FUNCTION:    - zeta  samples a random number from the          *
 *                Zeta distribution with parameters  ro > 0  and  *
 *                pk >= 0.                                        *
 * REFERENCE:   - J. Dagpunar (1988): Principles of Random        *
 *                Variate  Generation, Clarendon Press, Oxford.   *
 *                                                                *
 ******************************************************************/

    if (ro != roPrev || pk != pkPrev) {                   // Set-up
      roPrev = ro;
      pkPrev = pk;
      if (ro < pk) {
        c = pk - 0.5;
        d = 0;
      } else {
        c = ro - 0.5;
        d = (1.0 + ro) * Math.log((1.0 + pk) / (1.0 + ro));
      }
    }
    long k;
    double x;
    double e;
    do {
      double v;
      do {
        double u = randomGenerator.nextDouble();
        v = randomGenerator.nextDouble();
        x = (c + 0.5) * Math.exp(-Math.log(u) / ro) - c;
      } while (x <= 0.5 || x >= MAX_LONG_INT);

      k = (int) (x + 0.5);
      e = -Math.log(v);
    } while (e < (1.0 + ro) * Math.log((k + pk) / (x + c)) - d);

    return k;
  }

  /** Returns a random number from the distribution.
  @Override
  public int nextInt() {
    return (int) generateZeta(ro, pk, this.randomGenerator);
  }

  /** Sets the parameters. */
  public void setState(double ro, double pk) {
    this.ro = ro;
    this.pk = pk;
  }

  /** Returns a String representation of the receiver. */
  public String toString() {
    return this.getClass().getName() + '(' + ro + ',' + pk + ')';
  }

}
