package Geometry.Grids.Real

import Geometry.Grids.Abstract.Grid
import bwapi.TilePosition

import scala.collection.mutable

abstract class GridItems[T] extends Grid[mutable.HashSet[T]] {
  
  override val _positions: Array[mutable.HashSet[T]]    = Array.fill(_width * _height)(_defaultValue)
  override def _defaultValue: mutable.HashSet[T]        = mutable.HashSet.empty
  override def repr(value: mutable.HashSet[T]): String  = value.size.toString
  
  override def update() {
    reset()
    _getUnits.foreach(item => _getTiles(item).foreach(tile => get(tile).add(item)))
  }
  
  def _getTiles(item: T): Iterable[TilePosition]
  def _getUnits: Iterable[T]
}
