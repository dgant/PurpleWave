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

  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
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
        val explorationGrid = if (isFriendly) With.battles.clustering.exploredFriendly else With.battles.clustering.exploredEnemy
        if ( ! explorationGrid.get(nextTile.i)) {
          explorationGrid.set(nextTile.i, true)
          val neighbors = With.grids.units.get(nextTile)
          val nNeighbors = neighbors.size
          var iNeighbor = 0
          while (iNeighbor < nNeighbors) {
            val neighbor = neighbors(iNeighbor)
            iNeighbor += 1
            if (neighbor.clusteringEnabled && nextUnit.isFriendly != neighbor.isFriendly) {
              if (unitLinks.contains(neighbor)) {
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
      unitLinks.put(newFoes(iFoe), nextUnit)
      iFoe += 1
    }
    if ( ! unitLinks.contains(nextUnit)) {
      unitLinks.put(nextUnit, linkedFoe.getOrElse(nextUnit))
    }
    horizon.pushAll(newFoes.filter(seedUnits.contains))
  }

  private lazy val finalClusters: Vector[Vector[UnitInfo]] = {
    val roots = unitLinks.toSeq.filter(p => p._1 == p._2).map(_._1)
    val clusters = roots.map(root => (root, new ArrayBuffer[UnitInfo])).toMap // Was :+ root before fixing dupes
    // This could be faster if we didn't have to find more
    unitLinks.keys.foreach(unit => {
      val unitRoot = getRoot(unit)
      clusters(unitRoot) += unit
    })
    val output = clusters.toVector.map(_._2.toVector)
    output
  }
  
  def clusters: Vector[Vector[UnitInfo]] = {
    if (isComplete)
      finalClusters
    else
      Vector.empty
  }
  
  @tailrec
  private def getRoot(unit: UnitInfo): UnitInfo = {
    //val linkedUnit = unit.clusterParent.getOrElse(unit)
    val linkedUnit = unitLinks(unit)
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
