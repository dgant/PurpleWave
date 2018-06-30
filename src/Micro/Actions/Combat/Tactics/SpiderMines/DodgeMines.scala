package Micro.Actions.Combat.Tactics.SpiderMines

d import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Dodge
import Micro.Coordination.Explosions.{ExplosionSpiderMineBlast, ExplosionSpiderMineTrigger}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class DodgeMines(mines: Iterable[UnitInfo]) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = true

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val explosions = mines.flatMap(mine => Vector(
      new ExplosionSpiderMineBlast(mine),
      new ExplosionSpiderMineTrigger(mine)
    ))

    new Dodge(explosions).consider(unit)
  }
}
