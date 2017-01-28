package UnitMatching.Matcher

import bwapi.Unit

object UnitMatchWorker extends UnitMatch{
  override def accept(unit: Unit): Boolean = {
    return unit.canGather
  }
}
