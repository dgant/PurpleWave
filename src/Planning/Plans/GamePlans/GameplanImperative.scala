package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Predicates.MacroCounting

abstract class GameplanImperative extends Plan with Modal with MacroCounting with MacroActions {
  def activateIf: Boolean = true
  def concludeIf: Boolean = false

  override def onUpdate(): Unit = {
    if (activateIf && ! concludeIf) {
      execute()
    }
  }

  def execute(): Unit
}
