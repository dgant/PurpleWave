package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With

class GridBuildableTownHall extends AbstractGridBoolean {
  
  final override val defaultValue: Boolean = true
  
  override def onInitialization() {
    reset()
    With.units.neutral.foreach(unit =>
      if (unit.unitClass.isResource) {
        unit.tileArea.expand(3, 3).tiles.foreach(set(_, false))
      })
  }
}

