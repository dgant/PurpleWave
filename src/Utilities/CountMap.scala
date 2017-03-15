package Utilities

import scala.collection.mutable

//There must be a more idiomatic Scala way of doing this
object CountMapper {
  def make[T](map:Map[T, Int]):CountMap[T] = {
    val output = new CountMap[T]
    map.keys.foreach(key => output.put(key, map(key)))
    output
  }
}

class CountMap[T] extends mutable.HashMap[T, Int] {
  override def default(key: T): Int = { put(key, 0); 0 }
  def add         (key:T, value:Int) = put(key, this(key) + value)
  def addOne      (key:T)            = add(key, 1)
  def subtract    (key:T, value:Int) = add(key, -value)
  def subtractOne (key:T)            = subtract(key, 1)
  
  override def clone:CountMap[T] = {
    val output = new CountMap[T]
    keys.foreach(key => output.put(key, this(key)))
    output
  }
}
