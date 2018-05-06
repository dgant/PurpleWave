package Information.Intelligenze

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
  
  def apply(player: PlayerInfo, unitClass: UnitClass): Int = {
    unitCounts(player)(unitClass).size
  }
  
  def update() {
    With.units.all.foreach(unit => {
      unitCounts(unit.player)(unit.unitClass) += unit.id
    })
  }
}
