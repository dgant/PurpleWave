package Information.Scouting

import Lifecycle.With
import Performance.Tasks.TimedTask
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}

import scala.collection.mutable

class UnitsShown extends TimedTask {
  
  private lazy val unitCounts =
    Players.all.map(player =>
      (player, UnitClasses.all.map(clazz =>
        (clazz, new mutable.HashSet[Int])).toMap))
      .toMap
  
  def apply(player: PlayerInfo, unitClasses: UnitClass*): Int = unitClasses.map(unitCounts(player)(_).size).sum

  def all(player: PlayerInfo): Map[UnitClass, mutable.HashSet[Int]] = unitCounts(player)
  def allEnemies(unitClasses: UnitClass*): Int = With.enemies.map(apply(_, unitClasses: _*)).sum
  def any(unitClasses: UnitClass*): Boolean = unitClasses.exists(unitClass => With.enemies.exists(apply(_, unitClass) > 0))

  override protected def onRun(budgetMs: Long): Unit = {
    With.units.all.foreach(unit => unitCounts(unit.player)(unit.unitClass) += unit.id)
  }
}
