package Information.Grids.ArrayTypes

import Mathematics.Pixels.Tile

import scala.collection.mutable

abstract class AbstractGridVector[T] extends AbstractGridArray[mutable.ArrayBuffer[T]] {
  
  private val empty = Array.fill(width * height)(defaultValue)
  
  override protected var values: Array[mutable.ArrayBuffer[T]] = Array.fill(width * height)(defaultValue)
  override def defaultValue: mutable.ArrayBuffer[T] = new mutable.ArrayBuffer
  override def repr(value: mutable.ArrayBuffer[T]): String  = value.size.toString
  
  override def reset() {
    values = empty.clone
  }
  
  override def update() {
    reset()
    getObjects.foreach(item => get(getTile(item)).append(item))
  }
  
  protected def getTile(item: T): Tile
  protected def getObjects: Traversable[T]
}
