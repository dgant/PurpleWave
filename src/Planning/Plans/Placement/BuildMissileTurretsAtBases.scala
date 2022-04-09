package Planning.Plans.Placement

import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtBases(initialCount: Int) extends BuildTowersAtBases(initialCount, Terran.MissileTurret)