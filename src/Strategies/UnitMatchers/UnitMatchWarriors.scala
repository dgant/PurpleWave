package Strategies.UnitMatchers

import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichUnitType._

object UnitMatchWarriors extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo):Boolean = {
    unit.complete &&
      unit.impactsCombat &&
      unit.utype.canMove &&
      unit.utype.orderable &&
      ! unit.utype.isWorker
  }
}
