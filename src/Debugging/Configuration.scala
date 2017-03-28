package Debugging

class Configuration {
  
  val combatStickinessLeash           = 64
  var enableGoonStopProtection        = true
  var enablePathRecalculation         = false
  var enableHeuristicTravel           = false
  var enableFastGroundDistance        = false
  
  var gameSpeed                         = 0
  var enableStdOut                      = false
  var enableChat                        = true
  var enableLatencyCompensation         = false
  
  var enableCamera                      = false
  var cameraDynamicSpeed                = false
  var cameraDynamicSpeedSlowest         = 30
  var cameraDynamicSpeedFastest         = 0
  
  var enableVisualization                   = true
  var enableVisualizationBases              = true
  var enableVisualizationBattles            = true
  var enableVisualizationBullets            = false
  var enableVisualizationChokepoints        = true
  var enableVisualizationEconomy            = false
  var enableVisualizationGrids              = false
  var enableVisualizationMovementHeuristics = true
  var enableVisualizationPerformance        = true
  var enableVisualizationPlans              = false
  var enableVisualizationResources          = false
  var enableVisualizationScheduler          = false
  var enableVisualizationGeography          = true
  var enableVisualizationTextOnly           = false
  var enableVisualizationUnitsForeign       = true
  var enableVisualizationUnitsOurs          = true
  var enableVisualizationVectorUnits        = false
  var enableVisualizationReservations       = true
}
