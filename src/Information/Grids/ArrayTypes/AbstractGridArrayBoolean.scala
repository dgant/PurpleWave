package Information.Grids.ArrayTypes

abstract class AbstractGridArrayBoolean extends AbstractGridArray[Boolean] {
  override val defaultValue: Boolean = false
  override val values: Array[Boolean] = Array.fill(width * height)(defaultValue)
  override def repr(value: Boolean): String = if (value) "true" else "false"
}
