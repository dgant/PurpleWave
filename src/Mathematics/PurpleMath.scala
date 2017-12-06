package Mathematics

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
  
  def clampToOne(value: Double): Double = Math.max(0.0, Math.min(1.0, value))
  
  def signum(int: Int): Int = if (int == 0) 0 else if (int < 0) -1 else 1
  
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
}
