package Debugging.Visualizations

import Debugging.Visualizations.Views.Fun.{ShowHappyVision, ShowTextOnly}
import Debugging.Visualizations.Views.Performance.ShowPerformance
import Debugging.Visualizations.Views.View
import Lifecycle.With

import scala.util.Random

class Visualization {
  
  val lineHeightSmall = 9
  
  private val viewCycle = Vector(
    ShowPerformance
  )
  
  private var view: View = ShowPerformance
  private var lastCycle = 0
  
  var enabled   = true
  var cycle     = false
  var screen    = true
  var map       = true
  var happy     = false
  var textOnly  = false
  
  def setView(newView: View) {
    view = newView
  }
  
  def forceCycle() {
    cycle = false
    pickNextView()
  }
  
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
        view.renderMap()
      }
      if (screen) {
        view.renderScreen()
      }
      
      if (cycle && (With.frame - lastCycle) > 24 * 8) {
        pickNextView()
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
  
  private def pickNextView() {
    var i = -1
    if (viewCycle.contains(view)) {
      i = (viewCycle.indexOf(view) + 1) % viewCycle.length
    }
    if (i >= 0) {
      view = viewCycle(i)
      lastCycle = With.frame
    }
  }
}
