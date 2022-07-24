package Planning.Plans.GamePlans.All

import Planning.Plan
import Planning.Predicates.MacroCounting

abstract class GameplanImperative extends Plan with Modal with MacroCounting with MacroActions  {
  def activated: Boolean = true
  def completed: Boolean = false
  final def isComplete: Boolean = completed || ! activated

  var doBasics: Boolean = true
  var doBuildOrder: Boolean = true

  override def onUpdate(): Unit = {
    if ( ! activated) return
    if (isComplete) return
    status(toString)
    if (doBasics) {
      requireEssentials()
    }
    if (doBuildOrder) {
      executeBuild()
    }
    if (doBasics) {
      pumpSupply() // This currently just prioritizes Supplier
      pumpWorkers(oversaturate = false)
    }
    executeMain()
  }

  def executeBuild(): Unit = {}

  def executeMain(): Unit
}