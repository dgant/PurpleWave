package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

abstract class AbstractGridVersionedValue[T] extends AbstractGridArray[T] {
  private val framestamps = new AbstractGridVersioned {
    override protected def updateTimestamps(): Unit = {}
  }

  override def get(i: Int): T = {
    if (framestamps.isSet(i)) super.get(i) else defaultValue
  }

  override def set(i: Int, value: T): Unit = {
    framestamps.stamp(i)
    super.set(i, value)
  }
  protected def isSet(i: Int): Boolean = framestamps.isSet(i)
  protected def isSet(tile: Tile): Boolean = framestamps.isSet(tile)

  override def update(): Unit = {
    reset()
    onUpdate()
  }

  override def reset(): Unit = {
    framestamps.update()
  }

  protected def onUpdate(): Unit = {}
}
