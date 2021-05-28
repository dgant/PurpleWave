package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class GridItems[T] extends AbstractGridArray[mutable.ArrayBuffer[T]] {

  final override val defaultValue: mutable.ArrayBuffer[T] = new mutable.ArrayBuffer
  override protected val values: Array[ArrayBuffer[T]] = Array.fill(length)(defaultValue)
  override def repr(value: mutable.ArrayBuffer[T]): String  = value.size.toString
  
  private val populatedTiles = new mutable.ArrayBuffer[Int]()
  
  override def reset() {
    populatedTiles.foreach(values(_).clear())
    populatedTiles.clear()
  }

  @inline final def addItem(item: T, tile: Tile): Unit = {
    if (tile.valid) {
      addItemUnchecked(item, tile.i)
    }
  }

  @inline final def addItem(item: T, tileI: Int): Unit = {
    if (tileI >= 0 && tileI < values.length) {
      addItemUnchecked(item, tileI)
    }
  }

  @inline final def addItemUnchecked(item: T, tileI: Int): Unit = {
      populatedTiles.append(tileI)
      getUnchecked(tileI).append(item)
  }
  
  override def update() {
    reset()
    getDefaultItems.foreach(item => addItem(item, getItemTile(item)))
  }

  protected def getItemTile(item: T): Tile = ???
  protected def getDefaultItems: Traversable[T] = Traversable.empty
}
