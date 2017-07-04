package Planning.Plans.Information.Scenarios

import Lifecycle.With
import Planning.Plan

class WeAreBeingProxiedInBase extends Plan {
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit =>
      unit.unitClass.isBuilding && {
        val zone = unit.pixelCenter.zone
        zone.owner.isUs &&
        (
          zone.bases.exists(_.isStartLocation) ||
          zone.bases.exists(_.isNaturalOf.exists(main => main.owner.isUs))
        )
      })
  }
}
