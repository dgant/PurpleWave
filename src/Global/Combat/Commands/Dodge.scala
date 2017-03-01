package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._

object Dodge extends Command{
  
  def execute(intent:Intention) {
  
    val unit = intent.unit
    val closestEnemy = intent.battle.map(_.enemy.units.minBy(_.distanceSquared(unit)))
    
    val kitePositions =
      (-5 to 5).flatten(dy =>
        (-5 to 5).map(dx =>
          unit.tilePosition.add(dx, dy)
        ))
        .filter(With.geography.isWalkable)
    
    if (kitePositions.nonEmpty) {
      With.commander.move(this, unit,
        kitePositions.map(_.toPosition)
          .sortBy(position => position.distanceSquared(With.geography.home))
          .maxBy(position => closestEnemy.get.distanceSquared(position).toInt/32))
    }
    else {
      With.logger.warn(unit.utype + " had nowhere to dodge near " + unit.tilePosition)
    }
  }
}
