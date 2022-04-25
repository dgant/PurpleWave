package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With

class GridBuildableTownHall extends AbstractGridArrayBoolean {
  
  final override val defaultValue: Boolean = true
  
  override def onInitialization(): Unit = {
    reset()
    With.units.all.filter(_.unitClass.isResource).foreach(_.tileArea.expand(3, 3).tiles.foreach(set(_, false)))
  }
}

