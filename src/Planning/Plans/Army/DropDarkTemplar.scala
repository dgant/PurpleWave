package Planning.Plans.Army

import Planning.Composition.UnitMatchers.UnitMatchOr
import ProxyBwapi.Races.Protoss

class DropDarkTemplar extends DropAttack {
  paratrooperMatcher.set(UnitMatchOr(Protoss.DarkTemplar))
}
