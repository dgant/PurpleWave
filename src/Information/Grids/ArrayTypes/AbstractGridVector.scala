package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

import scala.collection.mutable

abstract class AbstractGridVector[T] extends AbstractGridArray[mutable.ArrayBuffer[T]] {
  
  override protected var values = Array.fill(length) { defaultValue }
  override def defaultValue: mutable.ArrayBuffer[T] = new mutable.ArrayBuffer
  override def repr(value: mutable.ArrayBuffer[T]): String  = value.size.toString
  
  private val populatedTiles = new mutable.HashSet[Tile]
  
  override def reset() {
    populatedTiles.foreach(tile => get(tile).clear())
    populatedTiles.clear()
  }
  
  override def update() {
    reset()
    getObjects.foreach(item => {
      val tile = getTile(item)
      populatedTiles.add(tile)
      get(tile).append(item)
    })
  }
  
  protected def getTile(item: T): Tile
  protected def getObjects: Traversable[T]
}
