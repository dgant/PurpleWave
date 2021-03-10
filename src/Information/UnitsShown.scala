package Information

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
  
  def apply(player: PlayerInfo, unitClass: UnitClass): Int = unitCounts(player)(unitClass).size

  def allEnemies(unitClass: UnitClass): Int = With.enemies.map(apply(_, unitClass)).sum
  def any(unitClasses: UnitClass*): Boolean = unitClasses.exists(unitClass => With.enemies.exists(apply(_, unitClass) > 0))

  def all(player: PlayerInfo): Map[UnitClass, mutable.HashSet[Int]] = unitCounts(player)

  override protected def onRun(budgetMs: Long): Unit = {
    With.units.all.foreach(unit => unitCounts(unit.player)(unit.unitClass) += unit.id)
  }
}
