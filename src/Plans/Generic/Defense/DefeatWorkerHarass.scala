package Plans.Generic.Defense

import Geometry.TileRectangle
import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSpecific
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose

import scala.collection.JavaConverters._
import scala.collection.mutable

class DefeatWorkerHarass extends Plan {
  
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
    
    With.map.ourHarvestingAreas.foreach(_defendBaseWorkers)
  
    _enemyDefense.foreach(pair => {
      pair._2.onFrame()
      pair._2.units.foreach(defender => defender.attack(pair._1))
    })
  }
  
  def _defendBaseWorkers(miningArea:TileRectangle) {
    val enemiesInBox = With.game.getUnitsInRectangle(
      miningArea.start.getX * 32,
      miningArea.start.getY * 32,
      miningArea.end.getX   * 32,
      miningArea.end.getY   * 32)
    .asScala
    .filter(unit => unit.getPlayer.isEnemy(With.game.self))
    .filter(unit => unit.getType.canAttack)
    .filter(unit => ! unit.isFlying)
    
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
