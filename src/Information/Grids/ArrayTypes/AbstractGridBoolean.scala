package Information.Grids.ArrayTypes

class AbstractGridBoolean extends AbstractGridArray[Boolean] {
  
  override var values: Array[Boolean] = Array.fill(width * height)(defaultValue)
  override def defaultValue: Boolean = false
  override def repr(value: Boolean): String = value.toString
}
