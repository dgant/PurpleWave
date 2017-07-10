package Debugging.Visualizations

import Debugging.Visualizations.Views.Fun.{ShowHappyVision, ShowTextOnly}
import Debugging.Visualizations.Views.Geography.ShowBases
import Debugging.Visualizations.Views.Micro.{ShowUnits, ShowUnitsForeign, ShowUnitsFriendly}
import Debugging.Visualizations.Views.Performance.ShowPerformanceSummary
import Debugging.Visualizations.Views.Planning.{ShowPlans, ShowResources, ShowStrategy}
import Debugging.Visualizations.Views.ShowClock
import Lifecycle.With

import scala.util.Random

class Visualization {
  
  val lineHeightSmall = 9
  
  val views = Vector(
    ShowBases,
    ShowUnits,
    ShowUnitsForeign,
    ShowUnitsFriendly,
    ShowPerformanceSummary,
    ShowClock,
    ShowStrategy,
    ShowResources,
    ShowPlans
  )
  
  var enabled   = false
  var screen    = true
  var map       = false
  var happy     = false
  var textOnly  = false
  
  def render() {
    requireInitialization()
    if ( ! enabled) return
    if (With.tasks.tasks.exists(_.totalRuns == 0)) return
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    
    if (happy) {
      ShowHappyVision.render()
    }
    else if (textOnly) {
      ShowTextOnly.render()
    }
    else {
      if (map) {
        views.foreach(_.renderMap())
      }
      if (screen) {
        views.foreach(_.renderScreen())
      }
      
    }
  }
  
  private var initialized = false
  private def requireInitialization() {
    if (initialized) return
    initialized = true
    enabled = With.configuration.visualize
    var random = Random.nextDouble()
    random -= With.configuration.visualizationProbabilityHappyVision
    if (random < 0) {
      happy = true
    }
    else {
      random -= With.configuration.visualizationProbabilityTextOnly
      if (random < 0) {
        textOnly = true
      }
    }
  }
}
