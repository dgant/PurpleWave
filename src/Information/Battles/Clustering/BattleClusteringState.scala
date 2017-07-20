package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(units: Traversable[UnitInfo]) {
  
  val unitLinks = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  
  horizon.pushAll(units.toSeq.filter(_.isEnemy))
  
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
    
    horizon.pushAll(newFoes)
  }
  
  private lazy val finalClusters: Vector[Vector[UnitInfo]] = {
    val roots = unitLinks.toSeq.filter(p => p._1 == p._2).map(_._1)
    val clusters = roots.map(root => (root, new ArrayBuffer[UnitInfo] :+ root)).toMap
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
    val linkedUnit = unitLinks(unit)
    if (unit == linkedUnit) unit else getRoot(linkedUnit)
  }
  
  private def foesNear(unit: UnitInfo): Iterable[UnitInfo] = {
    val tileRadius = Math.min(With.configuration.battleMarginPixels, unit.pixelRangeMax + unit.topSpeed * With.configuration.battleEstimationFrames) / 32.0
    val enemies = With.units.inTileRadius(unit.tileIncludingCenter, tileRadius.toInt).toIterable.filter(_.isEnemyOf(unit))
    enemies
  }
}
