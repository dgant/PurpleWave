package Mathematics

import Mathematics.Points.AbstractPoint

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
  
  def broodWarDistance(a: AbstractPoint, b: AbstractPoint): Double = broodWarDistance(a.x, a.y, b.x, b.y)
  def broodWarDistance(x0: Int, y0: Int, x1: Int, y1: Int): Double = {
    val dx = Math.abs(x0 - x1)
    val dy = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d < D / 4) {
      return D
    }
    D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256
  }
  def broodWarDistanceBox(
    p00: AbstractPoint,
    p01: AbstractPoint,
    p10: AbstractPoint,
    p11: AbstractPoint)
    : Double = broodWarDistanceBox(
      p00.x, p00.y,
      p01.x, p01.y,
      p10.x, p10.y,
      p11.x, p11.y)
  def broodWarDistanceBox(
    x00: Int, y00: Int,
    x01: Int, y01: Int,
    x10: Int, y10: Int,
    x11: Int, y11: Int)
    : Double = {
    if (x11 < x00) {
      if (y11 < y00) {
        return PurpleMath.broodWarDistance(x11, y11, x00, y00)
      } else if (y10 > y01) {
        return PurpleMath.broodWarDistance(x11, y10, x00, y01)
      } else {
        return x00 - x11
      }
    } else if (x10 > x01) {
      if (y11 < y00) {
        return PurpleMath.broodWarDistance(x10, y11, x01, y00)
      } else if (y10 > y01) {
        return PurpleMath.broodWarDistance(x10, y10, x01, y01)
      } else {
        return x10 - x01
      }
    } else if (y11 < y00) {
      return y00 - y11
    } else if (y10 > y01) {
      return y10 - y01
    }
    0
  }
  
  def distanceFromLineSegment(
    point: AbstractPoint,
    segmentStart: AbstractPoint,
    segmentEnd: AbstractPoint)
      : Double = {
    val x = point.x
    val y = point.y
    val x0 = segmentStart.x
    val y0 = segmentStart.y
    val x1 = segmentEnd.x
    val y1 = segmentEnd.y
    
    val dx0 = x - x0
    val dy0 = y - y0
    val dx1 = x1 - x0
    val dy1 = y1 - y0
  
    val dotProduct = dx0 * dx1 + dy0 * dy1
    val lengthSquared = dx1 * dx1 + dy1 * dy1
    var param = -1.0
    if (lengthSquared != 0) {
      param = dotProduct / lengthSquared
    }
  
    var xx = 0.0
    var yy = 0.0
  
    if (param < 0) {
      xx = x0
      yy = y0
    }
    else if (param > 1) {
      xx = x1
      yy = y1
    }
    else {
      xx = x0 + param * dx1
      yy = y0 + param * dy1
    }
  
    var dx = x - xx
    var dy = y - yy
    
    Math.sqrt(dx * dx + dy * dy)
  }
  
  def fastSigmoid(x: Double): Double = {
    x / (1.0 + Math.abs(x))
  }
  
  private val piOver4 = Math.PI / 4.0
  private val pi3Over4 = 3 * Math.PI / 4.0
  def fastAtan2(y: Double, x: Double): Double = {
    var r = 0.0
    var angle = 0.0
    val absY = Math.abs(y) + 0.000000001
    if (x < 0.0) {
      r = (x + absY) / (absY - x)
      angle = pi3Over4
    } else {
      r = (x - absY) / (absY + x)
      angle = piOver4
    }
    angle += r * (0.1963 * r * r - 0.9817 * r)
    if (y < 0.0) -angle else angle
  }
}
