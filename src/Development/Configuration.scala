package Development

class Configuration {
  var gameSpeed                         = 0
  
  var enableStdOut                      = false
  var enableChat                        = true
  var enableLatencyCompensation         = false
  
  var enableCamera                      = false
  var cameraDynamicSpeed                = false
  var cameraDynamicSpeedMin             = 30
  var cameraDynamicSpeedMax             = 0
  
  var enableVisualization               = true
  var enableVisualizationBattles        = true
  var enableVisualizationEconomy        = true
  var enableVisualizationGrids          = true
  var enableVisualizationPerformance    = true
  var enableVisualizationPlans          = false
  var enableVisualizationResources      = true
  var enableVisualizationScheduler      = false
  var enableVisualizationGeography      = false
  var enableVisualizationUnitsForeign   = true
  var enableVisualizationUnitsOurs      = true
}
