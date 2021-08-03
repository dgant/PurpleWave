package NeoGeo

object NeoMath {

  /**
    * Brood War's fast distance approximation
    */
  @inline final def distanceBW(x0: Int, y0: Int, x1: Int, y1: Int): Double = {
    val dx = Math.abs(x0 - x1)
    val dy = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d < D / 4) D else D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256
  }
  @inline final def lengthBW(x0: Int, y0: Int): Double = distanceBW(x0, y0, 0, 0)
  @inline final def lengthBW(p: (Int, Int)): Double = distanceBW(p._1, p._2, 0, 0)
}
