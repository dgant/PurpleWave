package Debugging

class Configuration {
  
  val combatEvaluationDistanceTiles   = 15
  val combatDistancePenalty           = 0.25
  val combatMovementPenalty           = 0.5
  val combatCooldownPenalty           = 0.75
  val combatStickinessLeash           = 64
  var enableGoonStopProtection        = true
  var enablePathRecalculation         = false
  var enableHeuristicTravel           = false
  var enableFastGroundDistance        = true
  
  var enableVisualization                   = true
  var enableVisualizationBases              = false
  var enableVisualizationBattles            = true
  var enableVisualizationBullets            = false
  var enableVisualizationChokepoints        = false
  var enableVisualizationEconomy            = false
  var enableVisualizationGeography          = true
  var enableVisualizationGrids              = false
  var enableVisualizationMovementHeuristics = true
  var enableVisualizationPerformance        = true
  var enableVisualizationPlans              = false
  var enableVisualizationResources          = false
  var enableVisualizationRealEstate         = true
  var enableVisualizationScheduler          = false
  var enableVisualizationTextOnly           = false
  var enableVisualizationUnitsForeign       = true
  var enableVisualizationUnitsOurs          = true
  var enableVisualizationVectorUnits        = false
  
  var gameSpeed                         = 0
  var enableSurrendering                = true
  var enableStdOut                      = false
  var enableChat                        = true
  var enableLatencyCompensation         = false
  
  var enableCamera                      = false
  var cameraDynamicSpeed                = false
  var cameraDynamicSpeedSlowest         = 30
  var cameraDynamicSpeedFastest         = 0
  var viewportWidth                     = 640 * 2
  var viewportHeight                    = 480 * 2
}
