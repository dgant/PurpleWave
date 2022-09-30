package Information.Battles.Types

import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.UnitGroup

class EnemyTeam(battle: Battle, units: Seq[UnitInfo]) extends Team(battle, units) with UnitGroup {

}
