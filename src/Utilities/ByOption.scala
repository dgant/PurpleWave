package Utilities


object ByOption {
  
  def min[A](sequence: TraversableOnce[A])(implicit cmp : scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.min)
  }
  
  def max[A](sequence: TraversableOnce[A])(implicit cmp : scala.Ordering[A]): Option[A] = {
    if (sequence.isEmpty) None else Some(sequence.max)
  }
  
  def minBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).min)
  }
  
  def maxBy[A, B: Ordering](sequence: TraversableOnce[A])(feature: A => B): Option[A] = {
    sequence.reduceOption(Ordering.by(feature).max)
  }
}
