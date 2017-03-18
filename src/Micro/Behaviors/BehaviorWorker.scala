package Micro.Behaviors
import Micro.Intentions.Intention
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object BehaviorWorker extends Behavior {
  
  def execute(intent: Intention) {
    
    val enemiesInRange = intent.unit.enemiesInRange
    
    if (enemiesInRange.isEmpty) {
      if (intent.toBuild.isDefined) {
        if (intent.unit.pixelCenter.distancePixels(intent.destination.get.pixelCenter) < 32 * 6
        && With.game.isVisible(intent.destination.get)) {
          return With.commander.build(intent, intent.toBuild.get, intent.destination.get)
        }
      }
      else if (intent.toGather.isDefined) {
        return With.commander.gather(intent, intent.toGather.get)
      }
    }
    
    BehaviorDefault.execute(intent)
  }
}
