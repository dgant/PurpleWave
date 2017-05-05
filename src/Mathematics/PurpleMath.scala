package Mathematics

object PurpleMath {
  
  def mean(values:Traversable[Double]):Double = {
    if (values.isEmpty)
      0.0
    else
      values.sum / values.size
  }
  
  def nanToZero(value:Double):Double = {
    if (value.isNaN) 0.0 else value
  }
  
  def nanToOne(value:Double):Double = {
    if (value.isNaN) 1.0 else value
  }
  
  def clampToOne(value:Double):Double = Math.max(0.0, Math.min(1.0, value))
}
