package Micro.Actions.Combat.Tactics.SpiderMines

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object HandleMines extends Action{

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && ! unit.flying
    && unit.matchups.enemies.exists(isRelevantMine(unit, _))
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    // Decide whether to Drag, Defuse, or Dodge mines
    //
    // Spider Mines rely on normal target acquisition AI range math, which is 96px + attack range edge-to-edge
    // However, this is often limited by Spider Mine sight range, which is 96px exactly.
    // According to jaj22 the acqusition range is center-to-center (unlike most units, which are edge-to-edge)

    val mines = unit.matchups.enemies.filter(isRelevantMine(unit, _))
    val mine = mines.minBy(_.pixelDistanceEdge(unit))

    lazy val canKillForFree   = ! mine.effectivelyCloaked & unit.pixelRangeAgainst(mine) > 96 && unit.readyForAttackOrder
    lazy val canSnipeAlone    = unit.damageOnNextHitAgainst(mine) >= mine.unitClass.maxTotalHealth
    lazy val canSnipeTogether = mine.matchups.threatsInRange.map(t => if (t.readyForAttackOrder) t.damageOnNextHitAgainst(mine) else 0).sum > mine.unitClass.maxTotalHealth
    lazy val dragTargets =
      if (unit.unitClass.floats)
        Iterable.empty
      else
        unit.matchups.enemies.filter(e => ! e.flying && ! e.unitClass.isBuilding && e.pixelDistanceEdge(mine) < 96)

    if (canKillForFree) {
      unit.agent.toAttack = Some(mine)
      Attack.delegate(unit)
    }
    else if (canSnipeAlone) {
      new Defuse(unit, mine).delegate(unit)
    }
    else if (canSnipeTogether) {
      new Defuse(unit, mine).delegate(unit)
    }
    else if (dragTargets.nonEmpty) {
      new Drag(dragTargets).consider(unit)
    }
    else {
     new DodgeMines(mines).consider(unit)
    }
  }

  protected def isRelevantMine(unit: FriendlyUnitInfo, other: UnitInfo): Boolean = {
    other.isEnemy && other.is(Terran.SpiderMine) && unit.pixelDistanceEdge(other) < 32 * 5
  }
}
