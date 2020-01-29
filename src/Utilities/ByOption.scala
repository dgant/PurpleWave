package Utilities


object ByOption {
  
  def min[A](sequence: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.min)
  }
  
  def max[A](sequence: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.max)
  }
  
  def mean(sequence: TraversableOnce[Double])(implicit cmp: scala.Ordering[Double]): Option[Double] = {
    if (sequence.isEmpty)
      None
    else {
      var numerator = 0.0
      var denominator = 0.0
      sequence.foreach(value => {
        numerator += value
        denominator += 1.0
      })
      Some(numerator / denominator)
    }
  }
  
  // Root mean square
  def rms(sequence: TraversableOnce[Double])(implicit cmp: scala.Ordering[Double]): Option[Double] = {
    if (sequence.isEmpty)
      None
    else {
      var numerator = 0.0
      var denominator = 0.0
      sequence.foreach(value => {
        numerator += value * value
        denominator += 1.0
      })
      val sumOfSquares  = numerator / denominator
      val output        = Math.sqrt(sumOfSquares)
      Some(sumOfSquares)
    }
  }
  
  def minBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).min)
  }
  
  def maxBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).max)
  }
}
