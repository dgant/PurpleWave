package Information.Battles.Clustering

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(seedUnits: Vector[UnitInfo]) {

  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  horizon.pushAll(seedUnits.filter(_.isEnemy))
  
  def isComplete: Boolean = horizon.isEmpty
  
  def step() {
    if (isComplete) return
  
    var linkedFoe: Option[UnitInfo] = None
    var newFoes     = new ArrayBuffer[UnitInfo]
    val nextUnit    = horizon.pop()
    val tileRadius  = radiusTiles(nextUnit)
    val tileCenter  = nextUnit.tileIncludingCenter
    val isFriendly  = nextUnit.isFriendly
    val points      = Circle.points(tileRadius)
    val nPoints     = points.size
    var iPoint      = 0
    while (iPoint < nPoints) {
      val nextTile = tileCenter.add(points(iPoint))
      iPoint += 1
      if (nextTile.valid) {
        val exploredGrid = if (isFriendly) With.battles.clustering.exploredFriendly else With.battles.clustering.exploredEnemy
        if ( ! exploredGrid.get(nextTile.i)) {
          exploredGrid.set(nextTile.i, true)
          val neighbors = With.grids.units.get(nextTile)
          val nNeighbors = neighbors.size
          var iNeighbor = 0
          while (iNeighbor < nNeighbors) {
            val neighbor = neighbors(iNeighbor)
            iNeighbor += 1
            if (areOppositeTeams(nextUnit, neighbor) && neighbor.clusteringEnabled) {
              if (neighbor.clusterParent.isDefined) {
                if (linkedFoe.isEmpty) {
                  linkedFoe = Some(neighbor)
                }
              }
              else {
                newFoes += neighbor
              }
            }
          }
        }
      }
    }
    val nFoes = newFoes.size
    var iFoe = 0
    while (iFoe < nFoes) {
      newFoes(iFoe).clusterChild = Some(nextUnit)
      nextUnit.clusterParent = Some(newFoes(iFoe))
      iFoe += 1
    }
    if (nextUnit.clusterParent.isEmpty) {
      nextUnit.clusterParent = linkedFoe.orElse(Some(nextUnit))
    }
    horizon.pushAll(newFoes.filter(seedUnits.contains))
  }
  
  private lazy val finalClusters: Vector[Vector[UnitInfo]] = {
    val roots = seedUnits.view.filter(_.clusterRoot).toArray
    val nRoots = roots.length
    var iRoot = 0
    while (iRoot < nRoots) {
      val unit = roots(iRoot)
      unit.cluster.clear()
      iRoot += 1
    }

    // This could be faster if we didn't have to find more
    seedUnits.foreach(unit => getRoot(unit).cluster += unit)
    roots.view.map(_.cluster.toVector).toVector
  }
  
  def clusters: Vector[Vector[UnitInfo]] = {
    if (isComplete)
      finalClusters
    else
      Vector.empty
  }
  
  @tailrec
  private def getRoot(unit: UnitInfo): UnitInfo = {
    val linkedUnit = unit.clusterParent.getOrElse(unit)
    if (linkedUnit == unit) unit else getRoot(linkedUnit)
  }
  
  private def radiusTiles(unit: UnitInfo): Int ={
    val tilesSpeed  = if (unit.is(Protoss.Interceptor)) 8 else (unit.topSpeed * With.reaction.clusteringMax / 32).toInt
    val tilesMargin = Math.max(tilesSpeed, With.configuration.battleMarginTileBase)
    PurpleMath.clamp(
      tilesMargin + unit.effectiveRangePixels.toInt / 32,
      With.configuration.battleMarginTileMinimum, With.configuration.battleMarginTileMaximum)
  }
  
  @inline
  private def areOppositeTeams(a: UnitInfo, b: UnitInfo): Boolean = (
    a.isFriendly != b.isFriendly
    && ! a.isNeutral
    && ! b.isNeutral
  )
}
