package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(seedUnits: Set[UnitInfo]) {
  
  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  
  horizon.pushAll(seedUnits.toSeq.filter(_.isEnemy))
  
  def isComplete: Boolean = horizon.isEmpty
  
  def step() {
    if (isComplete) return
    
    val next    = horizon.pop()
    val allFoes = foesNear(next)
    val oldFoe  = allFoes.find(unitLinks.contains)
    val newFoes = allFoes.filterNot(unitLinks.contains)
  
    newFoes.foreach(unitLinks.put(_, next))
    if ( ! unitLinks.contains(next)) {
      unitLinks.put(next, oldFoe.getOrElse(next))
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
  
  private def foesNear(unit: UnitInfo): Iterable[UnitInfo] = {
    val tileRadius = radiusTiles(unit)
    val enemies = With.units
      .inTileRadius(unit.tileIncludingCenter, tileRadius.toInt)
      .toIterable
      .filter(foundUnit =>
        foundUnit.isEnemyOf(unit) &&
        seedUnits.contains(foundUnit))
    enemies
  }
  
  private def radiusTiles(unit: UnitInfo): Int ={
    val tilesDetecting  = if (unit.unitClass.isDetector) 11 else 0
    val tilesCasting    = if (unit.unitClass.isSpellcaster) 32 * 8 else 0
    val tilesAttacking  = unit.pixelRangeMax.toInt / 32
    val tilesMoving     = (unit.topSpeed * 24 * 4 / 32).toInt
    val tilesMargin     = 5
    val tilesCustom     = tilesMargin + tilesMoving + Vector(tilesCasting, tilesAttacking, tilesDetecting).max
    val tilesLimit      = With.configuration.battleMarginTiles
    val output          = Math.min(tilesLimit, tilesCustom)
    output
  }
}
