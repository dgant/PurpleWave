package Information.Grids.Abstract

import bwapi.TilePosition

import scala.collection.mutable

abstract class GridItems[T] extends GridArray[mutable.HashSet[T]] {
  
  override protected val values: Array[mutable.HashSet[T]] = Array.fill(_width * _height)(defaultValue)
  override def defaultValue: mutable.HashSet[T] = mutable.HashSet.empty
  override def repr(value: mutable.HashSet[T]): String  = value.size.toString
  
  override def update() {
    reset()
    getUnits.foreach(item => getTiles(item).foreach(tile => get(tile).add(item)))
  }
  
  protected def getTiles(item: T): Iterable[TilePosition]
  protected def getUnits: Iterable[T]
}
