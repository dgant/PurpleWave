package Gameplans.All

import Macro.Actions.{MacroActions, RequireEssentials}
import Planning.Plans.Plan

abstract class GameplanImperative extends Plan with Modal with MacroActions  {
  def activated: Boolean = true
  def completed: Boolean = false
  final def isComplete: Boolean = completed || ! activated

  var doBasics: Boolean = true
  var doBuildOrder: Boolean = true
  def doWorkers(): Unit = {
    pumpWorkers(maximumConcurrently = 2, oversaturate = false)
  }

  override def onUpdate(): Unit = {
    if ( ! activated) return
    if (isComplete) return
    status(toString)
    if (doBasics) {
      RequireEssentials()
    }
    if (doBuildOrder) {
      executeBuild()
    }
    if (doBasics) {
      pumpSupply() // This currently just prioritizes Supplier
      doWorkers()
    }
    executeMain()
  }

  def executeBuild(): Unit = {}

  def executeMain(): Unit
}