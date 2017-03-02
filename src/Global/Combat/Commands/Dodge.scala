package Global.Combat.Commands

import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._

object Dodge extends Command{
  
  def execute(intent:Intention) {
  
    val unit = intent.unit
    
    if (intent.battle.isEmpty) {
      Flee.execute(intent)
      return
    }
    
    val kitePositions =
      (-3 to 3).flatten(dy =>
        (-3 to 3).map(dx =>
          unit.tilePosition.add(dx, dy)
        ))
        .filter(With.maps.walkability.get(_) > 0)
    
    if (kitePositions.nonEmpty) {
      With.commander.move(this, unit,
        kitePositions
          .maxBy(tilePosition =>
            With.maps.mobility.get(tilePosition)
              / (1 + With.maps.enemyGroundStrength.get(tilePosition)))
          .toPosition)
    }
    else {
      With.logger.warn(unit.utype + " had nowhere to dodge near " + unit.tilePosition)
    }
  }
}
