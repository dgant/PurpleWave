package Planning.Plans.GamePlans.All

import Planning.Plan
import Planning.Predicates.MacroCounting

abstract class GameplanImperative extends Plan with Modal with MacroCounting with MacroActions  {
  def activated: Boolean = true
  def completed: Boolean = false
  final def isComplete: Boolean = completed || ! activated

  var doBasics: Boolean = true
  var doBuildOrder: Boolean = true
  var oversaturate: Boolean = false

  override def onUpdate(): Unit = {
    if ( ! activated) return
    if (isComplete) return
    status(toString)
    if (oversaturate) status("Oversaturate")
    if (doBasics) {
      requireEssentials()
    }
    if (doBuildOrder) {
      executeBuild()
    }
    if (doBasics) {
      pumpSupply()
      pumpWorkers(oversaturate = false)
    }
    executeMain()
    if (oversaturate) {
      pumpWorkers(oversaturate = true)
    }
  }

  def executeBuild(): Unit = {}

  def executeMain(): Unit
}