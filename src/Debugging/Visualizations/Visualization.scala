package Debugging.Visualizations

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Battles.{ShowBattle, ShowClustering}
import Debugging.Visualizations.Views.Economy._
import Debugging.Visualizations.Views.Fun._
import Debugging.Visualizations.Views.Geography._
import Debugging.Visualizations.Views.Micro._
import Debugging.Visualizations.Views.Performance.{ShowPerformanceDetails, ShowPerformanceSummary, ShowReactionTime}
import Debugging.Visualizations.Views.Planning._
import Debugging.Visualizations.Views.{ShowClock, View}
import Lifecycle.With
import bwapi.Text
import Mathematics.Points.Pixel
import bwapi.MouseButton

import scala.collection.mutable
import scala.util.Random

class Visualization {
  
  //////////////
  // Settings //
  //////////////
  
  var views = mutable.ArrayBuffer(
    // Evergreen views
    ShowClock,
    ShowStrategyName,
    ShowHealthAndCooldown,
    ShowUtilization,
    ShowUnitsFriendly,
    ShowUnitsEnemy,
    ShowExplosions,
    ShowStatus,
    ShowPerformanceSummary,

    // Temporary views
    ShowPreplacement,
    //ShowArchitecture,
    //ShowPlans
  )
  
  var enabled   : Boolean = _
  var screen    : Boolean = _
  var map       : Boolean = _
  var happy     : Boolean = _
  var textOnly  : Boolean = _
  
  //////////////
  
  val lineHeightSmall = 9
  
  def toggle(view: View) {
    if (views.contains(view)) {
      views -= view
    }
    else {
      views += view
    }
  }
  
  def tryToggle(viewName: String): Boolean = {
    val matches = knownViews.filter(_.name.toLowerCase.contains(viewName.toLowerCase))
    val matched = matches.size == 1
    if (matched) {
      toggle(matches.head)
    }
    matched
  }
  
  def render() {
    requireInitialization()
    if ( ! enabled) return
    if (With.tasks.tasks.exists(_.totalRuns == 0)) return
    With.game.setTextSize(Text.Size.Small)
    
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
        if (With.game.getMouseState(MouseButton.M_LEFT)) {
          val mouse = new Pixel(With.game.getMousePosition)
          val pixel = With.viewport.start + mouse
          val tile = pixel.tileIncluding
          DrawMap.label(tile.toString, pixel.subtract(0, 15))
        }
      }
    }
  }
  
  private var initialized = false
  private def requireInitialization() {
    if (initialized) return
    initialized = true
    enabled = With.configuration.visualizeDebug
    screen  = With.configuration.visualizeScreen
    map     = With.configuration.visualizeMap
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
  
  lazy val knownViews: Vector[View] = Vector[View](
    ShowArchitecture,
    ShowBases,
    ShowBattle,
    ShowBlackScreen,
    ShowBulletsAsHearts,
    ShowCarriers,
    ShowClustering,
    ShowDesire,
    ShowEconomy,
    ShowExplosions,
    ShowFingerprints,
    ShowGas,
    ShowGradients,
    ShowGrids,
    ShowHappyUnits,
    ShowHappyVision,
    ShowHistory,
    ShowIgnorance,
    ShowKills,
    ShowMobility,
    ShowHealthAndCooldown,
    ShowUnitsEnemy,
    ShowUnitsFriendly,
    ShowPerformanceDetails,
    ShowPerformanceSummary,
    ShowProduction,
    ShowPlans,
    ShowPreplacement,
    ShowReactionTime,
    ShowResources,
    ShowRushDistances,
    ShowSaturation,
    ShowScheduler,
    ShowSquads,
    ShowStatus,
    ShowStrategyInterest,
    ShowStrategyName,
    ShowStrategyEvaluations,
    ShowTechniques,
    ShowTextOnly,
    ShowTextOnlyUnits,
    ShowTileInfo,
    ShowUnitCounts,
    ShowUnitPaths,
    ShowZoneBorderTiles,
    ShowZoneLabels,
    ShowZonePathDemo,
    ShowZones
  )
}
