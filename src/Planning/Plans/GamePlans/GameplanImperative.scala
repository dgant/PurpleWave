package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Predicates.MacroCounting

abstract class GameplanImperative extends Plan with Modal with MacroCounting with MacroActions {
  def activated: Boolean = true
  def completed: Boolean = false
  final def isComplete: Boolean = completed || ! activated

  var doBasics: Boolean = true
  var doBuildOrder: Boolean = true

  override def onUpdate(): Unit = {
    if ( ! activated) return
    if (isComplete) return
    if (doBasics) {
      requireEssentials()
    }
    if (doBuildOrder) {
      executeBuild()
    }
    if (doBasics) {
      pumpSupply()
      pumpWorkers()
    }
    execute()
  }

  def executeBuild(): Unit = {}

  def execute(): Unit
}