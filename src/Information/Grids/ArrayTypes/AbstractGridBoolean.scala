package Information.Grids.ArrayTypes

class AbstractGridBoolean extends AbstractGridArray[Boolean] {

  override val defaultValue: Boolean = false
  override var values: Array[Boolean] = Array.fill(width * height)(defaultValue)
  override def repr(value: Boolean): String = if (value) "true" else ""
}
