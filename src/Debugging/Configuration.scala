package Debugging

class Configuration {
  var gameSpeed                         = 0
  
  var enableStdOut                      = false
  var enableChat                        = true
  var enableLatencyCompensation         = false
  
  var enableCamera                      = false
  var cameraDynamicSpeed                = false
  var cameraDynamicSpeedSlowest         = 30
  var cameraDynamicSpeedFastest         = 0
  
  var enableVisualization               = true
  var enableVisualizationBases          = false
  var enableVisualizationBattles        = false
  var enableVisualizationBullets        = false
  var enableVisualizationChokepoints    = false
  var enableVisualizationEconomy        = false
  var enableVisualizationGrids          = false
  var enableVisualizationTileHeuristics = true
  var enableVisualizationPerformance    = false
  var enableVisualizationPlans          = false
  var enableVisualizationResources      = false
  var enableVisualizationScheduler      = false
  var enableVisualizationGeography      = false
  var enableVisualizationTextOnly       = false
  var enableVisualizationUnitsForeign   = false
  var enableVisualizationUnitsOurs      = false
  var enableVisualizationVectorUnits    = false
  var enableVisualizationZones          = false
  
  var enableGoonStopProtection          = true
}
