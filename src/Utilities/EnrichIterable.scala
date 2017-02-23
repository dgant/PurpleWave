package Utilities

import scala.collection.mutable

case object EnrichIterable {
  implicit class EnrichedIterable[T](iterable: Iterable[T]) {
    
    def minOption[B](implicit cmp : scala.Ordering[B]):Option[T] = {
      if (iterable.isEmpty) { None }
      else { Some(iterable.min) }
    }
    
    def minByOption[B](featureExtrator: (T) => B)(implicit cmp : scala.Ordering[B]):Option[T] = {
      if (iterable.isEmpty) { None }
      else { Some(iterable.minBy(featureExtrator)(cmp)) }
    }
    
  }
  
  implicit class EnrichedSet[T](set: Set[T]) {
    def lacks(element:T):Boolean = { ! set.contains(element) }
  }
  
  implicit class EnrichedMutableSet[T](set: mutable.Set[T]) {
    def lacks(element:T):Boolean = { ! set.contains(element) }
  }
  
  implicit class EnrichedMap[T1, T2](map: Map[T1, T2]) {
    def lacks(element:T1):Boolean = { ! map.contains(element) }
  }
  
  implicit class EnrichedMutableMap[T1, T2](map: mutable.Map[T1, T2]) {
    def lacks(element:T1):Boolean = { ! map.contains(element) }
  }
}