package Information.Battles.Clustering

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(seedUnits: Vector[UnitInfo]) {

  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  val stragglers: mutable.Queue[UnitInfo] = new mutable.Queue[UnitInfo]()
  seedUnits.view.filter(_.isEnemy).foreach(horizon.push)
  seedUnits.view.filter(_.isFriendly).foreach(stragglers.+=)

  private var _clusters: Option[Iterable[ArrayBuffer[UnitInfo]]] = None
  def clusters: Iterable[Iterable[UnitInfo]] = _clusters.getOrElse(Iterable.empty)
  def isComplete: Boolean = _clusters.isDefined

  def step() {
    if (horizon.nonEmpty) linkFromHorizon()
    else if (stragglers.nonEmpty) linkStraggler()
    else if ( ! isComplete) compileClusters()
  }

  def linkFromHorizon() {
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
        val nextTileI = nextTile.i
        val explorationGrid = if (isFriendly) With.battles.clustering.exploredFriendly else With.battles.clustering.exploredEnemy
        if ( ! explorationGrid.getUnchecked(nextTileI)) {
          explorationGrid.setUnchecked(nextTileI, true)
          val neighbors = With.grids.units.getUnchecked(nextTileI)
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
    newFoes.view.filter(seedUnits.contains).foreach(horizon.push)
  }

  private def linkStraggler(): Unit = {
    val straggler = stragglers.dequeue()
    if (unitLinks.contains(straggler)) return
    val clusteredSquadmates = straggler.friendly.get.squadmates.view.filter(unitLinks.contains)
    val clusteredSquadmateClosest = ByOption.minBy(clusteredSquadmates)(straggler.pixelDistanceEdge)
    clusteredSquadmateClosest.foreach(unitLinks(straggler) = _)
  }

  private def compileClusters(): Unit = {
    val clusters = unitLinks.view.filter(p => p._1 == p._2).map(_._1).map(root => (root, new ArrayBuffer[UnitInfo])).toMap
    unitLinks.keys.foreach(unit => clusters(getRoot(unit)) += unit)
    _clusters = Some(clusters.values)
  }


  @inline @tailrec private def getRoot(unit: UnitInfo): UnitInfo = {
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
}
