package UnitMatching.Matcher

import bwapi.Unit

object UnitMatchWorker extends UnitMatcher{
  override def accept(unit: Unit): Boolean = {
    return unit.canGather
  }
}
