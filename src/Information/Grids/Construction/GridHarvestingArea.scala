package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With

class GridHarvestingArea extends AbstractGridBoolean {
  override def onInitialization() {
    With.geography.bases.foreach(_.harvestingArea.tiles.foreach(set(_, true)))
  }
}
