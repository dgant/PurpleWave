package Information.Scouting

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

trait Tugging {
  def tugDistance: Int = With.geography.home.groundTiles(With.scouting.enemyHome)
  def enemyProgress: Double = Maff.clamp(1 - enemyMuscleOrigin.groundTiles(With.geography.home).toDouble / tugDistance, 0, 1)
  def ourProgress: Double = Maff.clamp(1 - ourMuscleOrigin.groundTiles(With.scouting.enemyHome).toDouble / tugDistance, 0, 1)

  def enemyThreatOrigin: Tile = _enemyThreatOrigin()
  def enemyMuscleOrigin: Tile = _enemyMuscleOrigin()
  def ourThreatOrigin: Tile = _ourThreatOrigin()
  def ourMuscleOrigin: Tile = _ourMuscleOrigin()

  private lazy val productionWeight = 5 * Terran.Marine.subjectiveValue
  private val _enemyThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.enemy.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.scouting.enemyHome.center, productionWeight))).walkableTile
  })

  private val _ourThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.ours.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.geography.home.center, productionWeight))).walkableTile
  })

  private val _enemyMuscleOrigin = new Cache(() => {
    val muscle = With.units.enemy.filter(countMuscle)
    if (muscle.isEmpty) With.scouting.enemyHome.walkableTile
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  private val _ourMuscleOrigin = new Cache(() => {
    val muscle = With.units.ours.filter(countMuscle)
    if (muscle.isEmpty) With.geography.home
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  @inline private def countMuscle(u: UnitInfo): Boolean = {
    u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker && ! u.unitClass.isBuilding
  }
}
