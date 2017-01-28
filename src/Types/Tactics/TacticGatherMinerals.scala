package Types.Tactics

import Startup.With
import bwapi.UnitCommandType
import bwta.BWTA

class TacticGatherMinerals(unit:bwapi.Unit) extends Tactic(unit) {
  override def execute() = {
    if (unit.getLastCommand.getUnitCommandType != UnitCommandType.Gather) {
      unit.gather(BWTA.getStartLocation(With.game.self).getMinerals.get(0))
    }
  }
}
