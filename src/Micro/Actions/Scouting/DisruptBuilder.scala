package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Targeting.Target
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object DisruptBuilder extends Action {

  def scaryThreats(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.threats.view.filter(t => t.pixelsToGetInRange(unit) < 64 && ! t.constructing)
  }

  def disruptableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.enemies.filter(e => e.unitClass.isWorker && e.constructing)
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val scaries = scaryThreats(unit)
    lazy val targets = disruptableBuilders(unit)
    (unit.canAttack
     && With.scouting.enemyMain.isDefined
     && With.enemies.exists(_.raceInitial == Race.Terran)
     && targets.nonEmpty
     && scaries.size < 2
     && (scaries.isEmpty || (unit.totalHealth > 5 && targets.exists(_.totalHealth < unit.totalHealth))))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Target.chooseUnfiltered(unit, disruptableBuilders(unit))
    Commander.attack(unit)
  }
}
