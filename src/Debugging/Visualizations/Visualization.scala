package Debugging.Visualizations

import Debugging.Visualizations.Views.Battles.ViewBattles
import Debugging.Visualizations.Views.Economy.ViewEconomy
import Debugging.Visualizations.Views.Fun._
import Debugging.Visualizations.Views.Geography._
import Debugging.Visualizations.Views.Micro._
import Debugging.Visualizations.Views.Performance.ViewPerformance
import Debugging.Visualizations.Views.Planning.ViewPlanning
import Debugging.Visualizations.Views.{View, ViewNothing}
import Lifecycle.With

class Visualization {
  
  val lineHeightSmall = 9
  
  private val viewCycle = Vector(
    ViewBattles,
    ViewEconomy,
    ViewGeography,
    ViewMicro,
    ViewPerformance,
    ViewPlanning
  )
  
  private var view: View = ViewNothing
  private var lastCycle = 0
  
  def setView(newView: View) {
    view = newView
  }
  
  def render() {
    if (With.configuration.visualize) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  
      view.render()
      
      if (With.configuration.cycleViews && (With.frame -lastCycle) > 24 * 8) {
        pickNextView()
      }
    }
  }
  
  private var initialized = false
  private def requireInitialization() {
    if (initialized) return
    initialized = true
    
    if (With.configuration.viewTextOnly)      view = ViewTextOnly
    if (With.configuration.viewHappyVision)   view = ViewHappy
    if (With.configuration.viewBattles)       view = ViewBattles
    if (With.configuration.viewEconomy)       view = ViewEconomy
    if (With.configuration.viewGeography)     view = ViewGeography
    if (With.configuration.viewMicro)         view = ViewMicro
    if (With.configuration.viewPerformance)   view = ViewPerformance
    if (With.configuration.viewPlanning)      view = ViewPlanning
  }
  
  private def pickNextView() {
    var i = -1
    if (view == ViewNothing) {
      i = 0
    }
    if (viewCycle.contains(view)) {
      i = (viewCycle.indexOf(view) + 1) % viewCycle.length
    }
    if (i >= 0) {
      view = viewCycle(i)
      lastCycle = With.frame
    }
  }
}
