package Information.Battles.Types

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.TFriendlyUnitGroup

class FriendlyTeam(battle: Battle, override val groupFriendlyUnits: Seq[FriendlyUnitInfo]) extends Team(battle, groupFriendlyUnits.map(_.asInstanceOf[UnitInfo])) with TFriendlyUnitGroup
