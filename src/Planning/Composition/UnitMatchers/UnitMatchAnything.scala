package Planning.Composition.UnitMatchers
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

object UnitMatchAnything extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = true
}
