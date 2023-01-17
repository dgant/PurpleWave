package Utilities

import scala.collection.mutable

/**
  * A CountMap that is inexpensive to clone, but slower to read, and depends on
  */
final class CloneyCountMap[T](parent: mutable.Map[T, Int] = null) extends BaseCountMap[T] {

  // OpenHashMap looks to be the best implementation for our use cases, based on:
  // https://github.com/dvmlls/scala-map-benchmarking/blob/master/src/R/plot.png
  private val _map = new mutable.OpenHashMap[T, Int]()
  private val _parent = Option(parent)

  override def default  (key: T)        : Int                       = 0
  override def +=       (kv: (T, Int))  : CloneyCountMap.this.type  = { _map += kv; this }
  override def -=       (key: T)        : CloneyCountMap.this.type  = { _map -= key; this }
  override def get      (key: T)        : Option[Int]               = Some(_map.getOrElseUpdate(key, _parent.map(_(key)).getOrElse(0)))
  override def iterator                 : Iterator[(T, Int)]        = _map.iterator ++ _parent.map(_.iterator.filterNot(i => _map.contains(i._1))).getOrElse(Iterator.empty)

  override def clone: CloneyCountMap[T] = new CloneyCountMap[T](this)
}
