package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedBoolean extends AbstractGridVersionedValue[Boolean] {
  override protected var values: Array[Boolean] = Array.fill(length)(defaultValue)
  override val defaultValue: Boolean = false
}
