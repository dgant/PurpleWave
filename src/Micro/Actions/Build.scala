package Micro.Actions
import Lifecycle.With
import Micro.Intent.Intention

object Build extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.toBuild.isDefined
  }
  
  override def perform(intent: Intention): Boolean = {
    With.commander.build(intent, intent.toBuild.get, intent.toBuildTile.get)
    intent.toBuild = None //Avoid repeating the build
    true
  }
}
