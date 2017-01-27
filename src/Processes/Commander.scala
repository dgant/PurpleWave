package Processes

import Startup.With
import Types.Tactics.Tactic
import bwapi.UnitCommandType

import scala.collection.JavaConverters._

class Commander {
  def command(tactics: Iterable[Tactic]) {
    //Let's keep it real simple, for the moment
    tactics.foreach(tactic => {
      if (tactic.unit.getLastCommand.getUnitCommandType != UnitCommandType.Gather) {
        With.game.neutral.getUnits.asScala
          .filter(unit => unit.getType.isMineralField)
          .take(1)
          .foreach(mineral => tactic.unit.gather(mineral))
      }
    })
  }
}
