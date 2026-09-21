package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeDarkTemplar extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.DarkTemplar(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.readyForAttackOrder && With.enemies.exists(_.isTerran)) {
      val mines = unit.matchups.threats.filter(Terran.SpiderMine)
      if (mines.nonEmpty) {
        val mine      = mines.minBy(_.pixelDistanceEdge(unit))
        val radius    = 96
        var snipe     = mine.orderTarget.contains(unit)
        snipe       ||= mine.pixelDistanceEdge(unit) < radius && mines.count(_.pixelDistanceEdge(mine) < radius) == 1
        snipe       &&= mine.visible
        snipe       &&= ! mine.effectivelyCloaked
        if (snipe) {
          unit.agent.toAttack = Some(mine)
          Commander.attack(unit)
        }
      }
    }
  }
}
