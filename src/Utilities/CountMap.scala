package Utilities

import scala.collection.mutable

class CountMap[T] extends mutable.ListMap[T, Int] {
  override def default(key: T): Int = { put(key, 0); 0 }
  def add         (key: T, value:Int)   : Option[Int] = put(key, this(key) + value)
  def addOne      (key: T)              : Option[Int] = add(key, 1)
  def subtract    (key: T, value: Int)  : Option[Int] = add(key, -value)
  def subtractOne (key: T)              : Option[Int] = subtract(key, 1)
  
  override def clone: CountMap[T] = {
    val output = new CountMap[T]
    keys.foreach(key => output.put(key, this(key)))
    output
  }

  def +(other: CountMap[T]): CountMap[T] = {
    clone += other
  }

  def +=(other: CountMap[T]): CountMap[T] = {
    other.foreach(pair => add(pair._1, pair._2))
    this
  }

  def ++= (others: Iterable[CountMap[T]]): CountMap[T] = {
    others.foreach(+=)
    this
  }

  def mode: Option[T] = {
    var output: Option[T] = None
    var maximumCount: Int = Int.MinValue
    foreach(p => if (p._2 >= maximumCount) {
      output = Some(p._1)
      maximumCount = p._2
    })
    output
  }
}
