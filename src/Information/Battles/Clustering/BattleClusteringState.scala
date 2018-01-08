package Information.Battles.Clustering

import Lifecycle.With
import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(seedUnits: Set[UnitInfo]) {
  
  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  
  lazy val exploredFriendly  : Array[Boolean] = new Array(With.geography.allTiles.size)
  lazy val exploredEnemy     : Array[Boolean] = new Array(With.geography.allTiles.size)
  
  horizon.pushAll(seedUnits.toSeq.filter(_.isEnemy))
  
  def isComplete: Boolean = horizon.isEmpty
  
  def step() {
    if (isComplete) return
  
    var oldFoe: Option[UnitInfo] = None
    var newFoes     = new ArrayBuffer[UnitInfo]
    val nextUnit    = horizon.pop()
    val tileRadius  = radiusTiles(nextUnit)
    val tileCenter  = nextUnit.tileIncludingCenter
    val isFriendly  = nextUnit.isFriendly
    for (point <- Circle.points(tileRadius)) {
      val nextTile = tileCenter.add(point)
      if (nextTile.valid) {
        val exploredGrid = if (isFriendly) exploredFriendly else exploredEnemy
        if ( ! exploredGrid(nextTile.i)) {
          exploredGrid(nextTile.i) = true
          for (neighbor <- With.grids.units.get(nextTile)) {
            if (areOppositeTeams(nextUnit, neighbor) && seedUnits.contains(neighbor)) {
              if (unitLinks.contains(neighbor)) {
                if (oldFoe.isEmpty) {
                  oldFoe = Some(neighbor)
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
    for (newFoe <- newFoes) {
      unitLinks.put(newFoe, nextUnit)
    }
    if ( ! unitLinks.contains(nextUnit)) {
      unitLinks.put(nextUnit, oldFoe.getOrElse(nextUnit))
    }
    horizon.pushAll(newFoes.filter(seedUnits.contains))
  }
  
  private lazy val finalClusters: Vector[Set[UnitInfo]] = {
    val roots = unitLinks.toSeq.filter(p => p._1 == p._2).map(_._1)
    val clusters = roots.map(root => (root, new ArrayBuffer[UnitInfo] :+ root)).toMap
    // This could be faster if we didn't have to find more
    unitLinks.keys.foreach(unit => {
      val unitRoot = getRoot(unit)
      clusters(unitRoot) += unit
    })
    val output = clusters.toVector.map(_._2.toSet)
    output
  }
  
  def clusters: Vector[Set[UnitInfo]] = {
    if (isComplete)
      finalClusters
    else
      Vector.empty
  }
  
  @tailrec
  private def getRoot(unit: UnitInfo): UnitInfo = {
    val linkedUnit = unitLinks(unit)
    if (unit == linkedUnit) unit else getRoot(linkedUnit)
  }
  
  private def radiusTiles(unit: UnitInfo): Int ={
    val tilesDetecting  = if (unit.unitClass.isDetector) 11 else 0
    val tilesCasting    = if (unit.unitClass.isSpellcaster) 32 * 8 else 0
    val tilesAttacking  = unit.pixelRangeMax.toInt / 32
    val tilesMoving     = (unit.topSpeed * 24 * 4 / 32).toInt
    val tilesMargin     = 4 * Math.max(36,  With.reaction.framesTotal)
    val tilesCustom     = tilesMargin + tilesMoving + Vector(tilesCasting, tilesAttacking, tilesDetecting).max
    val tilesLimit      = With.configuration.battleMarginTiles
    val output          = Math.min(tilesLimit, tilesCustom)
    output
  }
  
  @inline
  private def areOppositeTeams(a: UnitInfo, b: UnitInfo): Boolean = {
    a.isFriendly != b.isFriendly
  }
}
