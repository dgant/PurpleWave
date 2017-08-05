package Utilities

object ByOption {
  
  def min[A, B: Ordering](sequence: Seq[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).min)
  }
  
  def max[A, B: Ordering](seq: Seq[A])(feature: A => B): Option[A] = {
    seq.reduceOption(Ordering.by(feature).max)
  }
}
