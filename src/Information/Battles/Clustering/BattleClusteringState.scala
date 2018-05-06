package Information.Battles.Clustering

import Lifecycle.With
import Mathematics.PurpleMath
import Mathematics.Shapes.Circle
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(seedUnits: Set[UnitInfo]) {
  
  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  
  lazy val exploredFriendly  : Array[Boolean] = new Array(With.geography.allTiles.length)
  lazy val exploredEnemy     : Array[Boolean] = new Array(With.geography.allTiles.length)
  
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
    val tilesCasting    = if (unit.unitClass.isSpellcaster) 10 else 0
    val tilesAttacking  = 1 + Math.ceil(unit.pixelRangeMax / 32).toInt
    val tilesSpeed      = if (unit.is(Protoss.Interceptor)) 8 else (unit.topSpeed * With.reaction.clusteringMax / 32).toInt
    val tilesMargin     = Math.max(tilesSpeed, With.configuration.battleMarginTileBase)
    val tilesCustom     = tilesMargin + Vector(tilesCasting, tilesAttacking, tilesDetecting).max
    val output          = PurpleMath.clamp(tilesCustom, With.configuration.battleMarginTileMinimum, With.configuration.battleMarginTileMaximum)
    output
  }
  
  @inline
  private def areOppositeTeams(a: UnitInfo, b: UnitInfo): Boolean = (
    a.isFriendly != b.isFriendly
    && ! a.isNeutral
    && ! b.isNeutral
  )
}
