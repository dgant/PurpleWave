package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._

object Avoid extends Command {
 
  def execute(intent:Intention) {
    val unit = intent.unit
    val enemyAndProximity = intent.battle.get.enemy.units.map(enemy => (enemy, enemy.distance(unit) - enemy.range)).minBy(_._2)
    val doRetreat = enemyAndProximity._2 < 32 * 2
    
    if (doRetreat) {
      intent.destination = Some(With.geography.ourHarvestingAreas
        .map(area => area.start.midpoint(area.end).toPosition)
        .headOption
        .getOrElse(With.geography.home))
  
      March.execute(intent)
    } else {
      Reenforce.execute(intent)
    }
  }
}
