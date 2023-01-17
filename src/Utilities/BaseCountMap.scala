package Utilities

import scala.collection.mutable

abstract class BaseCountMap[T] extends mutable.Map[T, Int] {
  //final def +   (other: mutable.Map[T, Int])    : C = clone += other
  //final def +=  (other: mutable.Map[T, Int])    : C = { other.foreach(pair => add(pair._1, pair._2)); asInstanceOf[C] }
  //final def ++= (others: Iterable[CountMap[T]]) : C = { others.foreach(+=); asInstanceOf[C] }

  final def add         (key: T, value: Int)  : Option[Int] = put(key, this(key) + value)
  final def addOne      (key: T)              : Option[Int] = add(key, 1)
  final def subtract    (key: T, value: Int)  : Option[Int] = add(key, -value)
  final def subtractOne (key: T)              : Option[Int] = subtract(key, 1)
  final def reduceTo    (key: T, value: Int)  : Unit        = put(key, Math.min(this(key), value))
  final def increaseTo  (key: T, value: Int)  : Unit        = put(key, Math.max(this(key), value))

  final def mode: Option[T] = {
    var output: Option[T] = None
    var maximumCount: Int = Int.MinValue
    foreach(p => if (p._2 >= maximumCount) {
      output = Some(p._1)
      maximumCount = p._2
    })
    output
  }
}

