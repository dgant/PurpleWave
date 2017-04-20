package Mathematics

object PurpleMath {
  
  def mean(values:Traversable[Double]):Double = {
    if (values.isEmpty)
      0.0
    else
      values.sum / values.size
  }
}
