package Planning.Composition.UnitMatchers

import BWMirrorProxy.UnitInfo.FriendlyUnitInfo
import Utilities.TypeEnrichment.EnrichUnitType._

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.complete &&
      unit.impactsCombat &&
      unit.utype.canMove &&
      unit.utype.orderable &&
      ! unit.utype.isWorker
  }
}
