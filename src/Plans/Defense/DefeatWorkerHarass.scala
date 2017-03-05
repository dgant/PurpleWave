package Plans.Defense

import Geometry.TileRectangle
import Global.Combat.Commands.Hunt
import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSpecific
import Strategies.UnitCounters.UnitCountExactly
import Strategies.UnitMatchers.UnitMatchWorker
import Strategies.UnitPreferences.UnitPreferClose
import Types.Intents.Intention
import Types.UnitInfo.UnitInfo

import scala.collection.mutable

class DefeatWorkerHarass extends Plan {
  
  val _enemyDefense = new mutable.HashMap[UnitInfo, LockUnits]
  val _enemyUpdateFrames = new mutable.HashMap[UnitInfo, Integer]
  
  override def getChildren: Iterable[Plan] = _enemyDefense.values
  
  override def onFrame() {
    //Release defenders shortly after defender leaves the box
    _enemyUpdateFrames
        .filter(pair => pair._2 + 8 < With.game.getFrameCount)
        .map(_._1)
        .foreach(enemy => {
          _enemyDefense.get(enemy).foreach(defenders => With.recruiter._forgetRequest(defenders))
          _enemyDefense.remove(enemy)
          _enemyUpdateFrames.remove(enemy)
        })
    
    With.geography.ourHarvestingAreas.foreach(_defendBaseWorkers)
  
    _enemyDefense.foreach(pair => {
      pair._2.onFrame()
      pair._2.units.foreach(defender =>
        With.units.enemy.filter(_.id == pair._1).foreach(enemy =>
          With.commander.intend(new Intention(defender, Hunt, enemy.tileCenter) { targetUnit = Some(enemy) })))
    })
  }
  
  def _defendBaseWorkers(miningArea:TileRectangle) =
    With.units.inRectangle(miningArea)
      .filter(_.isEnemy)
      .filter(_.canFight)
      .filter(! _.flying)
      .foreach(_defendFromEnemy)
  
  def _defendFromEnemy(enemy:UnitInfo) {
    if ( ! _enemyDefense.contains(enemy)) {
      _enemyDefense.put(
        enemy,
        new LockUnits {
          description.set(Some("Eject enemy scout"))
          unitCounter.set(new UnitCountExactly(2))
          unitMatcher.set(UnitMatchWorker)
          unitPreference.set(new UnitPreferClose {
            positionFinder.set(new PositionSpecific(enemy.tileCenter))
          })
        })
    }
    _enemyUpdateFrames.put(enemy, With.game.getFrameCount)
  }
}
