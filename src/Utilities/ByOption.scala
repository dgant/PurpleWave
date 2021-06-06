package Utilities

import Mathematics.PurpleMath


object ByOption {
  
  @inline final def min[A](sequence: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.min(cmp))
  }
  
  @inline final def max[A](sequence: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.max(cmp))
  }
  
  @inline final def mean(sequence: TraversableOnce[Double]): Option[Double] = {
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

  @inline final def mode[T](sequence: Traversable[T]): Option[T] = if (sequence.isEmpty) None else Some(PurpleMath.mode(sequence))
  
  // Root mean square
  @inline final def rms(sequence: TraversableOnce[Double]): Option[Double] = {
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
  
  @inline final def minBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).min)
  }
  
  @inline final def maxBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).max)
  }
}
