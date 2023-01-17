package Utilities

import scala.collection.mutable

final class CountMap[T](argDefault: Int = 0) extends BaseCountMap[T] {

  // OpenHashMap looks to be the best implementation for our use cases, based on:
  // https://github.com/dvmlls/scala-map-benchmarking/blob/master/src/R/plot.png
  private val _map = new mutable.OpenHashMap[T, Int]()

  override def default  (key: T)        : Int                 = argDefault
  override def +=       (kv: (T, Int))  : CountMap.this.type  = { _map += kv; this }
  override def -=       (key: T)        : CountMap.this.type  = { _map -= key; this }
  override def get      (key: T)        : Option[Int]         = Some(_map.getOrElseUpdate(key, argDefault))
  override def iterator                 : Iterator[(T, Int)]  = _map.iterator

  override def clone: CountMap[T] = {
    val output = new CountMap[T]
    keys.foreach(key => output.put(key, this(key)))
    output
  }
}
