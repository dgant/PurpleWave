package Planning.Plans.Scouting

import ProxyBwapi.Races.Zerg

class ScoutWithOverlord extends Scout {
  scouts.get.unitMatcher.set(Zerg.Overlord)
}
