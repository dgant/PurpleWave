package Planning.Plans.Army

import Planning.UnitMatchers.UnitMatchOr
import ProxyBwapi.Races.Protoss

class DropReaver extends DropAttack {
  paratrooperMatcher.set(UnitMatchOr(Protoss.Zealot, Protoss.Reaver))
}
