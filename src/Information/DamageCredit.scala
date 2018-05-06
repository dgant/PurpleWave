package Information

import Lifecycle.With
import Micro.Decisions.MicroValue
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class DamageCredit {
  
  val value = new mutable.HashMap[PlayerInfo, mutable.HashMap[UnitClass, Double]] ++= Players.all.map((_, new mutable.HashMap[UnitClass, Double]))
  
  def onKill(unit: UnitInfo) {
    unit.lastAttacker.foreach(attacker => attacker.creditKill(Kill(attacker, unit.unitClass, With.frame)))
  }
  
  def onDamage(from: UnitInfo, to: UnitInfo) {
    value(from.player)(from.unitClass) =
      value(from.player).getOrElse(from.unitClass, 0.0) +
      MicroValue.valuePerAttackMaxHp(from, to )
  }
}
