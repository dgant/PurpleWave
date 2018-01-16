package Mathematics

import Mathematics.Points.Point

object PurpleMath {
  
  val twoPI: Double = 2 * Math.PI
  
  def mean(values: Traversable[Double]): Double = {
    if (values.isEmpty)
      0.0
    else
      values.sum / values.size
  }
  
  def nanToZero(value: Double): Double = {
    if (value.isNaN || value.isInfinity) 0.0 else value
  }
  
  def nanToOne(value: Double): Double = {
    if (value.isNaN || value.isInfinity) 1.0 else value
  }
  
  def nanToInfinity(value: Double): Double = {
    if (value.isNaN || value.isInfinity) Double.PositiveInfinity else value
  }
  
  def clampRatio(value: Double, ratio: Double): Double = clamp(value, ratio, 1.0 / ratio)
  
  def clamp(value: Int, bound0: Int, bound1: Int): Int = {
    val min = Math.min(bound0, bound1)
    val max = Math.max(bound0, bound1)
    Math.min(max, Math.max(min, value))
  }
  
  def clamp(value: Double, value1: Double, value2: Double): Double = {
    val min = Math.min(value1, value2)
    val max = Math.max(value1, value2)
    Math.min(max, Math.max(value, min))
  }
  
  def clampToOne(value: Double): Double = clamp(value, 0.0, 1.0)
  
  def signum(int: Int)        : Int = if (int == 0) 0 else if (int < 0) -1 else 1
  def signum(double: Double)  : Int = if (double == 0.0) 0 else if (double < 0) -1 else 1
  def forcedSignum(int: Int)  : Int = if (int < 0) -1 else 1
  
  val twoPi: Double = Math.PI * 2
  def normalizeAngle(angleRadians: Double): Double = {
    if      (angleRadians < 0) normalizeAngle(angleRadians + twoPi)
    else if (angleRadians > twoPi) normalizeAngle(angleRadians - twoPi)
    else    angleRadians
  }
  
  def geometricMean(values: Iterable[Double]): Double = {
    if (values.isEmpty) return 1.0
    Math.pow(values.product, 1.0 / values.size)
  }
  
  def fromBoolean(value: Boolean): Int = if (value) 1 else 0
  def toBoolean(value: Int): Boolean = value != 0
  
  def broodWarDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double = {
    val dx = Math.abs(x1 - x2)
    val dy = Math.abs(y1 - y2)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (D / 4 > d) {
      return D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256
    }
    D
  }
  
  def broodWarDistance(a: Point, b: Point): Double = broodWarDistance(a.x, a.y, b.x, b.y)
}
