package Planning.Plans.Macro.Terran

import Macro.Architecture.Heuristics.{PlacementProfile, PlacementProfiles}
import Planning.Plans.Macro.Protoss.BuildTowersAtBases
import ProxyBwapi.Races.Terran

class BuildMissileTurretsAtBases(
  initialCount: Int,
  placement: PlacementProfile = PlacementProfiles.hugWorkersWithCannon)
    extends BuildTowersAtBases(initialCount, placement, placement, Terran.MissileTurret)