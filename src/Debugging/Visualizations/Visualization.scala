package Debugging.Visualizations

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Battles._
import Debugging.Visualizations.Views.Economy._
import Debugging.Visualizations.Views.Fun._
import Debugging.Visualizations.Views.Geography._
import Debugging.Visualizations.Views.Micro._
import Debugging.Visualizations.Views.Performance._
import Debugging.Visualizations.Views.Planning._
import Debugging.Visualizations.Views.{DebugView, ShowHeader, ShowStoryteller}
import Lifecycle.With
import Mathematics.Points.Pixel
import Performance.Tasks.TimedTask
import bwapi.{MouseButton, Text}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class Visualization extends TimedTask {

  withSkipsMax(0)
  withCosmetic(true)
  
  //////////////
  // Settings //
  //////////////
  
  var views: ArrayBuffer[DebugView] = mutable.ArrayBuffer[DebugView](
    // Evergreen views
    ShowHeader,
    ShowGrids,
    ShowUtilization,
    ShowHealthAndCooldown,
    ShowUnitPaths,
    ShowUnitsFriendly,
    ShowUnitsEnemy,
    //ShowStoryteller,

    // Temporary views
    // ShowZones,
    ShowFormations,
    // ShowPlacement,
    // ShowFoundations,
    // ShowExpansions
  )

  lazy val knownViews: Vector[DebugView] = Vector[DebugView](
    ShowAccelerants,
    ShowAccounting,
    ShowArchitecture,
    ShowBattles,
    ShowBlackScreen,
    ShowBulletsAsHearts,
    ShowBWEB,
    ShowClustering,
    ShowDivisions,
    ShowDoom,
    ShowExpansions,
    ShowHeader,
    ShowFingerprints,
    ShowFormations,
    ShowFoundations,
    ShowGas,
    ShowGradients,
    ShowGrids,
    ShowHappyUnits,
    ShowHappyVision,
    ShowHistory,
    ShowIgnorance,
    ShowJudgment,
    ShowKills,
    ShowMacroSim,
    ShowHealthAndCooldown,
    ShowUnitsEnemy,
    ShowUnitsFriendly,
    ShowUtilization,
    ShowPerformance,
    ShowProduction,
    ShowPlacement,
    ShowPushes,
    ShowReactionTime,
    ShowBank,
    ShowRushDistances,
    ShowSquads,
    ShowStoryteller,
    ShowTeams,
    ShowTextOnly,
    ShowTileInfo,
    ShowUnitPaths,
    ShowZonePaths,
    ShowZones
  )
  
  var enabled   : Boolean = _
  var screen    : Boolean = _
  var map       : Boolean = _
  var happy     : Boolean = _
  var textOnly  : Boolean = _
  
  //////////////
  
  val lineHeightSmall = 9
  
  def toggle(view: DebugView): Unit = {
    if (views.contains(view)) {
      views -= view
    } else {
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
  
  override def onRun(budgetMs: Long): Unit = {
    initialize()
    if ( ! enabled) return
    if (With.tasks.tasks.exists(_.runsTotal == 0)) return
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
          val tile = pixel.tile
          DrawMap.label(tile.toString, pixel.subtract(0, 15))
        }
      }
    }
    With.animations.render()
  }
  
  private var initialized = false
  private def initialize(): Unit = {
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
}
