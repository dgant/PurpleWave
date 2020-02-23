package Information.Grids.ArrayTypes

import Mathematics.Points.Tile

abstract class AbstractGridVersionedValue[T] extends AbstractGridArray[T] {
  val framestamps: AbstractGridVersioned = new AbstractGridVersioned {
    override protected def updateTimestamps(): Unit = {}
  }

  @inline final override def get(i: Int): T = {
    if (framestamps.isSet(i) && i < length) values(i) else defaultValue
  }

  @inline final override def getUnchecked(i: Int): T = {
    if (framestamps.isSet(i)) values(i) else defaultValue
  }

  @inline final override def set(i: Int, value: T): Unit = {
    framestamps.stamp(i)
    super.set(i, value)
  }
  @inline final override def setUnchecked(i: Int, value: T): Unit = {
    framestamps.stamp(i)
    values(i) = value
  }

  @inline final protected def isSet(i: Int): Boolean = framestamps.isSet(i)
  @inline final protected def isSet(tile: Tile): Boolean = framestamps.isSet(tile)

  override def update(): Unit = {
    reset()
    onUpdate()
  }

  override def reset(): Unit = {
    framestamps.update()
  }

  protected def onUpdate(): Unit = {}
}
