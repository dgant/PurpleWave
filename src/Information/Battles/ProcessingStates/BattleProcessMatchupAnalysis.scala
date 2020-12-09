package Information.Battles.ProcessingStates

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleProcessMatchupAnalysis extends BattleProcessState {

  lazy val units = new mutable.Queue[UnitInfo]
  units ++= With.battles.nextBattlesLocal.view.flatMap(_.teams.view.flatMap(_.units))

  override def step(): Unit = {
    if (units.isEmpty) {
      transitionTo(new BattleProcessComplete)
      return
    }

    val nextUnit = units.dequeue()

    // TODO: Create matchup analyses here so it doesn't clog up expectations of agency duration
  }
}
