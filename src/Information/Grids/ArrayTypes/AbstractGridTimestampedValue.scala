package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

abstract class AbstractGridTimestampedValue[T] extends AbstractGridArray[T] {
  private val timestamps = new AbstractGridTimestamp {
    override protected def updateTimestamps(): Unit = {}
  }

  override def get(i: Int): T = {
    if (timestamps.isSet(i)) super.get(i) else defaultValue
  }

  override def set(i: Int, value: T): Unit = {
    timestamps.stamp(i)
    super.set(i, value)
  }

  override def update(): Unit = {
    timestamps.update()
    onUpdate()
  }

  protected def onUpdate(): Unit

  def isSet(i: Int): Boolean = timestamps.isSet(i)
  def isSet(tile: Tile): Boolean = timestamps.isSet(tile)
}
