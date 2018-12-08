package Utilities

import scala.collection.mutable

class CountMap[T] extends mutable.HashMap[T, Int] {
  override def default(key: T): Int = { put(key, 0); 0 }
  def add         (key: T, value:Int)   = put(key, this(key) + value)
  def addOne      (key: T)              = add(key, 1)
  def subtract    (key: T, value: Int)  = add(key, -value)
  def subtractOne (key: T)              = subtract(key, 1)
  
  override def clone: CountMap[T] = {
    val output = new CountMap[T]
    keys.foreach(key => output.put(key, this(key)))
    output
  }
}
