package Planning.Composition.UnitMatchers

import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

object UnitMatchWorker extends UnitMatcher{
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    return unit.utype.isWorker
  }
}
