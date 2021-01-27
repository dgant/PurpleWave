package Information

import Lifecycle.With
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}

import scala.collection.mutable

class UnitsShown {
  
  private lazy val unitCounts =
    Players.all.map(player =>
      (player, UnitClasses.all.map(clazz =>
        (clazz, new mutable.HashSet[Int])).toMap))
      .toMap

  def update() {
    With.units.all.foreach(unit => unitCounts(unit.player)(unit.unitClass) += unit.id)
  }
  
  def apply(player: PlayerInfo, unitClass: UnitClass): Int = unitCounts(player)(unitClass).size

  def allEnemies(unitClass: UnitClass): Int = With.enemies.map(apply(_, unitClass)).sum

  def all(player: PlayerInfo): Map[UnitClass, mutable.HashSet[Int]] = unitCounts(player)
}
