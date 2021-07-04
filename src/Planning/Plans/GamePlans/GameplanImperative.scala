package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Predicates.MacroCounting

abstract class GameplanImperative extends Plan with Modal with MacroCounting with MacroActions {
  var complete: Boolean = false
  var doBasics: Boolean = true
  var doBuildOrder: Boolean = true
  override def isComplete: Boolean = complete

  override def onUpdate(): Unit = {
    if (isComplete) return
    if (doBasics) {
      requireEssentials()
    }
    if (doBuildOrder) {
      buildOrder()
    }
    if (doBasics) {
      pumpSupply()
      pumpWorkers()
    }
    execute()
  }

  def doIf: Boolean = true

  def buildOrder(): Unit = {}

  def execute(): Unit
}