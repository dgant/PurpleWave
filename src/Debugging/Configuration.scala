package Debugging

import java.io.File

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With

class Configuration {

  /////////////////////////
  // Tournament Settings //
  /////////////////////////
  
  var enableSurrenders                = false
  var enablePerformanceStops          = true
  var enablePerformanceSurrender      = false
  var enableChat                      = false
  var enableStreamManners             = false
  var identifyGhostUnits              = false
  var targetFrameDurationMilliseconds = 70
  
  //////////////
  // Strategy //
  //////////////

  var dynamicStickiness   = 4.0
  var targetWinrate       = 0.8
  var strategyRandomness  = 0.1
  var historyHalfLife     = 300.0
  var recentFingerprints  = 2
  
  /////////////
  // Battles //
  /////////////

  var enableMCRS                    = false
  var enableThreatAwarePathfinding  = true
  var retreatTowardsHomeOptional    = enableThreatAwarePathfinding // Works much better with threat-aware on
  var avatarBattleDistancePixels    = 32.0 * 6.0
  var battleMarginTileBase          = 12 + 2
  var battleMarginTileMinimum       = 12 + 2
  var battleMarginTileMaximum       = 12 * 2 + 2 // A bit over double Siege Tank range
  var battleHysteresisFrames        = GameTime(0, 6)()
  var baseTarget                    = 0.04 // 0.55 -> 0.1 from SSCAIT 2018/ AIST2
  var simulationFrames              = GameTime(0, 12)()
  var simulationEstimationPeriod    = 6
  var simulationScoreHalfLife       = GameTime(0, 2)()
  var simulationBonusTankRange      = 64.0
  var simulationDamageValueRatio    = 0.1
  
  ///////////
  // Micro //
  ///////////
  
  var concaveMarginPixels             = 20.0
  var fogPositionDurationFrames       = GameTime(0, 20)()
  var violenceThresholdFrames         = GameTime(0, 2)()
  var pickupRadiusPixels              = 48 //No idea what actual value is
  var workerDefenseRadiusPixels       = 32.0 * 4.0
  
  ///////////
  // Macro //
  ///////////
  
  var minimumMineralsBeforeMinedOut   = 150 * 8
  var maxFramesToSendAdvanceBuilder   = GameTime(0, 40)()
  var maxFramesToTrustBuildRequest    = GameTime(10, 0)()
  var blockerMineralThreshold         = 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  var maxPlacementAgeFrames           = GameTime(0, 8)()
  var enableTightBuildingPlacement    = false
  
  /////////////////
  // Performance //
  /////////////////

  var doAbsolutelyNothing                 = false
  var foreignUnitUpdatePeriod             = 4
  val friendlyUnitUpdatePeriod            = 4
  var performanceMinimumUnitSleep         = 2
  var maximumGamesHistoryPerOpponent      = 500
  
  var urgentBuildingPlacement             = true
  var urgentBuildingPlacementCutoffFrames = GameTime(15, 0)()
  var urgentBuildingPlacementCooldown     = GameTime(0, 1)()
  var buildingPlacementMaxTilesToEvaluate = 300
  var buildingPlacementBatchSize          = 300
  var buildingPlacementBatchingStartFrame = GameTime(4, 0)()
  var buildingPlacementMaximumQueue       = 12
  
  var urgencyManners            = 1
  var urgencyEconomy            = 1
  var urgencyGeography          = 1
  var urgencyArchitecture       = 1
  var urgencyGrids              = 2
  var urgencyPlanning           = 5
  var urgencyBattles            = 20
  var urgencyUnitTracking       = 20
  var urgencyMicro              = 100
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  var visualizeScreen                     = true
  var visualizeMap                        = true
  var visualizationProbabilityHappyVision = 0.05
  var visualizationProbabilityTextOnly    = 0.01
  var visualizationCullViewport           = true
  
  var camera                      = false
  var cameraDynamicSpeed          = false
  var cameraDynamicSpeedSlowest   = 30
  var cameraDynamicSpeedFastest   = 0
  var cameraViewportWidth         = 640
  var cameraViewportHeight        = 362
  var conservativeViewportWidth   = 640 + cameraViewportWidth
  var conservativeViewportHeight  = 480 + cameraViewportHeight

  class FileFlag(filename: String) {
    private lazy val fullPath: String = With.bwapiData.ai + filename
    private lazy val enabled: Boolean = {
      try {
        new File(fullPath).exists()
      }
      catch { case exception: Exception =>
        With.logger.warn("Exception looking for flag file at: " + fullPath)
        With.logger.onException(exception)
        false
      }
    }
    def apply(): Boolean = enabled
  }

  val humanMode = new FileFlag("human-mode-is.on")
  val visualize = new FileFlag("visualizations-are.on")
  val debugging = new FileFlag("debugging-is.on")
  def debugPauseThreshold: Int = if (debugging()) 250 else 24 * 60 * 60
}
