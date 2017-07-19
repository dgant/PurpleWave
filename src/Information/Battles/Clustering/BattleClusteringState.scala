package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClusteringState(units: Traversable[UnitInfo]) {
  
  val unitsByLink = new mutable.HashMap[UnitInfo, UnitInfo]
  val horizon: mutable.Stack[UnitInfo] = mutable.Stack[UnitInfo]()
  
  horizon.pushAll(units.toSeq.filter(_.isEnemy))
  
  def isComplete: Boolean = horizon.isEmpty
  
  def step() {
    if (isComplete) return
    
    val next = horizon.pop()
    if ( ! unitsByLink.contains(next)) {
      unitsByLink.put(next, next)
    }
    val foes = foesNear(next).filterNot(unitsByLink.contains)
    foes.foreach(foe => unitsByLink.put(foe, next))
    horizon.pushAll(foes)
  }
  
  def clusters: Vector[Vector[UnitInfo]] = {
    val roots = unitsByLink.toSeq.filter(p => p._1 == p._2).map(_._1)
    val clusters = roots.map(root => (root, new ArrayBuffer[UnitInfo] :+ root)).toMap
    // This could be faster if we didn't have to find more
    unitsByLink.keys.foreach(unit => {
      val root = getRoot(unit)
      clusters(root) :+ root
    })
    val output = clusters.toVector.map(_._2.toVector)
    output
  }
  
  @tailrec
  private def getRoot(unit: UnitInfo): UnitInfo = {
    if (unit == unitsByLink(unit)) unit else getRoot(unit)
  }
  
  private def foesNear(unit: UnitInfo): Iterable[UnitInfo] = {
    val tileRadius = Math.min(With.configuration.battleMarginPixels, unit.pixelRangeMax + unit.topSpeed * With.configuration.battleEstimationFrames) / 32.0
    val enemies = With.units.inTileRadius(unit.tileIncludingCenter, tileRadius.toInt).toIterable.filter(_.isEnemyOf(unit))
    enemies
  }
}
