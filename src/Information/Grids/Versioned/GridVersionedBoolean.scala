package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedBoolean extends AbstractGridVersionedValue[Boolean] {
  override val defaultValue: Boolean = false
  override protected val values: Array[Boolean] = Array.fill(length)(defaultValue)
}
