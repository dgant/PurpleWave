package Micro.Matchups

import ProxyBwapi.UnitInfo.UnitInfo

class MatchupGraph {
  private val maxUnitId = 10000 // Per jaj22, as usual
  
  val analyses = new Array[MatchupAnalysis](maxUnitId)
  
  def get(unit: UnitInfo): MatchupAnalysis = {
    if (analyses(unit.id) == null) {
      analyses(unit.id) = new MatchupAnalysis(unit)
    }
    analyses(unit.id)
  }
  
  def update() {
    var i = 0
    while (i < maxUnitId) {
      analyses(i) = null
      i += 1
    }
  }
}
