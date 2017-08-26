package Planning.Plans.Information.Scenarios

import Lifecycle.With
import Planning.Plan

class WeAreBeingProxiedInBase extends Plan {
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit =>
      unit.unitClass.isBuilding && {
        unit.zone.bases.exists(_.isStartLocation) ||
        unit.zone.bases.exists(_.isNaturalOf.exists(main => main.owner.isUs))
      })
  }
}
