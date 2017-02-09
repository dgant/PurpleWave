package Plans.Generic.Defense

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSpecific
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.UnitType

import scala.collection.JavaConverters._
import scala.collection.mutable

class DefeatWorkerHarass extends Plan {
  
  val _townHallTypes = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  )
  
  val _enemyDefense = new mutable.HashMap[bwapi.Unit, LockUnits]
  val _enemyUpdateFrames = new mutable.HashMap[bwapi.Unit, Integer]
  
  override def getChildren: Iterable[Plan] = { _enemyDefense.values }
  
  override def onFrame() {
    //Release defenders shortly after defender leaves the box
    _enemyUpdateFrames
        .filter(pair => pair._2 + 8 < With.game.getFrameCount)
        .foreach(pair => {
          _enemyDefense.get(pair._1).foreach(defenders => With.recruiter.remove(defenders))
          _enemyDefense.remove(pair._1)
          _enemyUpdateFrames.remove(pair._1)
        })
    With.ourUnits
      .filter(unit => _townHallTypes.contains(unit.getType))
      .foreach(_defendBaseWorkers)
  
    _enemyDefense.foreach(pair => {
      pair._2.onFrame()
      pair._2.units.foreach(defender => defender.attack(pair._1))
    })
  }
  
  def _defendBaseWorkers(base:bwapi.Unit) {
    val nearbyUnits = base.getUnitsInRadius(32 * 8).asScala
    val nearbyEnemies = nearbyUnits
      .filter(unit => unit.getPlayer.isEnemy(With.game.self))
      .filter(unit => unit.canAttack)
      .filter(unit => ! unit.isFlying)
      .toSet
    
    if (nearbyEnemies.isEmpty) {
      return
    }
    
    val minerals = nearbyUnits.filter(_.getType == UnitType.Resource_Mineral_Field)
    val geysers = nearbyUnits.filter(_.getType.isRefinery)
    if (minerals.isEmpty) {
      return
    }
    
    //Draw a box around the area
    val top           = (minerals ++ geysers :+ base).map(_.getTop   ).min - 32
    val bottom        = (minerals ++ geysers :+ base).map(_.getBottom).max - 32
    val left          = (minerals ++ geysers :+ base).map(_.getLeft  ).min - 32
    val right         = (minerals ++ geysers :+ base).map(_.getRight ).max - 32
    val enemiesInBox  = With.game.getUnitsInRectangle(left, top, right, bottom).asScala.toSet.intersect(nearbyEnemies)
    
    enemiesInBox.foreach(_defendFromEnemy)
  }
  
  def _defendFromEnemy(enemy:bwapi.Unit) {
    if ( ! _enemyDefense.contains(enemy)) {
      _enemyDefense.put(
        enemy,
        new LockUnitsExactly {
          this.description.set(Some("Eject enemy scout"))
          this.quantity.set(2)
          this.unitMatcher.set(UnitMatchWorker)
          this.unitPreference.set(new UnitPreferClose {
            this.positionFinder.set(new PositionSpecific(enemy.getTilePosition))
          })
        })
    }
    _enemyUpdateFrames.put(enemy, With.game.getFrameCount)
  }
  
}
