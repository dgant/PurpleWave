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
  
  def clampToOne(value: Double): Double = Math.max(0.0, Math.min(1.0, value))
  
  def signum(int: Int): Int = if (int == 0) 0 else if (int < 0) -1 else 1
}
