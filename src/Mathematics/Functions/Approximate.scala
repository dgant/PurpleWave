package Mathematics.Functions

import Mathematics.Maff
import Mathematics.Maff.halfPi
import Mathematics.Points.AbstractPoint

trait Approximate {
  @inline final def broodWarDistance(x0: Int, y0: Int, x1: Int, y1: Int): Double = {
    val dx  = Math.abs(x0 - x1)
    val dy  = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d * 4 < D) D else D - Maff.div16(D) + Maff.div8(d * 3) - Maff.div64(D) + Maff.div256(d * 3)
  }

  private val x3d8 = 3.0 / 8.0
  private val x3d256 = 3.0 / 256.0
  @inline final def broodWarDistanceDouble(x0: Double, y0: Double, x1: Double, y1: Double): Double = {
    val dx  = Math.abs(x0 - x1)
    val dy  = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d * 4 < D) D else  D - D * Maff.inv16 + d * x3d8 - D * Maff.inv64 + d * x3d256
  }
  @inline final def broodWarDistanceBox(
    p00: AbstractPoint,
    p01: AbstractPoint,
    p10: AbstractPoint,
    p11: AbstractPoint)
  : Double = broodWarDistanceBox(
    p00.x, p00.y,
    p01.x, p01.y,
    p10.x, p10.y,
    p11.x, p11.y)
  @inline final def broodWarDistanceBox(
    x00: Int, y00: Int,
    x01: Int, y01: Int,
    x10: Int, y10: Int,
    x11: Int, y11: Int)
  : Double = {
    if (x11 < x00) {
      if      (y11 < y00) Maff.broodWarDistance(x11, y11, x00, y00)
      else if (y10 > y01) Maff.broodWarDistance(x11, y10, x00, y01)
      else                x00 - x11
    } else if (x10 > x01) {
      if      (y11 < y00) Maff.broodWarDistance(x10, y11, x01, y00)
      else if (y10 > y01) Maff.broodWarDistance(x10, y10, x01, y01)
      else                x10 - x01
    } else if (y11 < y00) y00 - y11
      else if (y10 > y01) y10 - y01
      else                0
  }

  @inline final def fastSigmoid01(x: Double): Double = {
    0.5 + fastTanh11(x) / 2.0
  }

  @inline final def fastTanh11(x: Double): Double = {
    if (x.isPosInfinity) return 1.0
    if (x.isNegInfinity) return -1.0
    x / (1.0 + Math.abs(x))
  }

  // Via https://www.dsprelated.com/showarticle/1052.php
  @inline final def fastAtan(r: Double): Double = (0.97239411 - 0.19194795 * r * r) * r

  // Via https://www.dsprelated.com/showarticle/1052.php
  @inline final def fastAtan2(y: Double, x: Double): Double = {
    if (x == 0) {
      if (y > 0) halfPi else - halfPi
    } else if (x * x > y * y) {
      val a = fastAtan(y / x)
      if (x > 0) a else if (y > 0) a + Math.PI else a - Math.PI
    } else {
      val a = fastAtan(x / y)
      if (y > 0) halfPi - a else - halfPi - a
    }
  }
}
