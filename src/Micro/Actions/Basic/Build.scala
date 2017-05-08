package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Intent.Intention

object Build extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.toBuild.isDefined
  }
  
  override def perform(intent: Intention) {
    With.commander.build(intent.unit, intent.toBuild.get, intent.toBuildTile.get)
  }
}
