package Planning.Plans.Army

import Debugging.Visualizations.Rendering.DrawMap
import Micro.Intent.Intention
import Planning.Composition.PixelFinders.TileFinder
import Planning.Composition.PixelFinders.Tactics.TileEnemyBase
import Planning.Composition.Property
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Composition.ResourceLocks.LockUnits
import Lifecycle.With
import Utilities.EnrichPixel._

class Hunt extends Plan {
  
  description.set("Find some stray units to kill")
  
  val hunters = new Property[LockUnits](new LockUnits { unitMatcher.set(UnitMatchWarriors) })
  var position = new Property[TileFinder](new TileEnemyBase)
  
  override def update() {
    
    val targetPixel = position.get.find.getOrElse(With.intelligence.mostBaselikeEnemyPixel)
    
    hunters.get.acquire(this)
    if (hunters.get.satisfied) {
      hunters.get.units.foreach(fighter => {
        val targets = With.units.enemy.filter(fighter.canAttackThisSecond)
        val targetDestination =
          if(targets.isEmpty)
            targetPixel
          else
            targets.minBy(candidate =>
              candidate.pixelDistanceSquared(fighter)
            + candidate.pixelDistanceSquared(With.geography.home.pixelCenter)).tileIncludingCenter
        
        With.executor.intend(new Intention(this, fighter) { destination = Some(targetPixel) })
      })
    }
  }
  
  override def drawOverlay() {
    
    position.get.find.map(tile => {
      DrawMap.circle(
        tile.pixelCenter,
        64,
        With.self.colorDark)
      
      DrawMap.label(
        description.get,
        tile.pixelCenter,
        drawBackground = true,
        With.self.colorDark)
    })
  }
}
