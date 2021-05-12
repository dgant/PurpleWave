package Lifecycle

import Strategery.{DefaultPlaybook, Playbook}
import Utilities.Seconds

class Configuration {

  /////////////////////////
  // Tournament settings //s
  /////////////////////////

  var enableSurrenders  = false
  var enableChat        = false
  var frameTargetMs     = 20
  var frameLimitMs      = 55

  ///////////////////
  // Mode settings //
  ///////////////////


  var logstd            = false
  var humanMode         = false
  var visualizeFun      = false
  var visualizeDebug    = false
  var debugging         = false
  var detectBreakpoints = false
  var camera            = false

  //////////////
  // Strategy //
  //////////////

  var dynamicStickiness   = 15.0
  var targetWinrate       = 0.8
  var historyHalfLife     = 3.0
  var recentFingerprints  = 2
  var forcedStrategy: Option[String] = None
  var forcedPlaybook: Option[Playbook] = None
  def playbook: Playbook = forcedPlaybook.getOrElse(DefaultPlaybook)
  
  /////////////
  // Battles //
  /////////////

  var enableMCRS                    = false
  var avatarBattleDistancePixels    = 32.0 * 6.0
  var battleHysteresisFrames        = Seconds(6)()
  var simulationFrames              = Seconds(15)()
  var simulationResolution          = 8
  var simulationBonusTankRange      = 64.0
  var simulationDamageValueRatio    = 0.1
  
  ///////////
  // Macro //
  ///////////
  
  var minimumMineralsBeforeMinedOut   = 150 * 8
  var maxFramesToSendAdvanceBuilder   = Seconds(40)()
  var blockerMineralThreshold         = 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  var enableTightBuildingPlacement    = false
  
  /////////////////
  // Performance //
  /////////////////

  var enablePerformancePauses             = true
  var maximumGamesHistoryPerOpponent      = 500
  var logTaskDuration                     = false
  
  var buildingPlacementRefreshPeriod      = Seconds(3)()
  var buildingPlacementMaxTilesToEvaluate = 300
  var buildingPlacementMaximumQueue       = 12
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualizeScreen                     = true
  var visualizeMap                        = true
  var visualizationProbabilityHappyVision = 0.0 // 0.05
  var visualizationProbabilityTextOnly    = 0.0 // 0.01
  var visualizationCullViewport           = true

  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight

  ///////////////
  // Debugging //
  ///////////////
  var trackUnit = false
}
