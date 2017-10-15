package Macro.Decisions

import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.UnitClass
import bwapi.Race

import scala.collection.mutable

object Desires  {
  
  def haves: Map[UnitClass, Double] = {
    With.units.ours.groupBy(_.unitClass).map(x => (x._1, x._2.size.toDouble))
  }
  
  def wants(context: DesireContext): Map[UnitClass, Double] = {
    
    val desires = new mutable.HashMap[UnitClass, Double] {
      override def default(key: UnitClass): Double = 0.0
    }
    
    if (With.enemy.raceInitial == Race.Terran) {
      desires(Protoss.Dragoon) += 12.0
    }
    else if (With.enemy.raceInitial == Race.Protoss) {
      desires(Protoss.Zealot)  += 2.0
      desires(Protoss.Dragoon) += 6.0
    }
    else if (With.enemy.raceInitial == Race.Zerg) {
      desires(Protoss.Zealot)  += 8.0
      desires(Protoss.Corsair) += 2.0
    }
    else {
      desires(Protoss.Zealot)  += 1.0
      desires(Protoss.Dragoon) += 1.0
    }
    
    DesireRatios.ratios.foreach(ratio => {
      desires(ratio.unit) += ratio.addedQuantity(context)
      desires(ratio.unit) *= PurpleMath.nanToOne(context.minerals / ratio.unit.mineralPrice)
      desires(ratio.unit) *= PurpleMath.nanToOne(context.gas      / ratio.unit.gasPrice)
    })
    
    val output = desires.map(desire => DesireScales.forUnit(desire._1).scale(desire._2))
    
    desires.toMap
  }
}
